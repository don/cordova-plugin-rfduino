# RFduino Button Example

This example demonstrates the [Cordova RFduino Plugin](https://github.com/don/cordova-plugin-rfduino) with the [Button Shield](http://www.rfduino.com/product/rfd22122-rgb-button-shield-for-rfduino/) similar to the [RFduino's LedButton iPhone app](https://itunes.apple.com/us/app/rfduino-ledbutton/id704045041?mt=8).

This code expects [RFduino's LED Button Sketch](https://gist.github.com/don/7947381#file-ledbutton-ino) to be running on the device. The latest RFduino Arduino and iOS code is available in the [zip file available from  RFduino](http://www.rfduino.com/).

These instructions assume you have Xcode 5 and [NodeJS](http://nodejs.org) installed on your system.

Install Cordova

    $ npm install cordova -g
    
Create the iOS project

    $ cd cordova-plugin-rfduino/examples/button
    $ cordova platform add ios

Install the rfduino plugin

    $ cordova plugin add com.megster.cordova.rfduino
    
Use Xcode to install the application on your iOS device

    $ open platforms/ios/Button.xcodeproj

Works on Android 4.3+ too

    $ cd cordova-plugin-rfduino/examples/button
    $ cordova platform add android
    $ cordova plugin add com.megster.cordova.rfduino
    $ cordova run