package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.Context;

/**
 * Interface to support different hardware scanners (Urovo, Zebra, Honeywell, etc.)
 */
public interface ScannerInterface {

    interface OnScanListener {
        /**
         * @param data The scanned barcode string
         * @param type The barcode symbology name (e.g., CODE39, QRCODE)
         */
        void onScanResult(String data, String type);
    }

    void setOnScanListener(OnScanListener listener);

    boolean init();

    void register(Context context);

    void unregister(Context context);

    void close();
}
