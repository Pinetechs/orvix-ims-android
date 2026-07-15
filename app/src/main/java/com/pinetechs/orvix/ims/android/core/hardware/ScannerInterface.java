package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.Context;

import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;

import java.util.Set;

/**
 * Vendor-neutral hardware scanner contract.
 *
 * Implementations translate provider-specific SDK values into Orvix models so
 * presentation and domain code remain independent from UROVO, Zebra, Honeywell,
 * or any future scanner vendor.
 */
public interface ScannerInterface {

    interface OnScanListener {
        /**
         * @param data The decoded barcode value.
         * @param type The vendor-neutral symbology storage name, for example CODE39.
         */
        void onScanResult(String data, String type);
    }

    interface OnScanImageListener {
        /** Called after the scanner service returns the still image for the last decode. */
        void onScanImageCaptured(byte[] imageData);
    }

    void setOnScanListener(OnScanListener listener);

    void setOnScanImageListener(OnScanImageListener listener);

    /** Requests the still image associated with the most recent successful decode. */
    boolean requestLastScanImage();

    /** Prevents another physical trigger from replacing the frame of the active scan. */
    boolean setTriggerEnabled(boolean enabled);

    /** Selects and applies the logical scanner profile. */
    boolean applyProfile(ScannerProfile profile);

    ScannerProfile getProfile();

    /**
     * Temporarily enables every symbology supported by the active scanner and
     * applies permissive decode settings so a sample label can be identified.
     */
    boolean enterSymbologyDetectionMode();

    /** Restores the selected logical profile after a detection session. */
    boolean exitSymbologyDetectionMode();

    boolean isSymbologyDetectionMode();

    /** Returns the symbologies that the current hardware reports as supported. */
    Set<BarcodeSymbology> getSupportedSymbologies();

    boolean init();

    void register(Context context);

    void unregister(Context context);

    void close();
}
