// (c) 2103 Don Coleman
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.megster.cordova.rfduino;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Peripheral wraps the BluetoothDevice and provides methods to convert to JSON.
 */
public class Peripheral extends BluetoothGattCallback {

    // RFduino 2220, 2221, 2222, 2223
    public static final UUID RFDUINO_SERVICE_UUID = UUID.fromString("00002220-0000-1000-8000-00805F9B34FB");
    public static final UUID RECEIVE_CHARACTERISTIC_UUID = UUID.fromString("00002221-0000-1000-8000-00805F9B34FB");
    public static final UUID SEND_CHARACTERISTIC_UUID = UUID.fromString("00002222-0000-1000-8000-00805F9B34FB");
    public static final UUID DISCONNECT_CHARACTERISTIC_UUID = UUID.fromString("00002223-0000-1000-8000-00805F9B34FB");

    // 0x2902 org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
    public final static UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    private static final String TAG = "Peripheral";

    private BluetoothDevice device;
    private String advertising;
    private int advertisingRSSI;
    private boolean connected = false;

    private CallbackContext connectCallback;
    private CallbackContext onDataCallback;

    private BluetoothGattCharacteristic sendCharacteristic;
    private BluetoothGattCharacteristic disconnectCharacteristic;

    public Peripheral(BluetoothDevice device, int advertisingRSSI, byte[] scanRecord) {

        this.device = device;
        this.advertisingRSSI = advertisingRSSI;
        this.advertising = getAdvertisingValue(scanRecord);

    }

    public void disconnect() {
        connectCallback = null;
        connected = false;
    }

    public JSONObject asJSONObject()  {

        JSONObject json = new JSONObject();

        try {
            json.put("name", device.getName());
            json.put("uuid", device.getAddress()); // This is MAC address
            json.put("advertising", advertising );
            // TODO real RSSI if we have it, else
            json.put("rssi", advertisingRSSI);
        } catch (JSONException e) { // this shouldn't happen
            e.printStackTrace();
        }

        return json;
    }

    public void setConnectCallback(CallbackContext connectCallback) {
        this.connectCallback = connectCallback;
    }

    public void setOnDataCallback(CallbackContext onDataCallback) {
        this.onDataCallback = onDataCallback;
    }

    public boolean isConnected() {
        return connected;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public BluetoothGattCharacteristic getSendCharacteristic() {
        return sendCharacteristic;
    }

    public BluetoothGattCharacteristic getDisconnectCharacteristic() {
        return disconnectCharacteristic;
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, "gatt " + gatt);
        Log.d(TAG, "status " + status);
        super.onServicesDiscovered(gatt, status);

        BluetoothGattService service = gatt.getService(RFDUINO_SERVICE_UUID);
        Log.d(TAG, "service " + service);

        BluetoothGattCharacteristic receiveCharacteristic = service.getCharacteristic(RECEIVE_CHARACTERISTIC_UUID);
        sendCharacteristic = service.getCharacteristic(SEND_CHARACTERISTIC_UUID);
        disconnectCharacteristic = service.getCharacteristic(DISCONNECT_CHARACTERISTIC_UUID);

        if (receiveCharacteristic != null) {
            gatt.setCharacteristicNotification(receiveCharacteristic, true);

            BluetoothGattDescriptor receiveConfigDescriptor = receiveCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);
            if (receiveConfigDescriptor != null) {
                receiveConfigDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(receiveConfigDescriptor);
            } else {
                LOG.e(TAG, "Receive Characteristic can not be configured.");
            }
        } else {
            LOG.e(TAG, "Receive Characteristic is missing.");
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

        if (newState == BluetoothGatt.STATE_CONNECTED) {

            connected = true;
            gatt.discoverServices();

            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(true);
            connectCallback.sendPluginResult(result);

        } else {

            connected = false;
            if (connectCallback != null) {
                connectCallback.error("Disconnected");
                connectCallback = null;
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

        super.onCharacteristicChanged(gatt, characteristic);
        if (characteristic.getUuid().equals(RECEIVE_CHARACTERISTIC_UUID)) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, characteristic.getValue());
            result.setKeepCallback(true);
            onDataCallback.sendPluginResult(result);
        }
    }

    // The "advertising" value is RFduino specific and stored in the manufacturer data
    // https://www.bluetooth.org/en-us/specification/assigned-numbers/generic-access-profile
    // data is length type value length type value
    // Based on BluetoothHelper#parseScanRecord from https://github.com/lann/RFDuinoTest/
    private String getAdvertisingValue(byte[] scanRecord) {

        String advertising = "";

        int i = 0;
        while (i < scanRecord.length) {
            int length = scanRecord[i] & 0xFF;
            if (length == 0) { break; } // will this really happen?
            i++;
            int type = scanRecord[i] & 0xFF;
            if (type == 0xFF) { // manufacturer data
                // skip the first 2 char (because that's what the rfduino iOS code does)
                byte[] bytes = Arrays.copyOfRange(scanRecord, i + 3, i + length);
                try {
                    advertising = new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    advertising = "error";
                }
                break;
            } else {
                i += length;
            }
        }
        return advertising;
    }

    public void updateRssi(int rssi) {
        advertisingRSSI = rssi;
    }
}
