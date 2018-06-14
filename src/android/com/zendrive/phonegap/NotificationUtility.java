package com.zendrive.phonegap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.zendrive.sdk.ZendriveLocationSettingsResult;

/**
 * Utility to create notifications to show to the user when the Zendrive SDK has
 * something interesting to report.
 */
public class NotificationUtility {
    // Notification related constants
    public static final int FOREGROUND_MODE_NOTIFICATION_ID = 98;
    public static final int LOCATION_DISABLED_NOTIFICATION_ID = 99;
    public static final int LOCATION_PERMISSION_DENIED_NOTIFICATION_ID = 100;

    // channel keys (id) are used to sort the channels in the notification
    // settings page. Meaningful ids and descriptions tell the user
    // about which notifications are safe to toggle on/off for the application.
    private static final String FOREGROUND_CHANNEL_KEY = "Foreground";
    private static final String LOCATION_CHANNEL_KEY = "Location";

    /**
     * Create a notification that is displayed when the Zendrive SDK detects a
     * possible drive.
     *
     * @param context App context
     * @return the created notification.
     */
    public static Notification createMaybeInDriveNotification(Context context) {
        createNotificationChannels(context);

        // suppresses deprecated warning for setPriority(PRIORITY_MIN)
        // noinspection deprecation
        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_KEY).setContentTitle("Zendrive")
                .setDefaults(0).setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE).setContentText("Detecting possible drive.")
                .setContentIntent(getNotificationClickIntent(context)).build();
    }

    /**
     * Create a notification that is displayed when the Zendrive SDK determines that
     * the user is driving.
     *
     * @param context App context
     * @return the created notification.
     */
    public static Notification createInDriveNotification(Context context) {
        createNotificationChannels(context);
        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_KEY).setContentTitle("Zendrive")
                .setCategory(NotificationCompat.CATEGORY_SERVICE).setContentText("Drive started.")
                .setContentIntent(getNotificationClickIntent(context)).build();
    }

    private static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            NotificationChannel lowPriorityNotificationChannel = new NotificationChannel(FOREGROUND_CHANNEL_KEY,
                    "Zendrive trip tracking", NotificationManager.IMPORTANCE_MIN);
            lowPriorityNotificationChannel.setShowBadge(false);
            manager.createNotificationChannel(lowPriorityNotificationChannel);

            NotificationChannel defaultNotificationChannel = new NotificationChannel(LOCATION_CHANNEL_KEY, "Problems",
                    NotificationManager.IMPORTANCE_DEFAULT);
            defaultNotificationChannel.setShowBadge(true);
            manager.createNotificationChannel(defaultNotificationChannel);
        }
    }

    private static PendingIntent getNotificationClickIntent(Context context) {
        Intent notificationIntent = new Intent(context.getApplicationContext(),
                ZendriveCordovaPlugin.getCordovaInstance().getActivity().getClass());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent, 0);
    }
}