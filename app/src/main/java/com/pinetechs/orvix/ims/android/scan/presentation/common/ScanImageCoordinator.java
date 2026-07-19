package com.pinetechs.orvix.ims.android.scan.presentation.common;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.pinetechs.orvix.ims.android.core.hardware.ScannerInterface;

/**
 * Associates the still image returned by UROVO with exactly one decoded barcode.
 */
public final class ScanImageCoordinator {

    private static final String TAG = "ScanImageCoordinator";
    private static final long IMAGE_TIMEOUT_MS = 5000L;

    public interface Callback {
        /** Raw image bytes returned by the scanner service, suitable for local preview. */
        void onCapturedImage(byte[] imageData);

        /** uploadImage is normalized JPEG data and is non-null only when required. */
        void onScanReady(String barcode, String barcodeType, byte[] uploadImage);

        void onCaptureFailed(String message);
    }



    private final ScannerInterface scanner;
    private final Callback callback;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean imageRequired;
    private boolean showCapturedImage;
    private PendingScan pendingScan;

    private final Runnable timeoutRunnable = () -> completeWithoutImage(
            "The scanner image was not received. Please scan the barcode again."
    );

    public ScanImageCoordinator(ScannerInterface scanner, boolean imageRequired, boolean showCapturedImage, Callback callback) {
        this.scanner = scanner;
        this.callback = callback;
        updateOptions(imageRequired, showCapturedImage);
        this.scanner.setOnScanImageListener(this::onImageCaptured);
    }

    /** Reloads task/profile settings when the scan Activity resumes. */
    public void updateOptions(boolean imageRequired, boolean showCapturedImage) {
        this.imageRequired = imageRequired;
        this.showCapturedImage = showCapturedImage;
        Log.d(TAG, "Image options updated: required=" + imageRequired
                + ", preview=" + showCapturedImage);
    }

    public boolean handleHardwareScan(String barcode, String barcodeType) {
        if (barcode == null || barcode.trim().isEmpty() || pendingScan != null) {
            return false;
        }

        scanner.setTriggerEnabled(false);

        if (!imageRequired && !showCapturedImage) {
            callback.onScanReady(barcode, barcodeType, null);
            return true;
        }

        pendingScan = new PendingScan(barcode, barcodeType);
        Log.d(TAG, "Waiting for capture image for barcode: " + barcode);
        if (!scanner.requestLastScanImage()) {
            completeWithoutImage("The scanner could not request a barcode image.");
            return false;
        }

        handler.postDelayed(timeoutRunnable, IMAGE_TIMEOUT_MS);
        return true;
    }

    public boolean isWaitingForImage() {
        return pendingScan != null;
    }

    public void cancelPending() {
        handler.removeCallbacks(timeoutRunnable);
        pendingScan = null;
        scanner.setTriggerEnabled(true);
    }

    /** Called only after the server accepted the scan or the submission failed. */
    public void onSubmissionFinished() {
        scanner.setTriggerEnabled(true);
    }

    private void onImageCaptured(byte[] imageData) {
        PendingScan current = pendingScan;
        if (current == null) {
            Log.w(TAG, "Ignoring capture image because no scan is pending");
            return;
        }

        handler.removeCallbacks(timeoutRunnable);
        pendingScan = null;

        if (imageData == null || imageData.length == 0) {
            finishInvalidImage(current);
            return;
        }

        // Preview the exact bytes returned by UROVO, matching the official sample.
        // Upload conversion is intentionally separate so preview does not depend on
        // JPEG normalization succeeding.
        if (showCapturedImage) {
            callback.onCapturedImage(imageData);
        }

        byte[] uploadImage = null;
        if (imageRequired) {
            uploadImage = ScanImageUtils.toUploadJpeg(imageData);
            if (uploadImage == null || uploadImage.length == 0) {
                callback.onCaptureFailed(
                        "The scanner returned an invalid image. Please scan again."
                );
                scanner.setTriggerEnabled(true);
                return;
            }
        }

        callback.onScanReady(current.barcode, current.barcodeType, uploadImage);
    }

    private void finishInvalidImage(PendingScan current) {
        if (imageRequired) {
            callback.onCaptureFailed("The scanner returned an empty image. Please scan again.");
            scanner.setTriggerEnabled(true);
        } else {
            callback.onScanReady(current.barcode, current.barcodeType, null);
        }
    }

    private void completeWithoutImage(String message) {
        PendingScan current = pendingScan;
        if (current == null) return;

        handler.removeCallbacks(timeoutRunnable);
        pendingScan = null;
        Log.w(TAG, message);

        if (imageRequired) {
            callback.onCaptureFailed(message);
            scanner.setTriggerEnabled(true);
        } else {
            // Preview is optional; do not block the actual inventory scan.
            callback.onScanReady(current.barcode, current.barcodeType, null);
        }
    }

    private static final class PendingScan {
        private final String barcode;
        private final String barcodeType;

        private PendingScan(String barcode, String barcodeType) {
            this.barcode = barcode;
            this.barcodeType = barcodeType;
        }
    }
}
