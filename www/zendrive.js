/*global cordova, module*/
var exec = require('cordova/exec');

/**
 * Zendrive global namespace
 * @namespace
 */
var Zendrive = {};

/**
 * @class Callback for Zendrive
 * @param {Zendrive.ZendriveCallback.processStartOfDrive} processStartOfDrive - Called on callback
 * when Zendrive SDK detects a potential start of a drive.
 *
 * @param {Zendrive.ZendriveCallback.processEndOfDrive} processEndOfDrive - Called on the callback
 * when Zendrive SDK detects a drive to have been completed.
 *
 * @param {Zendrive.ZendriveCallback.processLocationDenied} processLocationDenied - This is an iOS
 * specific callback. Called on callback when location services are denied for the SDK.
 * @constructor
 */
Zendrive.ZendriveCallback = function (processStartOfDrive, processEndOfDrive, processLocationDenied) {
    /**
     * Called on callback when Zendrive SDK detects a potential start of a drive.
     * @callback processStartOfDrive
     * @param {Zendrive.ZendriveDriveStartInfo} driveStartInfo Information about start of a drive.
     * @memberOf Zendrive.ZendriveCallback
     */
    this.processStartOfDrive = processStartOfDrive;

    /**
     * Called on the callback when Zendrive SDK detects a drive to have been completed.
     * @callback processEndOfDrive
     * @param {Zendrive.ZendriveDriveInfo} driveInfo Meta-information related to a drive.
     * @memberOf Zendrive.ZendriveCallback
     */
    this.processEndOfDrive = processEndOfDrive;

    /**
     * This is an iOS specific callback. Called on callback when location services are denied for the SDK.
     * After this callback, drive detection is paused until location services are re-enabled for the SDK.
     * @callback processLocationDenied
     * @memberOf Zendrive.ZendriveCallback
     */
    this.processLocationDenied = processLocationDenied;
};

Zendrive.registerForDelegateCallbacks = function (zendriveCallback) {
    var callbackNotNull = (null != zendriveCallback);

    // We are allowing to clear out existing individual callbacks by sending null
    // for callback and the bool as false for first argument
    var processStartOfDriveCallback = callbackNotNull ? zendriveCallback.processStartOfDrive : null;
    exec(processStartOfDriveCallback, null, "Zendrive", "setProcessStartOfDriveDelegateCallback",
        [(null != processStartOfDriveCallback), processStartOfDriveCallback]);

    var processEndOfDriveCallback = callbackNotNull ? zendriveCallback.processEndOfDrive : null;
    exec(processEndOfDriveCallback, null, "Zendrive", "setProcessEndOfDriveDelegateCallback",
        [(null != processEndOfDriveCallback)]);

    var processLocationDeniedCallback = callbackNotNull ? zendriveCallback.processLocationDenied : null;
    exec(processLocationDeniedCallback, null, "Zendrive", "setProcessLocationDeniedDelegateCallback",
        [(null != processLocationDeniedCallback)]);
};

/**
 * Initializes the Zendrive library to automatically detect driving and collect data.
 * Client code should call this method before anything else in the Zendrive API.
 *
 * @param  {Zendrive.ZendriveConfiguration} zendriveConfiguration - The configuration object used to setup
 * the SDK. This object contains your credentials along with additional setup parameters that you
 * can use to provide meta-information about the user or to tune the sdk functionality.
 * @param  {Zendrive.ZendriveCallback} zendriveCallback - Callback for Zendrive events
 * @param  {Zendrive.setupSuccessCallback} successCallback - This is called when Zendrive SDK setup succeeds
 * @param  {Zendrive.failureCallback} errorCallback - This is called if Zendrive SDK setup fails for some reason
 */
Zendrive.setup = function (zendriveConfiguration, zendriveCallback, successCallback, errorCallback) {
    Zendrive.callback = zendriveCallback;
    Zendrive.registerForDelegateCallbacks(zendriveCallback);

    /**
     * Setup success callback
     * @callback setupSuccessCallback
     * @memberOf Zendrive
     */
    var setupSuccessCallback = successCallback;

    /**
     * Failure callback
     * @callback failureCallback
     * @param {String} error This will have an error message
     * @memberOf Zendrive
     */
    var setupFailureCallback = errorCallback;

    exec(setupSuccessCallback, setupFailureCallback, "Zendrive", "setup", [zendriveConfiguration]);
};

/**
 * Stops driving data collection. The application can disable the Zendrive SDK by invoking this method.
 */
Zendrive.teardown = function () {
    exec(null, null, "Zendrive", "teardown", []);
};

/**
 * Invoking this method forces the start of a drive. If this API is used then it is application’s
 * responsibility to terminate the drive by invoking @{@link Zendrive.stopDrive} method. If an auto-detected drive is
 * in progress, that drive is stopped and a new drive is started.
 *
 * @param  {String} driveTrackingId - Pass a tracking Id to correlate apps internal data with the
 *  drive data. Cannot be null or empty string. Cannot be longer than 64 characters. Sending null or
 *  empty string as tracking id is a no-op.
 */
Zendrive.startDrive = function (driveTrackingId) {
    exec(null, null, "Zendrive", "startDrive", [driveTrackingId]);
};

/**
 * This method provides information about current active drive.
 * @param  {Zendrive.activeDriveCallback} callback - Callback containing active drive information.
 */
Zendrive.getActiveDriveInfo = function (callback) {
    /**
     * active drive callback
     * @callback activeDriveCallback
     * @param {Zendrive.ZendriveActiveDriveInfo} information about the currently active drive.
     * @memberOf Zendrive
     */
    var activeDriveCallback = callback;
    exec(activeDriveCallback, null, "Zendrive", "getActiveDriveInfo", []);
};

/**
 * This should be called to indicate the end of a drive started by invoking @{@link Zendrive.startDrive}
 *
 * @param  {String} driveTrackingId - This trackingId should match the trackingId sent to
 * @{@link Zendrive.startDrive} while starting the current drive. If the trackingIds do not match, this
 * function is a no-op. Cannot be null or empty string.
 */
Zendrive.stopDrive = function (driveTrackingId) {
    exec(null, null, "Zendrive", "stopDrive", [driveTrackingId]);
};

/**
 * All drives, either automatically detected or started using @{@link Zendrive.startDrive}, will be tagged with the
 * sessionId if a session is already in progress. If a drive is already on when this call is made,
 * that drive will not belong to this session.
 *
 * This session id will be made available as a query parameter in the reports and API that Zendrive provides.
 *
 * The application must call @{@link Zendrive.stopSession} when it wants to end the session.
 *
 * Only one session may be active at a time. Calling startSession when a session is already active
 *  with a new sessionId will stop the ongoing session and start a new one.
 * @param  {String} sessionId - an identifier that identifies this session uniquely. Cannot be null
 * or an empty string. Cannot be longer than 64 characters.
 */
Zendrive.startSession = function (sessionId) {
    exec(null, null, "Zendrive", "startSession", [sessionId]);
};

/**
 * Stop currently ongoing session. No-op if no session is ongoing. Trips that start after this call
 * do not belong to the session. Ongoing trips at the time of this call will continue to belong to
 * the session that was just stopped.
 */
Zendrive.stopSession = function () {
    exec(null, null, "Zendrive", "stopSession", []);
};

/**
 * Change the drive detection mode to control how Zendrive SDK detects drives.
 * See @{@link Zendrive.ZendriveDriveDetectionMode} for further details. This will override the mode sent
 * with @{@link Zendrive.ZendriveConfiguration} during setup.
 *
 * @param {Zendrive.ZendriveDriveDetectionMode}
 */
Zendrive.setDriveDetectionMode = function (driveDetectionMode) {
    exec(null, null, "Zendrive", "setDriveDetectionMode", [driveDetectionMode]);
};

/**
 * Dictates the functioning of Zendrive’s drive detection
 * @enum {number}
 */
Zendrive.ZendriveDriveDetectionMode = {
    /**
     * Zendrive SDK will automatically track drives in background in this mode once the SDK is setup.
     *  At the same time, the application can invoke @{@link Zendrive.startDrive} to explicitly start
     *  recording a drive. This is the Default mode.
     * @type {Number}
     */
    ZendriveDriveDetectionModeAutoON: 0,

    /**
     * In this mode auto drive-detection is disabled. All other APIs on Zendrive can be invoked
     * independent of this mode. For recording trips in this mode, the application has to explicitly
     *  invoke the @{@link Zendrive.startDrive} method.
     * @type {Number}
     */
    ZendriveDriveDetectionModeAutoOFF: 1
};

/**
 * @class Additional attributes of a Zendrive driver.
 * The application can specify both predefined and custom attributes for a driver.
 * These attributes are associated with a SDK driverId at SDK initialization time.
 * In addition to predefined special attributes, up to 4 custom key value attributes can be associated
 * with a driver using the Zendrive SDK.
 * @constructor
 */
Zendrive.ZendriveDriverAttributes = function () {
    /**
     * First name of the user.
     * @type {String}
     */
    this.firstName = null;

    /**
     * Last name of the user.
     * @type {String}
     */
    this.lastName = null;

    /**
     * Email of the user.
     * @type {String}
     */
    this.email = null;

    /**
     * A unique id that associates the current user to a group. This groupId will be made available
     * as a query parameter to filter users in the reports and API that Zendrive provides.
     * @type {String}
     */
    this.group = null;

    /**
     * Phone number of the user without '+' character.
     * @type {String}
     */
    this.phoneNumber = null;

    /**
     * The date in seconds at which the driver signed up/started using your application.
     * The date would be available in YYYY-MM-DD format on Zendrvie dashboard
     * @type {number}
     */
    this.driverStartDate = null;

    /**
     * Don't modify this directly, use setCustomAttribute(key, value)
     * @type {Object}
     */
    this.customAttributes = {};

    /**
     * Set the custom attribute of the user, maximum 4 custom attributes are allowed.
     * @param {String} key - A key for the custom attribute. The maximum key length is 64 characters.
     * @param {String} value - Value of the custom attribute. The maximum value length is 64 characters.
     */
    this.setCustomAttribute = function (key, value) {
        // TODO: Add success and error callbacks in case tried to set more than the
        // allowed number of attributes.
        this.customAttributes[key] = value;
    }
};

/**
 * @class This class contains parameters required by @{@link Zendrive} during @{@link Zendrive.setup}.
 * @constructor
 * @param {String} applicationKey - Your application key
 * @param {String} driverId - Unique ID for the current user. This can be any ID used by your app to
 * identify its users. This is the ID which will be used in Zendrive reports.
 */
Zendrive.ZendriveConfiguration = function (applicationKey, driverId) {
    /**
     * Your application key
     * @type {String}
     */
    this.applicationKey = applicationKey;

    /**
     * Unique ID for the current user. This can be any ID used by your app to
     * identify its users. This is the ID which will be used in Zendrive reports.
     * @type {String}
     */
    this.driverId = driverId;

    /**
     * Attributes for the current user. These attributes are stored on the server and are provided
     * in Zendrive’s APIs. Any existing attributes would be overwritten on the server when a
     * non-null value for this param is passed. Passing null is a no-op.
     * @type {Zendrive.ZendriveDriverAttributes}
     */
    this.driverAttributes = null;

    /**
     * Use this mode to control the SDK’s behaviour for detecting drives automatically.
     * This mode can be changed at a later point using @{@link Zendrive.setDriveDetectionMode} method.
     * @type {Zendrive.ZendriveDriveDetectionMode}
     */
    this.driveDetectionMode = Zendrive.ZendriveDriveDetectionMode.ZendriveDriveDetectionModeAutoON;
};

/**
 * @class Wrapper for meta-information related to a drive.
 * @constructor
 */
Zendrive.ZendriveDriveInfo = function () {
    /**
     * The average speed of trip in metres/second
     * @type {Number}
     */
    this.averageSpeed = 0.0;

    /**
     * The distance of the trip in metres
     * @type {Number}
     */
    this.distance = 0.0;

    /**
     * The end timestamp of trip in milliseconds since epoch
     * @type {Number}
     */
    this.endTimestamp = 0;

    /**
     * The start timestamp of trip in milliseconds since epoch.
     * @type {Number}
     */
    this.startTimestamp = 0;

    /**
     * Sometimes, the SDK detects that a drive is invalid after it has been started.
     * In these cases, the isValid property will be set to false and values for all other properties
     * in this class have default values.
     * @type {Boolean}
     */
    this.isValid = true;

    /**
     * A list of @{@link Zendrive.ZendriveLocationPoint} objects corresponding to this trip in increasing order of
     * timestamp. The first point corresponds to trip start location and last to trip end location.
     * @type {Array}
     */
    this.waypoints = [];
}

/**
 * @class Wrapper for meta-information related to a active drive.
 * @constructor
 */
Zendrive.ZendriveActiveDriveInfo = function () {

    /**
     * The start timestamp of trip in milliseconds since epoch.
     * @type {Number}
     */
    this.startTimestamp = 0;

    /**
     * tracking Id correlates apps internal data with the drive data.
     * @type {String}
    */
    this.trackingId;

    /**
     * Identifier that identifies this session uniquely.
     * @type {String}
     */
    this.sessionId;
}

/**
 * @class Information about start of a drive.
 * @constructor
 */
Zendrive.ZendriveDriveStartInfo = function () {
    /**
     * The start timestamp of trip in milliseconds since epoch.
     * @type {Number}
     */
    this.startTimestamp = 0;

    /**
     * The start location of the drive.
     *
     * If the drive is automatically detected by Zendrive SDK, then this location
     * is approximate and close to the start location but not exactly the start location.
     * @type {Zendrive.ZendriveLocationPoint}
     */
    this.startLocation = null;
}

/**
 * @class Represents a geographical coordinate along with accuracy and timestamp information.
 * @constructor
 */
Zendrive.ZendriveLocationPoint = function () {
    /**
     * Latitude in degrees
     * @type {Number}
     */
    this.latitude = 0.0;

    /**
     * Longitude in degrees
     * @type {Number}
     */
    this.longitude = 0.0;
}

module.exports = Zendrive;
