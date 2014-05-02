# RFduino Temperature Example

This example demonstrates the [Cordova RFduino Plugin](https://github.com/don/cordova-plugin-rfduino) by duplicating [RFduino's iPhone Temperature app](https://itunes.apple.com/us/app/rfduino-temperature/id668832196?mt=8).

This code expects [RFduino's Temperature Sketch](https://gist.github.com/don/7947381#file-temperature-ino) to be running on the device. The latest FRduino Arduino and iOS code is available in the [zip file available from  RFduino](http://www.rfduino.com/download.html).

These instructions assume you have Xcode 5 and [NodeJS](http://nodejs.org) installed on your system.

Install Cordova

    $ npm install cordova -g
    
Create the iOS project

    $ cd cordova-plugin-rfduino/examples/temperature
    $ cordova platform add ios

Install the rfduino plugin

    $ cordova plugin add com.megster.cordova.rfduino
    
Use Xcode to install the application on your iOS device

    $ open platforms/ios/Temp.xcodeproj

Works on Android 4.3+ too

    $ cd cordova-plugin-rfduino/examples/temperature
    $ cordova platform add android
    $ cordova plugin add com.megster.cordova.rfduino
    $ cordova run