package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.Context;
import android.os.Build;

public class ScannerFactory {

    public static ScannerInterface getScanner(Context context) {
        String manufacturer = Build.MANUFACTURER.toUpperCase();
        
        // Strategy pattern to return the correct scanner implementation
        if (manufacturer.contains("UROVO")) {
            return new UrovoScannerManager(context);
        }
        
        // Add other manufacturers here (Zebra, Honeywell, etc.)
        // if (manufacturer.contains("ZEBRA")) return new ZebraScannerManager(context);
        
        // Default fallback (can be a generic or null)
        return new UrovoScannerManager(context); 
    }
}
