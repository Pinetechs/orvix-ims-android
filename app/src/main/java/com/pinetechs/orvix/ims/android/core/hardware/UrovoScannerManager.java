package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Symbology;

import androidx.core.content.ContextCompat;

import com.pinetechs.orvix.ims.android.core.storage.SessionManager;

public class UrovoScannerManager implements ScannerInterface {

    private final Context context;
    private final SessionManager sessionManager;
    private ScanManager scanManager;
    private OnScanListener listener;
    private boolean isScannerOpen = false;

    public UrovoScannerManager(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public void setOnScanListener(OnScanListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean init() {
        try {
            scanManager = new ScanManager();
            isScannerOpen = scanManager.getScannerState();
            if (!isScannerOpen) {
                isScannerOpen = scanManager.openScanner();
            }
            
            if (isScannerOpen) {
                // 1. تفعيل وضع الـ Intent أولاً
                scanManager.switchOutputMode(0); 
                
                // 2. ضبط الإعدادات (بعد الـ Switch لضمان عدم ضياعها)
                int beepVal = sessionManager.isScannerBeepEnabled() ? 1 : 0;
                
                // تفعيل الصوت والاهتزاز معاً لتعزيز تجربة المستخدم
                int[] ids = {
                    PropertyID.GOOD_READ_BEEP_ENABLE, 
                    PropertyID.GOOD_READ_VIBRATE_ENABLE,
                    PropertyID.WEDGE_KEYBOARD_ENABLE
                };
                int[] vals = {
                    beepVal, // Beep
                    beepVal, // Vibrate (نفس حالة الـ Beep حالياً)
                    0        // Disable Keyboard Wedge
                };
                scanManager.setParameterInts(ids, vals);

                // 3. ضبط مسارات الـ Intent المخصصة
                scanManager.setParameterString(
                    new int[]{
                        PropertyID.WEDGE_INTENT_ACTION_NAME, 
                        PropertyID.WEDGE_INTENT_DATA_STRING_TAG,
                        PropertyID.WEDGE_INTENT_LABEL_TYPE_TAG
                    }, 
                    new String[]{
                        sessionManager.getUrovoIntentAction(), 
                        sessionManager.getUrovoDataTag(),
                        sessionManager.getUrovoTypeTag()
                    }
                );
                
                scanManager.unlockTrigger();
            }
            return isScannerOpen;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void register(Context activityContext) {
        if (scanManager != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(sessionManager.getUrovoIntentAction());
            
            // Use ContextCompat to safely register with flags for Android 14+ compatibility
            ContextCompat.registerReceiver(activityContext, scanReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }
    }

    @Override
    public void unregister(Context activityContext) {
        try {
            activityContext.unregisterReceiver(scanReceiver);
        } catch (Exception ignored) {}
    }

    @Override
    public void close() {
        if (scanManager != null && isScannerOpen) {
            scanManager.stopDecode();
            scanManager.closeScanner();
            isScannerOpen = false;
        }
    }

    private final BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = sessionManager.getUrovoIntentAction();
            String dataTag = sessionManager.getUrovoDataTag();
            String typeTag = sessionManager.getUrovoTypeTag();

            if (action.equals(intent.getAction())) {
                String barcode = intent.getStringExtra(dataTag);
                
                // Get barcode type ID and convert to Symbology name
                byte typeId = intent.getByteExtra(typeTag, (byte) 0);
                Symbology symbology = Symbology.fromInt(typeId);
                String typeName = (symbology != null) ? symbology.name() : "UNKNOWN";

                if (barcode != null && listener != null) {
                    listener.onScanResult(barcode.trim(), typeName);
                }
            }
        }
    };
}
