package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;

public class UrovoScannerManager implements ScannerInterface {

    private final Context context;
    private ScanManager scanManager;
    private OnScanListener listener;
    private boolean isScannerOpen = false;

    private static final String ACTION_DECODE = ScanManager.ACTION_DECODE;
    private static final String BARCODE_STRING_TAG = ScanManager.BARCODE_STRING_TAG;

    public UrovoScannerManager(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void setOnScanListener(OnScanListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean init() {
        try {
            scanManager = new ScanManager();
            isScannerOpen = scanManager.openScanner();
            if (isScannerOpen) {
                // Configure scanner for Intent Mode
                scanManager.switchOutputMode(0); // 0 = Intent Mode
                
                // Ensure the scan trigger is unlocked
                scanManager.unlockTrigger();
                
                // Enable beep on good read
                int[] id = {PropertyID.GOOD_READ_BEEP_ENABLE};
                int[] val = {1};
                scanManager.setParameterInts(id, val);
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
            filter.addAction(ACTION_DECODE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                activityContext.registerReceiver(scanReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                activityContext.registerReceiver(scanReceiver, filter);
            }
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
            if (ACTION_DECODE.equals(intent.getAction())) {
                String barcode = intent.getStringExtra(BARCODE_STRING_TAG);
                if (barcode != null && listener != null) {
                    listener.onScanResult(barcode.trim());
                }
            }
        }
    };
}
