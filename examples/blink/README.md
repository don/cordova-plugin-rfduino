# RFduino Blink Example

This example demonstrates the [Cordova RFduino Plugin](https://github.com/don/cordova-plugin-rfduino) by causing an LED to blink on an RFDuino when the user initiates it from their iPhone. 

This code expects [the Blink Sketch](https://gist.github.com/SaraJo/11353238) to be running on the device. The latest RFduino Arduino and iOS code is available in the [zip file available from  RFduino](http://www.rfduino.com/download.html).

These instructions assume you have Xcode 5 and [NodeJS](http://nodejs.org) installed on your system.

Install Cordova

    $ npm install cordova -g
    
Create the iOS project

    $ cd cordova-plugin-rfduino/examples/blink
    $ cordova platform add ios

Install the rfduino plugin

    $ cordova plugin add com.megster.cordova.rfduino
    
Use Xcode to install the application on your iOS device

    $ open platforms/ios/Blink.xcodeproj
    
Works on Android 4.3+ too

    $ cd cordova-plugin-rfduino/examples/blink
    $ cordova platform add android
    $ cordova plugin add com.megster.cordova.rfduino
    $ cordova run