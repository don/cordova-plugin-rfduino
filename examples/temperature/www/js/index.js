/* global mainPage, deviceList, refreshButton */
/* global detailPage, tempFahrenheit, tempCelsius, closeButton */
/* global rfduino, alert */
'use strict';

var arrayBufferToFloat = function (ab) {
    var a = new Float32Array(ab);
    return a[0];
};

var app = {
    initialize: function() {
        this.bindEvents();
        detailPage.hidden = true;
    },
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
        refreshButton.addEventListener('touchstart', this.refreshDeviceList, false);
        closeButton.addEventListener('touchstart', this.disconnect, false);
        deviceList.addEventListener('touchstart', this.connect, false); // assume not scrolling
    },
    onDeviceReady: function() {
        app.refreshDeviceList();
    },
    refreshDeviceList: function() {
        deviceList.innerHTML = ''; // empty the list
        rfduino.discover(5, app.onDiscoverDevice, app.onError);
        app.onDiscoverDevice({ uuid: '16C269CF-B2DB-40DC-8644-8F4C90610C72', rssi: -72, name: 'RFduino F1', advertising: 'temp' });
        app.onDiscoverDevice({ uuid: '263BD2A1-20BC-42FF-968C-E223E82258B8', rssi: -68, name: 'RFduino F2', advertising: 'echo'});
    },
    onDiscoverDevice: function(device) {
        var listItem = document.createElement('li'),
            html = '<b>' + device.name + '</b><br/>' +
                'RSSI: ' + device.rssi + '&nbsp;|&nbsp;' +
                'Advertising: ' + device.advertising + '<br/>' +
                device.uuid;

        listItem.setAttribute('uuid', device.uuid);
        listItem.innerHTML = html;
        deviceList.appendChild(listItem);
    },
    connect: function(e) {
        var uuid = e.target.getAttribute('uuid'),
            onConnect = function() {
                rfduino.onData(app.onData, app.onError);
                app.showDetailPage();
            };

        rfduino.connect(uuid, onConnect, app.onError);
    },
    onData: function(data) {
        console.log(data);
        var celsius = arrayBufferToFloat(data),
            fahrenheit = celsius * 1.8 + 32;

        tempCelsius.innerHTML = celsius.toFixed(2);
        tempFahrenheit.innerHTML = fahrenheit.toFixed(2);
    },
    disconnect: function() {
        rfduino.disconnect(app.showMainPage, app.onError);
    },
    showMainPage: function() {
        mainPage.hidden = false;
        detailPage.hidden = true;
    },
    showDetailPage: function() {
        mainPage.hidden = true;
        detailPage.hidden = false;
    },
    onError: function(reason) {
        alert(reason); // real apps should use notification.alert
    }
};
