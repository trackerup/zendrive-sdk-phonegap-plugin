package com.zendrive.phonegap;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import com.zendrive.sdk.AccidentInfo;
import com.zendrive.sdk.ActiveDriveInfo;
import com.zendrive.sdk.AnalyzedDriveInfo;
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.DriveResumeInfo;
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.LocationPointWithTimestamp;
import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveLocationSettingsResult;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yogesh on 7/20/16.
 */

public class ZendriveManager {

    // String Constants
    // ZendriveLocationPoint dictionary keys
    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";

    // ZendriveDriveStartInfo dictionary keys
    private static final String START_TIMESTAMP_KEY = "startTimestamp";
    private static final String START_LOCATION_KEY = "startLocation";

    // ZendriveDriveInfo dictionary keys
    private static final String IS_VALID_KEY = "isValid";
    private static final String END_TIMESTAMP_KEY = "endTimestamp";
    private static final String AVERAGE_SPEED_KEY = "averageSpeed";
    private static final String DISTANCE_KEY = "distance";
    private static final String WAYPOINTS_KEY = "waypoints";
    private static final String TRACKING_ID_KEY = "trackingId";
    private static final String SESSION_ID_KEY = "sessionId";

    private static final String EVENT_LOCATION_PERMISSION_CHANGE = "location_permission_change";
    private static final String EVENT_LOCATION_SETTING_CHANGE = "location_setting_change";

    private final Context context;

    // Callbacks
    private CallbackContext processStartOfDriveCallback;
    private CallbackContext processEndOfDriveCallback;

    private static ZendriveManager sharedInstance;

    public static synchronized ZendriveManager getSharedInstance() {
        if (sharedInstance == null) {
            throw new IllegalStateException("This class has to be initialized first!");
        }
        return sharedInstance;
    }

    public static void init(Context context) {
        if (sharedInstance != null) {
            return;
        }
        sharedInstance = new ZendriveManager(context);
    }

    private ZendriveManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized void teardown(Context context, final CallbackContext callbackContext) {
        Zendrive.teardown(context, zendriveOperationResult -> {
            if (zendriveOperationResult.isSuccess()) {
                callbackContext.success();
            } else {
                callbackContext.error(zendriveOperationResult.getErrorMessage());
            }
        });
        sharedInstance = null;
    }

    public void setProcessStartOfDriveDelegateCallback(JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        if (null != this.processStartOfDriveCallback) {
            /*
             * Delete old callback Sending NO_RESULT doesn't call any js callback method
             * Setting keepCallback to false would make sure that the callback is deleted
             * from memory after this call
             */
            result.setKeepCallback(false);
        }
        Boolean hasCallback = args.getBoolean(0);
        if (hasCallback) {
            this.processStartOfDriveCallback = callbackContext;
        } else {
            this.processStartOfDriveCallback = null;
        }
        callbackContext.sendPluginResult(result);
    }

    public void setProcessEndOfDriveDelegateCallback(JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        if (null != this.processEndOfDriveCallback) {
            /*
             * Delete old callback Sending NO_RESULT doesn't call any js callback method
             * Setting keepCallback to false would make sure that the callback is deleted
             * from memory after this call
             */
            result.setKeepCallback(false);
        }
        Boolean hasCallback = args.getBoolean(0);
        if (hasCallback) {
            this.processEndOfDriveCallback = callbackContext;
        } else {
            this.processEndOfDriveCallback = null;
        }
        callbackContext.sendPluginResult(result);
    }

    public void onDriveStart(DriveStartInfo driveStartInfo) {
        if (processStartOfDriveCallback == null || processStartOfDriveCallback.isFinished()) {
            return;
        }
        try {
            JSONObject driveStartInfoObject = new JSONObject();
            driveStartInfoObject.put(START_TIMESTAMP_KEY, driveStartInfo.startTimeMillis);

            if (null != driveStartInfo.startLocation) {
                JSONObject driveStartLocationObject = new JSONObject();
                driveStartLocationObject.put(LATITUDE_KEY, driveStartInfo.startLocation.latitude);
                driveStartLocationObject.put(LONGITUDE_KEY, driveStartInfo.startLocation.longitude);
                driveStartInfoObject.put(START_LOCATION_KEY, driveStartLocationObject);
            } else {
                driveStartInfoObject.put(START_LOCATION_KEY, JSONObject.NULL);
            }

            PluginResult result = new PluginResult(PluginResult.Status.OK, driveStartInfoObject);
            result.setKeepCallback(true);
            processStartOfDriveCallback.sendPluginResult(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getActiveDriveInfo(Context context, final CallbackContext callbackContext) {
        try {
            JSONObject activeDriveInfoObject = new JSONObject();
            ActiveDriveInfo activeDriveInfo = Zendrive.getActiveDriveInfo(context);
            activeDriveInfoObject.put(START_TIMESTAMP_KEY, activeDriveInfo.startTimeMillis);
            activeDriveInfoObject.put(TRACKING_ID_KEY,
                    (activeDriveInfo.trackingId != null) ? activeDriveInfo.trackingId : JSONObject.NULL);
            activeDriveInfoObject.put(SESSION_ID_KEY,
                    (activeDriveInfo.sessionId != null) ? activeDriveInfo.sessionId : JSONObject.NULL);
            return activeDriveInfoObject;
        } catch (Exception e) {
        }
        return null;
    }

    public void onDriveEnd(DriveInfo driveInfo) {
        if (processEndOfDriveCallback == null || processEndOfDriveCallback.isFinished()) {
            return;
        }
        try {
            JSONObject driveInfoObject = new JSONObject();
            driveInfoObject.put(START_TIMESTAMP_KEY, driveInfo.startTimeMillis);
            driveInfoObject.put(END_TIMESTAMP_KEY, driveInfo.endTimeMillis);

            driveInfoObject.put(AVERAGE_SPEED_KEY, driveInfo.averageSpeed);
            driveInfoObject.put(DISTANCE_KEY, driveInfo.distanceMeters);

            int waypointsCount = 0;
            if (null != driveInfo.waypoints) {
                waypointsCount = driveInfo.waypoints.size();
            }
            JSONArray waypointsArray = new JSONArray();
            for (int i = 0; i < waypointsCount; i++) {
                LocationPointWithTimestamp locationPoint = driveInfo.waypoints.get(i);

                JSONObject driveLocationObject = new JSONObject();
                driveLocationObject.put(LATITUDE_KEY, locationPoint.location.latitude);
                driveLocationObject.put(LONGITUDE_KEY, locationPoint.location.longitude);
                waypointsArray.put(driveLocationObject);
            }
            driveInfoObject.put(WAYPOINTS_KEY, waypointsArray);

            PluginResult result = new PluginResult(PluginResult.Status.OK, driveInfoObject);
            result.setKeepCallback(true);
            processEndOfDriveCallback.sendPluginResult(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onDriveResume(DriveResumeInfo driveResumeInfo) {

    }

    public void onAccident(AccidentInfo accidentInfo) {

    }

    public void onLocationPermissionsChange(boolean granted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            displayOrHideLocationPermissionNotification(granted);
            Intent intent = new Intent(EVENT_LOCATION_PERMISSION_CHANGE);
            intent.putExtra(EVENT_LOCATION_PERMISSION_CHANGE, granted);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);

        } else {
            throw new RuntimeException("Callback on non marshmallow sdk");
        }
    }

    private void displayOrHideLocationPermissionNotification(boolean isLocationPermissionGranted) {
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (isLocationPermissionGranted) {
            // Remove the displayed notification if any
            mNotificationManager.cancel(NotificationUtility.LOCATION_PERMISSION_DENIED_NOTIFICATION_ID);
        } else {
            // Notify user
            Notification notification = NotificationUtility.createLocationPermissionDeniedNotification(context);
            mNotificationManager.notify(NotificationUtility.LOCATION_PERMISSION_DENIED_NOTIFICATION_ID, notification);
        }
    }

    /**
     * Location settings on the device changed.
     */
    public void onLocationSettingsChange(ZendriveLocationSettingsResult settingsResult) {
        displayOrHideLocationSettingNotification(settingsResult);
        Intent intent = new Intent(EVENT_LOCATION_SETTING_CHANGE);
        intent.putExtra(EVENT_LOCATION_SETTING_CHANGE, settingsResult);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    private void displayOrHideLocationSettingNotification(ZendriveLocationSettingsResult settingsResult) {
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (settingsResult.isSuccess()) {
            // Remove the displayed notification if any
            mNotificationManager.cancel(NotificationUtility.LOCATION_DISABLED_NOTIFICATION_ID);
        } else {
            // Notify user
            Notification notification = NotificationUtility.createLocationSettingDisabledNotification(context,
                    settingsResult);
            mNotificationManager.notify(NotificationUtility.LOCATION_DISABLED_NOTIFICATION_ID, notification);
        }
    }

    public void onDriveAnalyzed(AnalyzedDriveInfo analyzedDriveInfo) {

    }
}
