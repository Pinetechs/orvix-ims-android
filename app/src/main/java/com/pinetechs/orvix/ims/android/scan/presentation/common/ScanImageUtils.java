package com.pinetechs.orvix.ims.android.scan.presentation.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public final class ScanImageUtils {

    private static final String TAG = "ScanImageUtils";
    private static final int MAX_DIMENSION = 1280;
    private static final int JPEG_QUALITY = 75;

    private ScanImageUtils() {
    }

    /**
     * Normalizes the scanner image to a bounded JPEG suitable for preview and upload.
     */
    public static byte[] toUploadJpeg(byte[] source) {
        if (source == null || source.length == 0) return null;

        Bitmap original = null;
        Bitmap scaled = null;
        try {
            original = BitmapFactory.decodeByteArray(source, 0, source.length);
            if (original == null) return null;

            int width = original.getWidth();
            int height = original.getHeight();
            int largest = Math.max(width, height);

            if (largest > MAX_DIMENSION) {
                float ratio = MAX_DIMENSION / (float) largest;
                int targetWidth = Math.max(1, Math.round(width * ratio));
                int targetHeight = Math.max(1, Math.round(height * ratio));
                scaled = Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true);
            } else {
                scaled = original;
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                return null;
            }
            return output.toByteArray();
        } catch (Exception exception) {
            Log.e(TAG, "Failed to normalize scanner image", exception);
            return null;
        } finally {
            if (scaled != null && scaled != original && !scaled.isRecycled()) {
                scaled.recycle();
            }
            if (original != null && !original.isRecycled()) {
                original.recycle();
            }
        }
    }
}
