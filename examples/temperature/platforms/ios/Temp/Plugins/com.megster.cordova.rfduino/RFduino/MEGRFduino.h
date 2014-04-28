//
//  MEGRFduino.h
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

#ifndef MEGRFduino_h
#define MEGRFduino_h

#import <Cordova/CDV.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "CBPeripheral+Extensions.h"

@interface MEGRFduino : CDVPlugin <CBCentralManagerDelegate, CBPeripheralDelegate> {
    NSString* connectCallbackId;
    NSString* onDataCallbackId;
    NSString* discoverPeripherialCallbackId;
}

@property (strong, nonatomic) NSMutableArray *peripherals;
@property (strong, nonatomic) CBCentralManager *manager;
@property (strong, nonatomic) CBPeripheral *activePeripheral;

- (void)discover:(CDVInvokedUrlCommand *)command;
- (void)list:(CDVInvokedUrlCommand *)command;

- (void)connect:(CDVInvokedUrlCommand *)command;
- (void)disconnect:(CDVInvokedUrlCommand *)command;

- (void)onData:(CDVInvokedUrlCommand *)command;
- (void)write:(CDVInvokedUrlCommand *)command;

- (void)isEnabled:(CDVInvokedUrlCommand *)command;
- (void)isConnected:(CDVInvokedUrlCommand *)command;

@end

#endif
