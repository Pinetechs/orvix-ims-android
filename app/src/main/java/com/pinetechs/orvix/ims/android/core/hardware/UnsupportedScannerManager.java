package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.Context;
import android.util.Log;

import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;

final class UnsupportedScannerManager implements ScannerInterface {

    private static final String TAG = "UnsupportedScanner";
    private ScannerProfile profile;

    UnsupportedScannerManager(ScannerProfile profile) {
        this.profile = profile != null ? profile : ScannerProfile.GENERAL;
    }

    @Override
    public void setOnScanListener(OnScanListener listener) {
    }

    @Override
    public void setOnScanImageListener(OnScanImageListener listener) {
    }

    @Override
    public boolean requestLastScanImage() {
        return false;
    }

    @Override
    public boolean applyProfile(ScannerProfile profile) {
        this.profile = profile != null ? profile : ScannerProfile.GENERAL;
        return false;
    }

    @Override
    public ScannerProfile getProfile() {
        return profile;
    }

    @Override
    public boolean init() {
        Log.w(TAG, "No scanner provider supports this device");
        return false;
    }

    @Override
    public void register(Context context) {
    }

    @Override
    public void unregister(Context context) {
    }

    @Override
    public void close() {
    }
}
