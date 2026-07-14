package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.Context;

import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;

/** Factory extension point for Zebra, Honeywell, UROVO, and other scanner SDKs. */
public interface ScannerProvider {

    String getVendorName();

    boolean supports(ScannerDeviceInfo deviceInfo);

    ScannerInterface create(Context context, ScannerProfile profile);
}
