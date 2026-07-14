package com.pinetechs.orvix.ims.android.core.hardware.urovo;

import android.content.Context;

import com.pinetechs.orvix.ims.android.core.hardware.ScannerDeviceInfo;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerInterface;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerProvider;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;

public final class UrovoScannerProvider implements ScannerProvider {

    @Override
    public String getVendorName() {
        return "UROVO";
    }

    @Override
    public boolean supports(ScannerDeviceInfo deviceInfo) {
        return deviceInfo != null && (
                deviceInfo.manufacturerContains("UROVO")
                        || deviceInfo.modelContains("CT58")
        );
    }

    @Override
    public ScannerInterface create(Context context, ScannerProfile profile) {
        return new UrovoScannerManager(context, profile);
    }
}
