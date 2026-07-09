package com.pinetechs.orvix.ims.android.core.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.pinetechs.orvix.ims.android.core.storage.SessionManager;

public final class VersionUtils {

    private VersionUtils() {
    }

    public static int getCurrentVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                return (int) packageInfo.getLongVersionCode();
            }
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 1;
        }
    }

    public static boolean isForceUpdateRequired(Context context, SessionManager sessionManager) {
        int currentVersionCode = getCurrentVersionCode(context);
        return sessionManager.isForceUpdate()
                || currentVersionCode < sessionManager.getMinSupportedAndroidVersionCode();
    }

    public static boolean isOptionalUpdateAvailable(Context context, SessionManager sessionManager) {
        int currentVersionCode = getCurrentVersionCode(context);
        return currentVersionCode < sessionManager.getLatestAndroidVersionCode();
    }
}
