package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.Context;

import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;

public final class ScannerFactory {

    private ScannerFactory() {
    }

    public static ScannerInterface getScanner(Context context) {
        return getScanner(context, ScannerProfile.GENERAL);
    }

    public static ScannerInterface getScanner(Context context, ScannerProfile profile) {
        return ScannerProviderRegistry.create(
                context,
                profile != null ? profile : ScannerProfile.GENERAL,
                ScannerDeviceInfo.current()
        );
    }

    public static String getCurrentScannerVendor() {
        return ScannerProviderRegistry.resolveVendorName(ScannerDeviceInfo.current());
    }
}
