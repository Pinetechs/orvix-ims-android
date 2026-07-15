package com.pinetechs.orvix.ims.android.core.hardware.urovo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.Symbology;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerInterface;
import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfileSettings;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class UrovoScannerManager implements ScannerInterface {

    private static final String TAG = "UrovoScannerManager";

    private static final String ACTION_CAPTURE_IMAGE_REQUEST = "action.scanner_capture_image";
    private static final String ACTION_CAPTURE_IMAGE_RESULT = "scanner_capture_image_result";
    private static final String CAPTURE_IMAGE_BYTES_TAG = "bitmapBytes";

    private final Context applicationContext;
    private final SessionManager sessionManager;
    private final Gson gson = new Gson();

    private ScanManager scanManager;
    private OnScanListener listener;
    private OnScanImageListener imageListener;
    private ScannerProfile profile;
    private boolean scannerOpen;
    private boolean receiverRegistered;
    private boolean symbologyDetectionMode;
    private String appliedConfigurationSignature;
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
    public boolean setTriggerEnabled(boolean enabled) {
        if (scanManager == null || !scannerOpen) return false;
        try {
            if (enabled) scanManager.unlockTrigger();
            else scanManager.lockTrigger();
            return true;
        } catch (RuntimeException exception) {
            Log.e(TAG, "Could not change scanner trigger state", exception);
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

            String configurationSignature = buildConfigurationSignature();
            if (!symbologyDetectionMode
                    && configurationSignature.equals(appliedConfigurationSignature)) {
                return true;
            }

            UrovoScannerConfigurator.applyStandardSettings(scanManager, sessionManager);
            boolean profileApplied = applyProfile(profile);
            if (profileApplied) {
                appliedConfigurationSignature = configurationSignature;
            }
            return profileApplied;
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

        // Keep detection mode permissive until the detection session explicitly exits.
        if (symbologyDetectionMode) {
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
    public boolean enterSymbologyDetectionMode() {
        try {
            if (!init()) return false;
            UrovoScannerConfigurator.applySymbologyDetectionMode(scanManager);
            symbologyDetectionMode = true;
            Log.d(TAG, "Entered symbology detection mode; supported="
                    + getSupportedSymbologies().size());
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "Failed to enter symbology detection mode", exception);
            symbologyDetectionMode = false;
            return false;
        }
    }

    @Override
    public boolean exitSymbologyDetectionMode() {
        if (!symbologyDetectionMode) return true;

        symbologyDetectionMode = false;
        try {
            if (scanManager == null || !scannerOpen) return true;
            ScannerProfileSettings settings = sessionManager.getScannerProfileSettings(profile);
            UrovoScannerConfigurator.applyProfile(scanManager, profile, settings);
            appliedConfigurationSignature = buildConfigurationSignature();
            Log.d(TAG, "Exited detection mode and restored profile: " + profile.name());
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "Failed to restore scanner profile after detection", exception);
            return false;
        }
    }

    @Override
    public boolean isSymbologyDetectionMode() {
        return symbologyDetectionMode;
    }

    @Override
    public Set<BarcodeSymbology> getSupportedSymbologies() {
        if (scanManager == null || !scannerOpen) return Collections.emptySet();

        EnumSet<BarcodeSymbology> supported = EnumSet.noneOf(BarcodeSymbology.class);
        for (BarcodeSymbology genericType : BarcodeSymbology.values()) {
            Symbology urovoType = UrovoSymbologyAdapter.toUrovo(genericType);
            if (urovoType == null) continue;
            try {
                if (scanManager.isSymbologySupported(urovoType)) supported.add(genericType);
            } catch (RuntimeException exception) {
                Log.w(TAG, "Could not query support for " + genericType, exception);
            }
        }
        return Collections.unmodifiableSet(supported);
    }

    @Override
    public void register(Context activityContext) {
        if (activityContext == null) return;

        // init() applies settings only when the scanner opens or their persisted
        // signature changes. Calling register() repeatedly is therefore inexpensive.
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
            symbologyDetectionMode = false;
            scannerOpen = false;
            appliedConfigurationSignature = null;
            scanManager = null;
        }
    }

    private String buildConfigurationSignature() {
        ScannerProfileSettings settings = sessionManager.getScannerProfileSettings(profile);
        return profile.name()
                + '|' + sessionManager.getScannerBeepMode().name()
                + '|' + sessionManager.isScannerVibrationEnabled()
                + '|' + sessionManager.getUrovoIntentAction()
                + '|' + sessionManager.getUrovoDataTag()
                + '|' + sessionManager.getUrovoTypeTag()
                + '|' + gson.toJson(settings);
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

        try {
            Symbology symbology = Symbology.fromInt(typeId);
            BarcodeSymbology genericType = UrovoSymbologyAdapter.fromUrovo(symbology);
            return genericType != null ? genericType.getStorageName() : "UNKNOWN";
        } catch (RuntimeException exception) {
            Log.w(TAG, "Unknown UROVO symbology code: " + typeId, exception);
            return "UNKNOWN";
        }
    }
}
