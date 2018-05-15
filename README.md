# zendrive-sdk-phonegap-plugin
============================
This is the fork from official plugin for [Zendrive](http://www.zendrive.com) in Apache Cordova/PhoneGap!

[Zendrive](http://www.zendrive.com) is commited to improving driving and transportation for everyone through better data and analytics.

You can integrate zendrive-sdk-phonegap-plugin in your application to get Zendrive's driver centric analytics and insights for your fleet.

## Zendrive plugin performs three key functions.
- Automatically collects data from various sensors on mobile phone while minimizing the battery impact.
- Provides end points to application to manually start/end drives.
- Uploads the data back to Zendrive servers for analysis. 

## How to integrate

### Install cocoapods
```
sudo gem install cocoapods
```

### Integrate zendrive phonegap plugin by running the following cordova command
```
cordova plugin add https://github.com/trackerup/zendrive-sdk-phonegap-plugin
```

### Alternatively if you are using plugman in your application then use the following
#### For Android
```
plugman install --platform android --project ./platforms/android --plugin https://github.com/trackerup/zendrive-sdk-phonegap-plugin
```

#### For iOS
```
plugman install --platform ios --project ./platforms/ios --plugin https://github.com/trackerup/zendrive-sdk-phonegap-plugin
```

### Alternatively if you are using phonegap build then add the following in your config.xml ( plugin only supported for android )
```
<gap:plugin name="com.zendrive.phonegap.sdk" version="2.0.0">;
```

### Install pods and build
Because of a bug in cordova-ios ([CB-12582](https://issues.apache.org/jira/browse/CB-12582)) the cocoa pods libary is not linked properly which is why we manually have to install the pods. cordova-ios@4.5.5 should contain a fix for this.

```
cd platform/ios
pod install
cd ../..
cordova build // This will fail, however it is still necessary.
```

## Enable Zendrive in the app
Refer the [full documentation](http://zendrive-root.bitbucket.org/phonegap/docs/jsdoc-2.0.0/Zendrive.html) for details.

### Initialize the SDK
Typically this is done after the driver logs into the application and you know the identity of the driver.
After this call, Zendrive SDK will start automatic trip detection and collect driver data. You can also record trips manually from your application.

```
var applicationKey = ZENDRIVE_APPLICATION_KEY;
var driverId = <DRIVER_ID>;
var config = new Zendrive.ZendriveConfiguration(applicationKey, driverId);
var driverAttributes = new Zendrive.ZendriveDriverAttributes();
driverAttributes.firstName = "first_name";
driverAttributes.lastName = "last_name";
driverAttributes.email = "e@mail.com";
driverAttributes.group = "group1";
driverAttributes.phoneNumber = "11234567890"
driverAttributes.driverStartDate = 1428953991;
driverAttributes.setCustomAttribute("custom_key", "custom_value");
config.driverAttributes = driverAttributes;
config.driveDetectionMode = Zendrive.ZendriveDriveDetectionMode.ZendriveDriveDetectionModeAutoON;

var processStartOfDrive = function(zendriveDriveStartInfo) {
    alert("processStartOfDrive: " + JSON.stringify(zendriveDriveStartInfo));
};
var processEndOfDrive = function(zendriveDriveInfo) {
    alert("processEndOfDrive: " + JSON.stringify(zendriveDriveInfo));
};
var processLocationDenied = function() {
    alert("Location denied, please enable location services to keep Zendrive working");
};

var zendriveCallback = new Zendrive.ZendriveCallback(processStartOfDrive,
    processEndOfDrive, processLocationDenied);

Zendrive.setup(config, zendriveCallback,
    function() {
        alert("Setup done!");
    },
    function (error) {
        alert("Setup failed: " + error);
    }
);
```

- If you don't have the ZENDRIVE_APPLICATION_KEY, sign up on the Zendrive Developer Portal and get the key.
- The <DRIVER_ID> is an ID for the driver currently using the application. Each driver using the application needs a unique ID

See the [SDK Reference](http://zendrive-root.bitbucket.org/phonegap/docs/jsdoc-2.0.0/Zendrive.html#.setup) for more details about setup.

### Manual Trip Tagging
The Zendrive SDK works in the background and automatically detects trips and tracks driving behaviour. However, some applications already have knowledge of point-to-point trips made by the driver using the application. For example - a taxi metering app. If your application has this knowledge, it can indicate that to the Zendrive SDK explicitly.
```
Zendrive.startDrive(<TRACKING_ID>);  // A non empty <TRACKING_ID> must be specified
Zendrive.stopDrive(<TRACKING_ID>);  // The <TRACKING_ID> should be same as passed in startDrive
```
The <TRACKING_ID> can be used to find Zendrive trips with this ID in the [Zendrive Analytics API](https://developers.zendrive.com/docs/api/). See the [SDK Reference](http://zendrive-root.bitbucket.org/phonegap/docs/jsdoc-2.0.0/Zendrive.html#.startDrive) for more details about these calls.

### Driving Sessions
Some applications want to track multiple point-to-point trips together as a single entity. For example, a car rental app may want to track all trips made by a user between a rental car pickup and dropoff as a single entity. This can be done using sessions in the Zendrive SDK.
Sessions can be used in the Zendrive SDK by making the following calls.
```
Zendrive.startSession(<SESSION_ID>);  // A non empty <SESSION ID> must be specified
Zendrive.stopSession();
```
All trips within a session are tagged with the session id. The session id can then be used to lookup Zendrive trips belonging to this session using the [Zendrive](https://developers.zendrive.com/docs/api/). See the [SDK Reference](http://zendrive-root.bitbucket.org/phonegap/docs/jsdoc-2.0.0/Zendrive.html#.startSession) for more details about these calls.

### Controlling Automatic Drive Detection
The Zendrive SDK works in the background and automatically detects trips and tracks driving behaviour. If needed, an application can change this behaviour. For example, a rideshare app may want to automatically track all drives made by a driver only when the driver is on-duty and not collect any data for off-duty drives.
The application can specify the required behaviour via an additional argument during setup of the Zendrive SDK. ZendriveDriveDetectionModeAutoOFF disables automatic drive detection in the SDK.
```
// ZendriveDriveDetectionMode.ZendriveDriveDetectionModeAutoOFF disables automatic drive detection in the SDK.
config.driveDetectionMode = Zendrive.ZendriveDriveDetectionMode.ZendriveDriveDetectionModeAutoOFF;
```
The application can also temporarily enable Zendrive's auto drive-detection. This can be done by setting the ZendriveDriveDetectionMode.
 To Turn on automatic drive detection in the SDK.
```
Zendrive.setDriveDetectionMode(Zendrive.ZendriveDriveDetectionMode.ZendriveDriveDetectionModeAutoON);
```
 To Turn off automatic drive detection in the SDK.
```
Zendrive.setDriveDetectionMode(Zendrive.ZendriveDriveDetectionMode.ZendriveDriveDetectionModeAutoOFF);
```
### Disable the SDK
To disable the SDK at any point in the application, you can invoke this method. The Zendrive SDK goes completely silent after this call and does not track any driving behaviour again.
```
Zendrive.teardown();
```
The application needs to re-initalize the SDK to start tracking driving behaviour.
