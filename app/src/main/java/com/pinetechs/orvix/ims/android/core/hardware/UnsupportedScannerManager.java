package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.Context;
import android.util.Log;

import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;

import java.util.Collections;
import java.util.Set;

final class UnsupportedScannerManager implements ScannerInterface {

    private static final String TAG = "UnsupportedScanner";
    private ScannerProfile profile;

    UnsupportedScannerManager(ScannerProfile profile) {
        this.profile = profile != null ? profile : ScannerProfile.GENERAL;
    }

    @Override public void setOnScanListener(OnScanListener listener) { }
    @Override public void setOnScanImageListener(OnScanImageListener listener) { }
    @Override public boolean requestLastScanImage() { return false; }

    @Override
    public boolean applyProfile(ScannerProfile profile) {
        this.profile = profile != null ? profile : ScannerProfile.GENERAL;
        return false;
    }

    @Override public ScannerProfile getProfile() { return profile; }
    @Override public boolean enterSymbologyDetectionMode() { return false; }
    @Override public boolean exitSymbologyDetectionMode() { return false; }
    @Override public boolean isSymbologyDetectionMode() { return false; }
    @Override public Set<BarcodeSymbology> getSupportedSymbologies() { return Collections.emptySet(); }

    @Override
    public boolean init() {
        Log.w(TAG, "No scanner provider supports this device");
        return false;
    }

    @Override public void register(Context context) { }
    @Override public void unregister(Context context) { }
    @Override public void close() { }
}
