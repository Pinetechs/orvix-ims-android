package com.pinetechs.orvix.ims.android.core.hardware.urovo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.Symbology;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.pinetechs.orvix.ims.android.core.hardware.ScannerInterface;
import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfileSettings;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;

import java.nio.charset.StandardCharsets;

public class UrovoScannerManager implements ScannerInterface {

    private static final String TAG = "UrovoScannerManager";

    private static final String ACTION_CAPTURE_IMAGE_REQUEST = "action.scanner_capture_image";
    private static final String ACTION_CAPTURE_IMAGE_RESULT = "scanner_capture_image_result";
    private static final String CAPTURE_IMAGE_BYTES_TAG = "bitmapBytes";

    private final Context applicationContext;
    private final SessionManager sessionManager;

    private ScanManager scanManager;
    private OnScanListener listener;
    private OnScanImageListener imageListener;
    private ScannerProfile profile;
    private boolean scannerOpen;
    private boolean receiverRegistered;
    private Context activeDecodeContext;

    public UrovoScannerManager(Context context) {
        this(context, ScannerProfile.GENERAL);
    }

    public UrovoScannerManager(Context context, ScannerProfile profile) {
        this.applicationContext = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);
        this.profile = profile != null ? profile : ScannerProfile.GENERAL;
    }

    @Override
    public void setOnScanListener(OnScanListener listener) {
        this.listener = listener;
    }

    @Override
    public void setOnScanImageListener(OnScanImageListener listener) {
        this.imageListener = listener;
    }

    @Override
    public boolean requestLastScanImage() {
        try {
            // UROVO's official sample sends the image request from the decode
            // receiver immediately after receiving the barcode result. Reuse that
            // receiver context when available so the scanner service still owns the
            // decoded frame associated with this barcode.
            Context requestContext = activeDecodeContext != null
                    ? activeDecodeContext
                    : applicationContext;
            requestContext.sendBroadcast(new Intent(ACTION_CAPTURE_IMAGE_REQUEST));
            Log.d(TAG, "Requested scanner capture image");
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "Failed to request the last scan image", exception);
            return false;
        }
    }

    @Override
    public boolean init() {
        try {
            if (scanManager == null) {
                scanManager = new ScanManager();
            }

            scannerOpen = scanManager.getScannerState();
            if (!scannerOpen) {
                scannerOpen = scanManager.openScanner();
            }

            if (!scannerOpen) {
                Log.e(TAG, "UROVO scanner could not be opened");
                return false;
            }

            UrovoScannerConfigurator.applyStandardSettings(scanManager, sessionManager);
            applyProfile(profile);
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "Failed to initialize UROVO scanner", exception);
            scannerOpen = false;
            return false;
        }
    }

    @Override
    public boolean applyProfile(ScannerProfile profile) {
        this.profile = profile != null ? profile : ScannerProfile.GENERAL;

        if (scanManager == null || !scannerOpen) {
            return true;
        }

        try {
            ScannerProfileSettings settings = sessionManager.getScannerProfileSettings(this.profile);
            UrovoScannerConfigurator.applyProfile(scanManager, this.profile, settings);
            Log.d(TAG, "Applied scanner profile: " + this.profile.name());
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "Failed to apply scanner profile: " + this.profile, exception);
            return false;
        }
    }

    @Override
    public ScannerProfile getProfile() {
        return profile;
    }

    @Override
    public void register(Context activityContext) {
        if (activityContext == null) return;

        // Reapply settings whenever the scan screen becomes active. This makes
        // changes saved in ScannerSettingsActivity effective without restarting the app.
        if (!init()) return;
        if (receiverRegistered) return;

        IntentFilter filter = new IntentFilter();
        filter.addAction(sessionManager.getUrovoIntentAction());
        filter.addAction(ACTION_CAPTURE_IMAGE_RESULT);

        // Official UROVO fallback in case a device firmware ignores the custom action.
        if (!ScanManager.ACTION_DECODE.equals(sessionManager.getUrovoIntentAction())) {
            filter.addAction(ScanManager.ACTION_DECODE);
        }

        try {
            // UROVO scan data is sent by a system scanner service outside this app.
            ContextCompat.registerReceiver(
                    activityContext,
                    scanReceiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
            );
            receiverRegistered = true;
        } catch (Exception exception) {
            Log.e(TAG, "Failed to register scanner receiver", exception);
        }
    }

    @Override
    public void unregister(Context activityContext) {
        if (activityContext == null || !receiverRegistered) return;

        try {
            activityContext.unregisterReceiver(scanReceiver);
        } catch (IllegalArgumentException ignored) {
            // Receiver was already unregistered by the activity lifecycle.
        } finally {
            receiverRegistered = false;
        }
    }

    @Override
    public void close() {
        if (scanManager == null) return;

        try {
            scanManager.stopDecode();
            if (scannerOpen || scanManager.getScannerState()) {
                scanManager.closeScanner();
            }
        } catch (Exception exception) {
            Log.w(TAG, "Failed to close UROVO scanner cleanly", exception);
        } finally {
            scannerOpen = false;
            scanManager = null;
        }
    }

    private final BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;

            if (ACTION_CAPTURE_IMAGE_RESULT.equals(intent.getAction())) {
                byte[] imageData = intent.getByteArrayExtra(CAPTURE_IMAGE_BYTES_TAG);
                if (imageData == null || imageData.length == 0) {
                    Log.w(TAG, "Scanner returned an empty capture image");
                    return;
                }

                Log.d(TAG, "Received scanner capture image: " + imageData.length + " bytes");
                OnScanImageListener currentImageListener = imageListener;
                if (currentImageListener != null) {
                    currentImageListener.onScanImageCaptured(imageData);
                } else {
                    Log.w(TAG, "Capture image received without an image listener");
                }
                return;
            }

            String configuredAction = sessionManager.getUrovoIntentAction();
            boolean supportedAction = configuredAction.equals(intent.getAction())
                    || ScanManager.ACTION_DECODE.equals(intent.getAction());
            if (!supportedAction) return;

            String barcode = extractBarcode(intent);
            if (barcode == null || barcode.trim().isEmpty()) return;

            String typeName = extractSymbologyName(intent);
            OnScanListener currentListener = listener;
            if (currentListener != null) {
                activeDecodeContext = context;
                try {
                    currentListener.onScanResult(barcode.trim(), typeName);
                } finally {
                    activeDecodeContext = null;
                }
            }
        }
    };

    private String extractBarcode(Intent intent) {
        String barcode = intent.getStringExtra(sessionManager.getUrovoDataTag());
        if (barcode != null) return barcode;

        // Official SDK tag fallback.
        barcode = intent.getStringExtra(ScanManager.BARCODE_STRING_TAG);
        if (barcode != null) return barcode;

        byte[] rawData = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
        if (rawData == null || rawData.length == 0) return null;

        int declaredLength = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, rawData.length);
        int safeLength = Math.max(0, Math.min(declaredLength, rawData.length));
        return new String(rawData, 0, safeLength, StandardCharsets.UTF_8);
    }

    private String extractSymbologyName(Intent intent) {
        String typeTag = sessionManager.getUrovoTypeTag();
        int typeId = intent.getByteExtra(typeTag, (byte) 0) & 0xFF;

        if (typeId == 0 && !ScanManager.BARCODE_TYPE_TAG.equals(typeTag)) {
            typeId = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0) & 0xFF;
        }

        Symbology symbology = Symbology.fromInt(typeId);
        BarcodeSymbology genericType = UrovoSymbologyAdapter.fromUrovo(symbology);
        return genericType != null ? genericType.getStorageName() : "UNKNOWN";
    }
}
