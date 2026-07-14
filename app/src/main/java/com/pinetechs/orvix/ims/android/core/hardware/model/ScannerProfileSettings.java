package com.pinetechs.orvix.ims.android.core.hardware.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** User-configurable settings for a logical scanner profile. */
public class ScannerProfileSettings {

    private LinkedHashMap<String, ScannerSymbologySettings> symbologies = new LinkedHashMap<>();
    private boolean showCapturedImage;
    private int minScanLength = 1;
    private int maxScanLength = 128;

    public ScannerProfileSettings() {
        // Required for Gson.
    }

    public ScannerProfileSettings(
            Map<String, ScannerSymbologySettings> symbologies,
            boolean showCapturedImage,
            int minScanLength,
            int maxScanLength
    ) {
        if (symbologies != null) {
            for (Map.Entry<String, ScannerSymbologySettings> entry : symbologies.entrySet()) {
                this.symbologies.put(
                        entry.getKey(),
                        entry.getValue() != null ? entry.getValue().copy() : new ScannerSymbologySettings()
                );
            }
        }
        this.showCapturedImage = showCapturedImage;
        this.minScanLength = minScanLength;
        this.maxScanLength = maxScanLength;
    }

    public Map<String, ScannerSymbologySettings> getSymbologies() {
        if (symbologies == null) symbologies = new LinkedHashMap<>();
        return symbologies;
    }

    public ScannerSymbologySettings getSymbologySettings(BarcodeSymbology symbology) {
        if (symbology == null) return null;
        return getSymbologies().get(symbology.getStorageName());
    }

    public ScannerSymbologySettings getOrCreateSymbologySettings(BarcodeSymbology symbology) {
        if (symbology == null) return null;
        ScannerSymbologySettings settings = getSymbologySettings(symbology);
        if (settings == null) {
            settings = ScannerProfileDefaults.defaultSymbologySettings(symbology, false);
            getSymbologies().put(symbology.getStorageName(), settings);
        }
        return settings;
    }

    public void setSymbologySettings(
            BarcodeSymbology symbology,
            ScannerSymbologySettings settings
    ) {
        if (symbology == null || settings == null) return;
        getSymbologies().put(symbology.getStorageName(), settings.copy());
    }

    public boolean hasEnabledSymbology() {
        for (ScannerSymbologySettings settings : getSymbologies().values()) {
            if (settings != null && settings.isEnabled()) return true;
        }
        return false;
    }

    public boolean isShowCapturedImage() {
        return showCapturedImage;
    }

    public void setShowCapturedImage(boolean showCapturedImage) {
        this.showCapturedImage = showCapturedImage;
    }

    public int getMinScanLength() {
        return minScanLength;
    }

    public void setMinScanLength(int minScanLength) {
        this.minScanLength = minScanLength;
    }

    public int getMaxScanLength() {
        return maxScanLength;
    }

    public void setMaxScanLength(int maxScanLength) {
        this.maxScanLength = maxScanLength;
    }

    public void setScanLengthRange(int minimumLength, int maximumLength) {
        if (!ScannerProfileDefaults.isValidLengthRange(minimumLength, maximumLength)) {
            throw new IllegalArgumentException(
                    "Invalid scanner length range: " + minimumLength + "-" + maximumLength
            );
        }
        this.minScanLength = minimumLength;
        this.maxScanLength = maximumLength;
    }

    public ScannerProfileSettings mergeWithDefaults(ScannerProfileSettings defaults) {
        ScannerProfileSettings merged = defaults != null ? defaults.copy() : new ScannerProfileSettings();
        merged.showCapturedImage = this.showCapturedImage;
        if (ScannerProfileDefaults.isValidLengthRange(this.minScanLength, this.maxScanLength)) {
            merged.minScanLength = this.minScanLength;
            merged.maxScanLength = this.maxScanLength;
        }

        for (Map.Entry<String, ScannerSymbologySettings> entry : getSymbologies().entrySet()) {
            BarcodeSymbology symbology = BarcodeSymbology.fromStorageName(entry.getKey());
            if (symbology == null || entry.getValue() == null) continue;

            ScannerSymbologySettings target = merged.getOrCreateSymbologySettings(symbology);
            ScannerSymbologySettings source = entry.getValue();
            target.setEnabled(source.isEnabled());
            target.getOptions().putAll(source.getOptions());
        }
        return merged;
    }

    public ScannerProfileSettings copy() {
        return new ScannerProfileSettings(getSymbologies(), showCapturedImage, minScanLength, maxScanLength);
    }
}
