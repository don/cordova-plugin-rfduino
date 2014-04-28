# RFduino Temperature Example

This example demonstrates the [Cordova RFduino Plugin](https://github.com/don/cordova-plugin-rfduino) by duplicating [RFduino's iPhone Temperature example](https://github.com/RFduino/RFduino/tree/master/RFduino/iPhone%20Apps/rfduinoTemperature).

This code expects [RFduino's Temperature Sketch](https://github.com/RFduino/RFduino/tree/master/RFduino/libraries/RFduinoBLE/examples/Temperature) to be running on the device.

These instructions assume you have Xcode 5 and [NodeJS](http://nodejs.org) installed on your system.

Install Cordova

    $ npm install corodva -g
    
Create the iOS project

    $ cd cordova-plugin-rfduino/examples/temperature
    $ cordova platform add ios

Install the rfduino plugin

    $ cordova plugin add com.megster.cordova.rfduino
    
Use Xcode to install the application on your iOS device

    $ open platforms/ios/Temp.xcodeproj
