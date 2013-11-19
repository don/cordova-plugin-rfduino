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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class RFduinoPlugin extends CordovaPlugin implements BluetoothAdapter.LeScanCallback {

    // actions
    private static final String DISCOVER = "discover";
    private static final String LIST = "list";

    private static final String CONNECT = "connect";
    private static final String DISCONNECT = "disconnect";

    private static final String ON_DATA = "onData";
    private static final String WRITE = "write";

    private static final String IS_ENABLED = "isEnabled";
    private static final String IS_CONNECTED  = "isConnected";

    // callbacks
    CallbackContext discoverCallback;

    private static final String TAG = "RFduinoPlugin";

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt gatt;
    Map<String, Peripheral> peripherals = new LinkedHashMap<String, Peripheral>();

    Peripheral activePeripheral;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        LOG.d(TAG, "action = " + action);

        if (bluetoothAdapter == null) {
            Activity activity = cordova.getActivity();
            BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        boolean validAction = true;

        if (action.equals(DISCOVER)) {

            int scanSeconds = args.getInt(0);
            findLowEnergyDevices(callbackContext, scanSeconds);

        } else if (action.equals(LIST)) {

            listKnownDevices(callbackContext);

        } else if (action.equals(CONNECT)) {

            String uuid = args.getString(0);
            connect(callbackContext, uuid);

        } else if (action.equals(ON_DATA)) {

            registerOnDataCallback(callbackContext);

        } else if (action.equals(DISCONNECT)) {

            disconnect(callbackContext);

        } else if (action.equals(WRITE)) {

            String data = args.getString(0); // TODO should be bytes here
            write(callbackContext, data.getBytes());

        } else if (action.equals(IS_ENABLED)) {

            if (bluetoothAdapter.isEnabled()) {
                callbackContext.success();
            } else {
                callbackContext.error("Bluetooth is disabled.");
            }

        } else if (action.equals(IS_CONNECTED)) {

            if (activePeripheral != null && activePeripheral.isConnected()) {
                callbackContext.success();
            } else {
                callbackContext.error("Not connected.");
            }

        } else {

            validAction = false;

        }

        return validAction;
    }

    private void connect(CallbackContext callbackContext, String uuid) {

        Peripheral peripheral = peripherals.get(uuid); // note uuid is mac address on android
        BluetoothDevice device = peripheral.getDevice();
        peripheral.setConnectCallback(callbackContext);
        gatt = device.connectGatt(cordova.getActivity(), false, peripheral);

        activePeripheral = peripheral;

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    private void disconnect(CallbackContext callbackContext) {

        if (activePeripheral != null) {
            BluetoothGattCharacteristic characteristic = activePeripheral.getDisconnectCharacteristic();
            characteristic.setValue("");
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            gatt.writeCharacteristic(characteristic);
            activePeripheral.disconnect();
        }

        if (gatt != null) {
            gatt.disconnect();
        }

        callbackContext.success();
    }

    private void write(CallbackContext callbackContext, byte[] data) {

        BluetoothGattCharacteristic characteristic = activePeripheral.getSendCharacteristic();
        characteristic.setValue(data);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        boolean success = gatt.writeCharacteristic(characteristic);

        if (success) {
            callbackContext.success();
        } else {
            callbackContext.error("Write Failed");
        }

    }

    private void registerOnDataCallback(CallbackContext callbackContext) {

        // TODO this should accept a MAC address and allow a listener to be added to
        // any device, not just the active device. Requires JS and ObjC updates.

        if (activePeripheral != null) {
            activePeripheral.setOnDataCallback(callbackContext);

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        } else {

            callbackContext.error("No connected device");

        }

    }

    private void findLowEnergyDevices(CallbackContext callbackContext, int scanSeconds) {

        // TODO skip if currently scanning
        peripherals.clear();

        discoverCallback = callbackContext;
        bluetoothAdapter.startLeScan(new UUID[] { Peripheral.RFDUINO_SERVICE_UUID }, this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LOG.d(TAG, "Stopping Scan");
                RFduinoPlugin.this.bluetoothAdapter.stopLeScan(RFduinoPlugin.this);
            }
        }, scanSeconds * 1000);

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    private void listKnownDevices(CallbackContext callbackContext) {

        JSONArray json = new JSONArray();

        // do we care about consistent order? will peripherals.values() be in order?
        for (Map.Entry<String, Peripheral> entry : peripherals.entrySet()) {
            Peripheral peripheral = entry.getValue();
            json.put(peripheral.asJSONObject());
        }

        PluginResult result = new PluginResult(PluginResult.Status.OK, json);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        String address = device.getAddress();

        if (!peripherals.containsKey(address)) {

            Peripheral peripheral = new Peripheral(device, rssi, scanRecord);
            peripherals.put(device.getAddress(), peripheral);

            if (discoverCallback != null) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, peripheral.asJSONObject());
                result.setKeepCallback(true);
                discoverCallback.sendPluginResult(result);
            }

        } else {
            // this isn't necessary
            Peripheral peripheral = peripherals.get(address);
            peripheral.updateRssi(rssi);
        }

    }

}
