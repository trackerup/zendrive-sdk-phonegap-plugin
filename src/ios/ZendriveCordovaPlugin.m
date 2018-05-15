//
//  ZendriveCordovaPlugin.m
//

#import "ZendriveCordovaPlugin.h"
#import <ZendriveSDK/Zendrive.h>
#import <ZendriveSDK/ZendriveActiveDriveInfo.h>

#pragma mark - Common dictionary keys
static NSString * const kTrackingIdKey = @"trackingId";
static NSString * const kSessionIdKey = @"sessionId";

#pragma mark - ZendriveDriveStartInfo dictionary keys
static NSString * const kStartTimestampKey = @"startTimestamp";
static NSString * const kStartLocationKey = @"startLocation";

#pragma mark - ZendriveDriveInfo dictionary keys
static NSString * const kEndTimestampKey = @"endTimestamp";
static NSString * const kAverageSpeedKey = @"averageSpeed";
static NSString * const kDistanceKey = @"distance";
static NSString * const kWaypointsKey = @"waypoints";

#pragma mark - DriverAttributes
static NSString * const kDriverStartDateKey = @"driverStartDate";
static NSString * const kDriverFirstNameKey = @"firstName";
static NSString * const kDriverLastNameKey = @"lastName";
static NSString * const kDriverEmailKey = @"email";
static NSString * const kDriverGroupKey = @"group";
static NSString * const kDriverPhoneNumberKey = @"phoneNumber";

#pragma mark - ZendriveConfiguration
static NSString * const kConfigurationApplicationKey = @"applicationKey";
static NSString * const kConfigurationDriverIdKey = @"driverId";
static NSString * const kConfigurationDriveDetectionModeKey = @"driveDetectionMode";

#pragma mark - SetupKeys
static NSString * const kCustomAttributesKey = @"customAttributes";
static NSString * const kDriverAttributesKey = @"driverAttributes";

@interface ZendriveCordovaPlugin()<ZendriveDelegateProtocol>

// Delegate callback ids
@property (nonatomic, strong) NSString *processStartOfDriveCallbackId;
@property (nonatomic, strong) NSString *processEndOfDriveCallbackId;
@property (nonatomic, strong) NSString *processLocationDeniedCallbackId;
@end

@implementation ZendriveCordovaPlugin

- (void)setup:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            NSDictionary *configDictionary = [command argumentAtIndex:0];
            ZendriveConfiguration *configuration = [self configurationFromDictionary:configDictionary];

            [Zendrive
             setupWithConfiguration:configuration
             delegate:self
             completionHandler:^(BOOL success, NSError *error) {
                 CDVPluginResult* pluginResult = nil;
                 if(error == nil){
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                 }
                 else {
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString:error.localizedFailureReason];
                 }
                 [self.commandDelegate sendPluginResult:pluginResult
                                             callbackId:command.callbackId];
             }];
        }
    }];
}

-(void)teardown:(CDVInvokedUrlCommand *)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            [Zendrive teardown];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)startDrive:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            NSString *trackingId = [command argumentAtIndex:0];
            [Zendrive startDrive:trackingId];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)getActiveDriveInfo:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            ZendriveActiveDriveInfo *activeDriveInfo = [Zendrive activeDriveInfo];
            NSMutableDictionary *activeDriveInfoDictionary;
            if (activeDriveInfo) {
                activeDriveInfoDictionary = [[NSMutableDictionary alloc] init];
                activeDriveInfoDictionary[kStartTimestampKey] = @(activeDriveInfo.startTimestamp);
                activeDriveInfoDictionary[kTrackingIdKey] = [NSNull null];
                if (activeDriveInfo.trackingId) {
                    activeDriveInfoDictionary[kTrackingIdKey] = activeDriveInfo.trackingId;
                }
                activeDriveInfoDictionary[kSessionIdKey] = [NSNull null];
                if (activeDriveInfo.sessionId) {
                    activeDriveInfoDictionary[kSessionIdKey] = activeDriveInfo.sessionId;
                }
            }
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                          messageAsDictionary:activeDriveInfoDictionary];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)stopDrive:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            NSString *trackingId = [command argumentAtIndex:0];
            [Zendrive stopDrive:trackingId];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)startSession:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            [Zendrive startSession:[command argumentAtIndex:0]];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)stopSession:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            [Zendrive stopSession];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)setDriveDetectionMode:(CDVInvokedUrlCommand *)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            CDVPluginResult *pluginResult;
            NSNumber *modeNsNum = [command argumentAtIndex:0];
            ZendriveDriveDetectionMode mode = modeNsNum.intValue;
            [Zendrive setDriveDetectionMode:mode];

            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

#pragma mark - Delegate callbacks
- (void)setProcessStartOfDriveDelegateCallback:(CDVInvokedUrlCommand*)command {
    if (self.processStartOfDriveCallbackId) {
        // Delete the old callback
        // Sending NO_RESULT doesn't call any js callback method
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];

        // Setting keepCallbackAsBool to NO would make sure that the callback is deleted from
        // memory after this call
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult
                                    callbackId:self.processStartOfDriveCallbackId];
    }

    NSNumber *hasCallbackNsNum = [command argumentAtIndex:0];
    if (hasCallbackNsNum.boolValue) {
        self.processStartOfDriveCallbackId = command.callbackId;
    }
    else {
        self.processStartOfDriveCallbackId = nil;
    }
}

- (void)setProcessEndOfDriveDelegateCallback:(CDVInvokedUrlCommand*)command {
    if (self.processEndOfDriveCallbackId) {
        // Delete the old callback
        // Sending NO_RESULT doesn't call any js callback method
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];

        // Setting keepCallbackAsBool to NO would make sure that the callback is deleted from
        // memory after this call
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult
                                    callbackId:self.processEndOfDriveCallbackId];
    }

    NSNumber *hasCallbackNsNum = [command argumentAtIndex:0];
    if (hasCallbackNsNum.boolValue) {
        self.processEndOfDriveCallbackId = command.callbackId;
    }
    else {
        self.processEndOfDriveCallbackId = nil;
    }
}

- (void)setProcessLocationDeniedDelegateCallback:(CDVInvokedUrlCommand*)command {
    if (self.processLocationDeniedCallbackId) {
        // Delete the old callback
        // Sending NO_RESULT doesn't call any js callback method
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];

        // Setting keepCallbackAsBool to NO would make sure that the callback is deleted from
        // memory after this call
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult
                                    callbackId:self.processLocationDeniedCallbackId];
    }

    NSNumber *hasCallbackNsNum = [command argumentAtIndex:0];
    if (hasCallbackNsNum.boolValue) {
        self.processLocationDeniedCallbackId = command.callbackId;
    }
    else {
        self.processLocationDeniedCallbackId = nil;
    }
}

#pragma mark - ZendriveDelegateProtocol
- (void)processStartOfDrive:(ZendriveDriveStartInfo *)startInfo {
    if (!self.processStartOfDriveCallbackId) {
        return;
    }
    id startLocationDictionary = [NSNull null];
    if (startInfo.waypoints && startInfo.waypoints.count > 0) {
        startLocationDictionary = [startInfo.waypoints[0] toDictionary];
    }
    NSDictionary *startInfoDictionary = @{kStartTimestampKey:@(startInfo.startTimestamp),
                                          kStartLocationKey:startLocationDictionary};

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                  messageAsDictionary:startInfoDictionary];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:self.processStartOfDriveCallbackId];
}

- (void)processEndOfDrive:(ZendriveDriveInfo *)driveInfo {
    if (!self.processEndOfDriveCallbackId) {
        return;
    }
    NSMutableArray *waypointsArray = [[NSMutableArray alloc] init];
    for (ZendriveLocationPoint *waypoint in driveInfo.waypoints) {
        NSDictionary *waypointDictionary = [waypoint toDictionary];
        [waypointsArray addObject:waypointDictionary];
    }
    NSDictionary *driveInfoDictionary = @{kStartTimestampKey:@(driveInfo.startTimestamp),
                                          kEndTimestampKey:@(driveInfo.endTimestamp),
                                          kAverageSpeedKey:@(driveInfo.averageSpeed),
                                          kDistanceKey:@(driveInfo.distance),
                                          kWaypointsKey:waypointsArray};

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                  messageAsDictionary:driveInfoDictionary];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:self.processEndOfDriveCallbackId];
}

- (void)processLocationDenied {
    if (!self.processLocationDeniedCallbackId) {
        return;
    }
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:self.processLocationDeniedCallbackId];
}

#pragma mark - Utility methods

- (ZendriveDriverAttributes *)driverAttrsFromDictionary:(NSDictionary *)driverAttrsDictionary {
    if ([self isNULL:driverAttrsDictionary]) {
        return nil;
    }

    ZendriveDriverAttributes *driverAttributes = [[ZendriveDriverAttributes alloc] init];
    NSNumber *startDateTimestamp = [driverAttrsDictionary objectForKey:kDriverStartDateKey];
    if (![self isNULL:startDateTimestamp]) {
        [driverAttributes setDriverStartDate:
         [NSDate dateWithTimeIntervalSince1970:startDateTimestamp.longValue]];
    }

    NSString *firstName = [driverAttrsDictionary objectForKey:kDriverFirstNameKey];
    if (![self isNULL:firstName]) {
        [driverAttributes setFirstName:firstName];
    }

    NSString *lastName = [driverAttrsDictionary objectForKey:kDriverLastNameKey];
    if (![self isNULL:lastName]) {
        [driverAttributes setLastName:lastName];
    }

    NSString *email = [driverAttrsDictionary objectForKey:kDriverEmailKey];
    if (![self isNULL:email]) {
        [driverAttributes setEmail:email];
    }

    NSString *group = [driverAttrsDictionary objectForKey:kDriverGroupKey];
    if (![self isNULL:group]) {
        [driverAttributes setGroup:group];
    }

    NSString *phoneNumber = [driverAttrsDictionary objectForKey:kDriverPhoneNumberKey];
    if (![self isNULL:phoneNumber]) {
        [driverAttributes setPhoneNumber:phoneNumber];
    }

    NSDictionary *customAttributes = [driverAttrsDictionary objectForKey:kCustomAttributesKey];
    if (![self isNULL:customAttributes]) {
        for (NSString *key in customAttributes.allKeys) {
            [driverAttributes setCustomAttribute:customAttributes[key] forKey:key];
        }
    }

    return driverAttributes;
}

- (ZendriveConfiguration *)configurationFromDictionary:(NSDictionary *)configDictionary {
    if ([self isNULL:configDictionary]) {
        return nil;
    }

    ZendriveConfiguration *configuration = [[ZendriveConfiguration alloc] init];
    configuration.operationMode = ZendriveOperationModeDriverAnalytics;

    NSString *applicationKey = [configDictionary objectForKey:kConfigurationApplicationKey];
    if (![self isNULL:applicationKey]) {
        configuration.applicationKey = applicationKey;
    }

    NSString *driverId = [configDictionary objectForKey:kConfigurationDriverIdKey];
    if (![self isNULL:driverId]) {
        configuration.driverId = driverId;
    }

    NSNumber *driveDetectionMode = [configDictionary objectForKey:kConfigurationDriveDetectionModeKey];
    if (![self isNULL:driveDetectionMode]) {
        configuration.driveDetectionMode = driveDetectionMode.unsignedIntValue;
    }

    NSDictionary *driverAttrsDictionary = [configDictionary objectForKey:kDriverAttributesKey];
    ZendriveDriverAttributes *driverAttrs = [self driverAttrsFromDictionary:driverAttrsDictionary];
    if (![self isNULL:driverAttrs]) {
        [configuration setDriverAttributes:driverAttrs];
    }

    return configuration;
}

- (BOOL)isNULL:(NSObject *)object {
    if (!object || (object == [NSNull null])) {
        return YES;
    }
    return NO;
}
@end