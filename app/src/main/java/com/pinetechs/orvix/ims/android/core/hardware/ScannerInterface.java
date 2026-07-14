package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.Context;

/**
 * Interface to support different hardware scanners (Urovo, Zebra, Honeywell, etc.)
 */
public interface ScannerInterface {

    interface OnScanListener {
        void onScanResult(String data);
    }

    void setOnScanListener(OnScanListener listener);

    boolean init();

    void register(Context context);

    void unregister(Context context);

    void close();
}
