package com.pinetechs.orvix.ims.android.recheck.presentation.submit;

import android.app.Activity;

import com.pinetechs.orvix.ims.android.core.hardware.ScannerFactory;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerInterface;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.scan.presentation.common.ScanImageCoordinator;
import com.pinetechs.orvix.ims.android.scan.presentation.common.ScanImageUtils;

import java.util.Locale;

/**
 * Owns the scanner lifecycle for one recheck submission screen.
 */
public final class RecheckScannerSession {

    public interface Callback {
        /**
         * Return a user-facing message when scanning is currently blocked,
         * otherwise return null.
         */
        String scanBlockReason();

        void onScanBlocked(String message);

        void onCaptureStarted();

        void onBarcodeReady(String barcode, String barcodeType);

        void onEvidenceReady(byte[] image, String imageSource);

        void onCaptureError(String message);
    }

    private final Activity activity;
    private final ScannerInterface scanner;
    private final ScanImageCoordinator imageCoordinator;
    private final Callback callback;

    public RecheckScannerSession(
            Activity activity,
            String inventoryDomain,
            boolean imageRequired,
            Callback callback
    ) {
        this.activity = activity;
        this.callback = callback;

        ScannerProfile profile = profileFor(inventoryDomain);
        scanner = ScannerFactory.getScanner(activity, profile);
        boolean showPreview = new SessionManager(activity)
                .getScannerProfileSettings(profile)
                .isShowCapturedImage();

        imageCoordinator = new ScanImageCoordinator(
                scanner,
                imageRequired,
                showPreview,
                new ScanImageCoordinator.Callback() {
                    @Override
                    public void onCapturedImage(byte[] imageData) {
                        activity.runOnUiThread(() -> {
                            byte[] normalized =
                                    ScanImageUtils.toUploadJpeg(imageData);
                            if (normalized != null) {
                                callback.onEvidenceReady(
                                        normalized,
                                        "UROVO_SCANNER_SENSOR"
                                );
                            }
                        });
                    }

                    @Override
                    public void onScanReady(
                            String barcode,
                            String barcodeType,
                            byte[] uploadImage
                    ) {
                        activity.runOnUiThread(() -> {
                            if (uploadImage != null) {
                                callback.onEvidenceReady(
                                        uploadImage,
                                        "UROVO_SCANNER_SENSOR"
                                );
                            }
                            callback.onBarcodeReady(barcode, barcodeType);
                            scanner.setTriggerEnabled(true);
                        });
                    }

                    @Override
                    public void onCaptureFailed(String message) {
                        activity.runOnUiThread(() ->
                                callback.onCaptureError(message));
                    }
                }
        );

        scanner.setOnScanListener((barcode, type) ->
                activity.runOnUiThread(() -> {
                    String blocked = callback.scanBlockReason();
                    if (blocked != null) {
                        callback.onScanBlocked(blocked);
                        return;
                    }
                    if (imageCoordinator.isWaitingForImage()) {
                        return;
                    }
                    imageCoordinator.handleHardwareScan(barcode, type);
                    callback.onCaptureStarted();
                }));
    }

    public void start() {
        scanner.register(activity);
    }

    public void stop() {
        imageCoordinator.cancelPending();
        scanner.unregister(activity);
    }

    public void close() {
        imageCoordinator.cancelPending();
        scanner.close();
    }

    public void cancelPending() {
        imageCoordinator.cancelPending();
    }

    public boolean isWaitingForImage() {
        return imageCoordinator.isWaitingForImage();
    }

    private static ScannerProfile profileFor(String inventoryDomain) {
        String domain = inventoryDomain == null
                ? ""
                : inventoryDomain.trim().toUpperCase(Locale.ROOT);
        return switch (domain) {
            case "VEHICLE" -> ScannerProfile.VEHICLE;
            case "ASSET" -> ScannerProfile.ASSET;
            case "SPARE_PART" -> ScannerProfile.SPARE_PART;
            default -> ScannerProfile.GENERAL;
        };
    }
}
