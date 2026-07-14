package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.Context;

import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;

/**
 * Interface to support different hardware scanners (Urovo, Zebra, Honeywell, etc.).
 */
public interface ScannerInterface {

    interface OnScanListener {
        /**
         * @param data The scanned barcode string.
         * @param type The barcode symbology name (for example CODE39 or QRCODE).
         */
        void onScanResult(String data, String type);
    }

    interface OnScanImageListener {
        /**
         * Called after the scanner service returns the still image for the last decode.
         */
        void onScanImageCaptured(byte[] imageData);
    }

    void setOnScanListener(OnScanListener listener);

    void setOnScanImageListener(OnScanImageListener listener);

    /**
     * Requests the still image associated with the most recent successful decode.
     */
    boolean requestLastScanImage();

    /**
     * Selects and applies the logical scanner profile.
     */
    boolean applyProfile(ScannerProfile profile);

    ScannerProfile getProfile();

    boolean init();

    void register(Context context);

    void unregister(Context context);

    void close();
}
