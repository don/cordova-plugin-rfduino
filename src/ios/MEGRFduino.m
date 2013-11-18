//
//  MEGRFduino.m
//  RFduino Cordova Plugin
//
//  (c) 2103 Don Coleman
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

#import "MEGRFduino.h"
#import <Cordova/CDV.h>

@interface MEGRFduino()
- (CBPeripheral *)findPeripheralByUUID:(NSString *)uuid;
- (void)stopScanTimer:(NSTimer *)timer;
@end

@implementation MEGRFduino

@synthesize manager;
@synthesize peripherals;
@synthesize activePeripheral;

CBUUID *service_uuid;
CBUUID *send_characteristic_uuid;
CBUUID *receive_characteristic_uuid;
CBUUID *disconnect_characteristic_uuid;
NSArray *characteristics;

CBCharacteristic *send_characteristic;
CBCharacteristic *disconnect_characteristic;

- (void)pluginInitialize {
    
    NSLog(@"RFduino Cordova Plugin");
    NSLog(@"(c)2013 Don Coleman");

    [super pluginInitialize];
    
    peripherals = [NSMutableArray array];

    // RFduino Service and Characteristics
    service_uuid = [CBUUID UUIDWithString:@"2220"];
    receive_characteristic_uuid = [CBUUID UUIDWithString:@"2221"];
    send_characteristic_uuid = [CBUUID UUIDWithString:@"2222"];
    disconnect_characteristic_uuid = [CBUUID UUIDWithString:@"2223"];
    
    characteristics = [NSArray arrayWithObjects:send_characteristic_uuid, receive_characteristic_uuid, disconnect_characteristic_uuid, nil];
    
    manager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
}

#pragma mark - Cordova Plugin Methods

- (void)connect:(CDVInvokedUrlCommand *)command {
    
    NSLog(@"connect");
    CDVPluginResult *pluginResult = nil;
    NSString *uuid = [command.arguments objectAtIndex:0];
    
    CBPeripheral *peripheral = [self findPeripheralByUUID:uuid];
    
    if (peripheral) {
        NSLog(@"Connecting to peripheral with UUID : %@", uuid);
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [pluginResult setKeepCallbackAsBool:TRUE];
        connectCallbackId = [command.callbackId copy];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
        [manager connectPeripheral:peripheral options:nil];

    } else {
        NSString *error = [NSString stringWithFormat:@"Could not find peripheral %@.", uuid];
        NSLog(@"%@", error);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    
}

- (void)disconnect:(CDVInvokedUrlCommand*)command {
    
    NSLog(@"disconnect");
    
    CDVPluginResult *pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    if(activePeripheral) {
        
        NSData *data = [NSData data];
        [activePeripheral writeValue:data forCharacteristic:disconnect_characteristic type:CBCharacteristicWriteWithoutResponse];
        
        if (activePeripheral.isConnected) {
            [manager cancelPeripheralConnection:activePeripheral];
        }
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    connectCallbackId = nil;
}

// BLE specific plugin (future) should have write(uuid, characteristic, value)
- (void)write:(CDVInvokedUrlCommand*)command {
    NSLog(@"write");
    
    CDVPluginResult *pluginResult = nil;
    NSString *message = [command.arguments objectAtIndex:0];

    if (message != nil) {
        
        NSData *d = [message dataUsingEncoding:NSUTF8StringEncoding];
        
        // TODO need to check the max length
        [activePeripheral writeValue:d forCharacteristic:send_characteristic type:CBCharacteristicWriteWithoutResponse];
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"message was null"];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

// list previously discovered peripherials, only works after calling discover
- (void)list:(CDVInvokedUrlCommand*)command {
    
    CDVPluginResult *pluginResult = nil;
    
    NSMutableArray *deviceInfo = [NSMutableArray array];
    
    for (CBPeripheral *p in peripherals) {
        [deviceInfo addObject:[p asDictionary]];
    }
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray: deviceInfo];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)isEnabled:(CDVInvokedUrlCommand*)command {
    
    CDVPluginResult *pluginResult = nil;
    int bluetoothState = [manager state];
    
    BOOL enabled = bluetoothState == CBCentralManagerStatePoweredOn;
    
    if (enabled) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsInt:bluetoothState];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)discover:(CDVInvokedUrlCommand*)command {
    
    CDVPluginResult *pluginResult = nil;
    // TODO don't need var and with timer, pick one
    discoverPeripherialCallbackId = [command.callbackId copy];
    
    // this should be optional
    NSNumber *timeout = [command.arguments objectAtIndex:0];

    // TODO do I need to empty the existing?
    [manager scanForPeripheralsWithServices:[NSArray arrayWithObject:service_uuid] options:nil];
    
    [NSTimer scheduledTimerWithTimeInterval:[timeout floatValue]
                                     target:self
                                   selector:@selector(stopScanTimer:)
                                   userInfo:[command.callbackId copy]
                                    repeats:NO];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)onData:(CDVInvokedUrlCommand*)command {
    
    CDVPluginResult *pluginResult = nil;
    onDataCallbackId = [command.callbackId copy];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}


- (void)isConnected:(CDVInvokedUrlCommand*)command {
    
    CDVPluginResult *pluginResult = nil;
    
    if ([activePeripheral isConnected]) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Not connected"];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

#pragma mark - timers

-(void)stopScanTimer:(NSTimer *)timer {
    NSLog(@"stopScanTimer");
    
    [manager stopScan];
    
    
    CDVPluginResult *pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:discoverPeripherialCallbackId];
    discoverPeripherialCallbackId = nil;
}

#pragma mark - CBCentralManagerDelegate

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {
    
    // TODO need to check UUIDs for duplicates
    [peripherals addObject:peripheral];
    [peripheral setAdvertisementData:advertisementData RSSI:RSSI];
    
    if (discoverPeripherialCallbackId) {
        CDVPluginResult *pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[peripheral asDictionary]];
        [pluginResult setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:discoverPeripherialCallbackId];
    }
    
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central
{
    NSLog(@"Status of CoreBluetooth central manager changed %d %@",central.state,[self centralManagerStateToString:central.state]);
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    
    NSLog(@"didConnectPeripheral");
    
    // TODO don't do this in connect too
    peripheral.delegate = self;
    self.activePeripheral = peripheral;
    
    [peripheral discoverServices:[NSArray arrayWithObject:service_uuid]];
    
    CDVPluginResult *pluginResult = nil;
    if (connectCallbackId) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [pluginResult setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:connectCallbackId];
    }
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    
    NSLog(@"didDisconnectPeripheral");
    
    // TODO send PhoneGap more info from NSError
    
    CDVPluginResult *pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Disconnected"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:connectCallbackId];

    connectCallbackId = nil;
    
}

- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    
    NSLog(@"didFailToConnectPeripheral");
    
    // TODO send PhoneGap more info from NSError
    
    CDVPluginResult *pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Failed to Connect"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:connectCallbackId];
    
    connectCallbackId = nil;
    
}

#pragma mark CBPeripheralDelegate

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    
    NSLog(@"didDiscoverServices");
    
    for (CBService *service in peripheral.services) {
        if ([service.UUID isEqual:service_uuid]) {
            [peripheral discoverCharacteristics:characteristics forService:service];
        }
    }
    
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    
    NSLog(@"didDiscoverCharacteristicsForService");
    
    // assume it's the current peripheral for now
    if ([service.UUID isEqual:service_uuid]) {
        for (CBCharacteristic *characteristic in service.characteristics) {
            if ([characteristic.UUID isEqual: send_characteristic_uuid]) {
                send_characteristic = characteristic;
            } else if ([characteristic.UUID isEqual: receive_characteristic_uuid]) {
                [peripheral setNotifyValue:YES forCharacteristic:characteristic];
            } else if ([characteristic.UUID isEqual: disconnect_characteristic_uuid]) {
                disconnect_characteristic = characteristic;
            }
        }
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {

    // NSLog(@"didUpdateValueForCharacteristic");
    if ([characteristic.UUID isEqual:receive_characteristic_uuid]) {
        
        // send RAW data to Javascript
        if (onDataCallbackId) {
            NSData *data = characteristic.value;
            
            CDVPluginResult *pluginResult = nil;
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArrayBuffer:data];
            [pluginResult setKeepCallbackAsBool:TRUE];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:onDataCallbackId];
        }
    }
}

#pragma mark - internal implemetation

- (CBPeripheral*)findPeripheralByUUID:(NSString*)uuid {
    
    CBPeripheral *peripheral = nil;
    
    for (CBPeripheral *p in peripherals) {
        
        NSString* other = CFBridgingRelease(CFUUIDCreateString(nil, p.UUID));
        
        if ([uuid isEqualToString:other]) {
            peripheral = p;
            break;
        }
    }
    return peripheral;
}

#pragma mark - util

- (NSString*) centralManagerStateToString: (int)state
{
    switch(state)
    {
        case CBCentralManagerStateUnknown:
            return @"State unknown (CBCentralManagerStateUnknown)";
        case CBCentralManagerStateResetting:
            return @"State resetting (CBCentralManagerStateUnknown)";
        case CBCentralManagerStateUnsupported:
            return @"State BLE unsupported (CBCentralManagerStateResetting)";
        case CBCentralManagerStateUnauthorized:
            return @"State unauthorized (CBCentralManagerStateUnauthorized)";
        case CBCentralManagerStatePoweredOff:
            return @"State BLE powered off (CBCentralManagerStatePoweredOff)";
        case CBCentralManagerStatePoweredOn:
            return @"State powered up and ready (CBCentralManagerStatePoweredOn)";
        default:
            return @"State unknown";
    }
    
    return @"Unknown state";
}

@end
