zendrive-sdk-phonegap-plugin
============================
This is the fork from official plugin for <a href="http://www.zendrive.com">Zendrive</a> in Apache Cordova/PhoneGap!

<a href="http://www.zendrive.com">Zendrive</a> is commited to improving driving and transportation for everyone through better data and analytics.
<br/>
You can integrate zendrive-sdk-phonegap-plugin in your application to get Zendrive's driver centric analytics and insights for your fleet.

<h3>Zendrive plugin performs three key functions.</h3>
<ul style="list-style-position: inside">
<li> Automatically collects data from various sensors on mobile phone while minimizing the battery impact.</li>
<li> Provides end points to application to manually start/end drives.</li>
<li> Uploads the data back to Zendrive servers for analysis. </li>
</ul>

<p class="center">
<img src="http://developers.zendrive.com/static/img/dev_intro_1.png" />

<h3>How to integrate</h3>

<h4>Integrate zendrive phonegap plugin by running the following cordova command</h4>
<code>cordova plugin add https://github.com/pulluptech/zendrive-sdk-phonegap-plugin.git</code>


<h4>Alternatively if you are using plugman in your application then use the following</h4>
<h5>For Android</h5>
<code>plugman install --platform android --project ./platforms/android --plugin https://github.com/zendrive/zendrive-sdk-phonegap-plugin.git</code>

<h5>For iOS</h5>
<code>plugman install --platform ios --project ./platforms/ios --plugin https://github.com/zendrive/zendrive-sdk-phonegap-plugin.git</code>


<h4>Alternatively if you are using phonegap build then add the following in your config.xml ( plugin only supported for android )</h4>
<code>&lt;gap:plugin name="com.zendrive.phonegap.sdk" version="2.0.0" /&gt;</code>


<h3>Enable Zendrive in the app</h3>
<p>Refer <a href="http://zendrive-root.bitbucket.org/phonegap/docs/jsdoc-2.0.0/Zendrive.html">Full Documentation</a> for details.</p>
<h4>Initialize the SDK</h4>
<p>Typically this is done after the driver logs into the application and you know the identity of the driver.
After this call, Zendrive SDK will start automatic trip detection and collect driver data. You can also record trips manually from your application.</p>
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

<ul style="list-style-position: inside">
<li>If you don't have the ZENDRIVE_APPLICATION_KEY, sign up on the Zendrive Developer Portal and get the key.</li>
<li>The <DRIVER_ID> is an ID for the driver currently using the application. Each driver using the application needs a unique ID</li>
</ul>
<p>See the <a target="_blank" href="http://zendrive-root.bitbucket.org/phonegap/docs/jsdoc-2.0.0/Zendrive.html#.setup">SDK Reference</a> for more details about setup.</p>

<h4>Manual Trip Tagging</h4>
<p>The Zendrive SDK works in the background and automatically detects trips and tracks driving behaviour. However, some applications already have knowledge of point-to-point trips made by the driver using the application. For example - a taxi metering app. If your application has this knowledge, it can indicate that to the Zendrive SDK explicitly.</p>
```
Zendrive.startDrive(<TRACKING_ID>);  // A non empty <TRACKING_ID> must be specified
Zendrive.stopDrive(<TRACKING_ID>);  // The <TRACKING_ID> should be same as passed in startDrive
```
<p>The &lt;TRACKING_ID&gt; can be used to find Zendrive trips with this ID in the <a href="https://developers.zendrive.com/docs/api/" target="_blank">Zendrive Analytics API</a>. See the <a href="http://zendrive-root.bitbucket.org/phonegap/docs/jsdoc-2.0.0/Zendrive.html#.startDrive" target="_blank">SDK Reference</a> for more details about these calls.</p>

<h4>Driving Sessions</h4>
<p>Some applications want to track multiple point-to-point trips together as a single entity. For example, a car rental app may want to track all trips made by a user between a rental car pickup and dropoff as a single entity. This can be done using sessions in the Zendrive SDK.
Sessions can be used in the Zendrive SDK by making the following calls.</p>
```
Zendrive.startSession(<SESSION_ID>);  // A non empty <SESSION ID> must be specified
Zendrive.stopSession();
```
<p>All trips within a session are tagged with the session id. The session id can then be used to lookup Zendrive trips belonging to this session using the <a href="https://developers.zendrive.com/docs/api/" target="_blank">Zendrive Analytics API</a>. See the <a href="http://zendrive-root.bitbucket.org/phonegap/docs/jsdoc-2.0.0/Zendrive.html#.startSession" target="_blank">SDK Reference</a> for more details about these calls.</p>

<h4>Controlling Automatic Drive Detection</h4>
<p>The Zendrive SDK works in the background and automatically detects trips and tracks driving behaviour. If needed, an application can change this behaviour. For example, a rideshare app may want to automatically track all drives made by a driver only when the driver is on-duty and not collect any data for off-duty drives.</p>
<p>The application can specify the required behaviour via an additional argument during setup of the Zendrive SDK. ZendriveDriveDetectionModeAutoOFF disables automatic drive detection in the SDK.</p>
```
// ZendriveDriveDetectionMode.ZendriveDriveDetectionModeAutoOFF disables automatic drive detection in the SDK.
config.driveDetectionMode = Zendrive.ZendriveDriveDetectionMode.ZendriveDriveDetectionModeAutoOFF;
```
<p>The application can also temporarily enable Zendrive's auto drive-detection. This can be done by setting the ZendriveDriveDetectionMode.</p>
<p> To Turn on automatic drive detection in the SDK.</p>
```
Zendrive.setDriveDetectionMode(Zendrive.ZendriveDriveDetectionMode.ZendriveDriveDetectionModeAutoON);
```
<p> To Turn off automatic drive detection in the SDK.</p>
```
Zendrive.setDriveDetectionMode(Zendrive.ZendriveDriveDetectionMode.ZendriveDriveDetectionModeAutoOFF);
```
<h4>Disable the SDK</h4>
<p>To disable the SDK at any point in the application, you can invoke this method. The Zendrive SDK goes completely silent after this call and does not track any driving behaviour again.</p>
```
Zendrive.teardown();
```
<p>The application needs to re-initalize the SDK to start tracking driving behaviour.</p>

<br/>
<br/>
<br/>
