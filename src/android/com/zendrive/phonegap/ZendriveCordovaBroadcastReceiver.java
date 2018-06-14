package com.zendrive.phonegap;

import android.content.Context;
import com.zendrive.sdk.AccidentInfo;
import com.zendrive.sdk.AnalyzedDriveInfo;
import com.zendrive.sdk.DriveResumeInfo;
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.EstimatedDriveInfo;
import com.zendrive.sdk.ZendriveBroadcastReceiver;
import com.zendrive.sdk.ZendriveLocationSettingsResult;

public class ZendriveCordovaBroadcastReceiver extends ZendriveBroadcastReceiver {

    @Override
    public void onDriveStart(Context context, DriveStartInfo startInfo) {
        ZendriveManager.getSharedInstance().onDriveStart(startInfo);
    }

    @Override
    public void onDriveResume(Context context, DriveResumeInfo resumeInfo) {
        ZendriveManager.getSharedInstance().onDriveResume(resumeInfo);
    }

    @Override
    public void onDriveEnd(Context context, EstimatedDriveInfo estimatedDriveInfo) {

        ZendriveManager.getSharedInstance().onDriveEnd(estimatedDriveInfo);

    }

    @Override
    public void onDriveAnalyzed(Context context, AnalyzedDriveInfo analyzedDriveInfo) {
        ZendriveManager.getSharedInstance().onDriveAnalyzed(analyzedDriveInfo);

    }

    @Override
    public void onAccident(Context context, AccidentInfo accidentInfo) {
        ZendriveManager.getSharedInstance().onAccident(accidentInfo);
    }

    @Override
    public void onLocationSettingsChange(Context context, ZendriveLocationSettingsResult locationSettingsResult) {
        ZendriveManager.getSharedInstance().onLocationSettingsChange(locationSettingsResult);
    }

    @Override
    public void onLocationPermissionsChange(Context context, boolean granted) {
        ZendriveManager.getSharedInstance().onLocationPermissionsChange(granted);
    }

}
