# RFduino Plugin for PhoneGap

This plugin enabled Bluetooth communication between a phone and an [RFduino](http://www.rfduino.com/).

## Supported Platforms

* iOS
* Android

Android 4.3 or greater is required. Update the generated cordova project from target 17 to 18 or 19

    $ android update project -p platforms/android -t android-19

## Limitations

This is an early version of plugin, the API is likely to change.

The current version will only connect to one RFduino at a time.

For this version rfduino.write() only accepts strings and does not check if data exceeds the max size.

# Installing

Install with Cordova cli

    $ cordova plugin add com.megster.cordova.rfduino

# API

## Methods

- [rfduino.discover](#discover)
- [rfduino.list](#list)

- [rfduino.connect](#connect)
- [rfduino.disconnect](#disconnect)

- [rfduino.onData](#ondata)
- [rfduino.write](#write)

- [rfduino.isEnabled](#isenabled)
- [rfduino.isConnected](#isconnected)


## discover

Discover RFduino devices

    rfduino.discover(seconds, success, failure);

### Description

Function `discover` discovers the local RFduino devices.  The success callback is called each time a peripheral is discovered.

    {
        "name": "RFduino",
        "uuid": "BD922605-1B07-4D55-8D09-B66653E51BBA",
        "advertising": "echo",
        "rssi": -79
    }

### Parameters

- __seconds__: Number of seconds to run discovery
- __success__: Success callback function that is invoked with a list of bonded devices.
- __failure__: Error callback function, invoked when error occurs. [optional]

### Quick Example

    rfduino.discover(3, function(device) {
        console.log(JSON.stringify(device));
    }, failure);

## list

Lists known devices

    rfduino.list(success, failure);

### Description

Function `list` lists the known RFduino devices.  The success callback is called with a list of objects.

This will return an empty list unless `discover` have previously run. You should prefer `discover` to `list`.

    [{
        "name": "RFduino",
        "uuid": "AEC00232-2F92-4033-8E80-FD4C2533769C",
        "advertising": "echo",
        "rssi": -79
    }, {
        "name": "RFduino",
        "uuid": "AEC00232-2F92-4033-8E80-FD4C2533769C",
        "advertising": "temp",
        "rssi": -55
    }]

### Parameters

- __success__: Success callback function that is invoked with a list of bonded devices.
- __failure__: Error callback function, invoked when error occurs. [optional]

### Quick Example

    rfduino.list(function(devices) {
        devices.forEach(function(device) {
            console.log(device.uuid);
        })
    }, failure);


## connect

Connect to a RFduino device.

    rfduino.connect(uuid, connectSuccess, connectFailure);

### Description

Function `connect` connects to a RFduino device.  The callback is long running.  Success will be called when the connection is successful.  Failure is called if the connection fails, or later if the connection disconnects. An error message is passed to the failure callback.

### Parameters

- __uuid__: UUID of the remote device
- __connectSuccess__: Success callback function that is invoked when the connection is successful.
- __connectFailure__: Error callback function, invoked when error occurs or the connection disconnects.

## disconnect

Disconnect.

    rfduino.disconnect([success], [failure]);

### Description

Function `disconnect` disconnects the current connection.

### Parameters

- __success__: Success callback function that is invoked when the connection is successful. [optional]
- __failure__: Error callback function, invoked when error occurs. [optional]

## onData

Adds a callback for processing data from the RFduino.

    rfduino.onData(success, failure);

### Description

Function `onData` registers a function that is called whenever phone receives data from the RFduino.

Raw data is passed from ObjectiveC the callback as an [ArrayBuffer](http://www.html5rocks.com/en/tutorials/webgl/typed_arrays/) and must be processed.

### Parameters

- __success__: Success callback function that is invoked when the connection is successful. [optional]
- __failure__: Error callback function, invoked when error occurs. [optional]

## write

Writes data to the currently connected device

    rfduino.write(data, success, failure);

### Description

Function `write` writes data to the connected device.  **Data must be a String.** for this version.

### Parameters

- __success__: Success callback function that is invoked when the connection is successful. [optional]
- __failure__: Error callback function, invoked when error occurs. [optional]

## isConnected

Reports the connection status.

    rfduino.isConnected(success, failure);

### Description

Function `isConnected` calls the success callback when connected to a peer and the failure callback when *not* connected.

### Parameters

- __success__: Success callback function that is invoked with a boolean for connected status.
- __failure__: Error callback function, invoked when error occurs. [optional]

### Quick Example

    rfduino.isConnected(
        function() {
            console.log("RFduino is connected");
        },
        function() {
            console.log("RFduino is *not* connected");
        }
    );

## isEnabled

Reports if bluetooth is enabled.

    rfduino.isEnabled(success, failure);

### Description

Function `isEnabled` calls the success callback when Bluetooth is enabled and the failure callback when Bluetooth is *not* enabled.

### Parameters

- __success__: Success callback function that is invoked with a boolean for connected status.
- __failure__: Error callback function, invoked when error occurs. [optional]

### Quick Example

    rfduino.isEnabled(
        function() {
            console.log("Bluetooth is enabled");
        },
        function() {
            console.log("Bluetooth is *not* enabled");
        }
    );

# License

Apache 2.0

# Feedback

Try the code. If you find an problem or missing feature, file an issue or create a pull request.

