package com.pinetechs.orvix.ims.android.core.hardware;

import android.os.Build;

import java.util.Locale;

public final class ScannerDeviceInfo {

    private final String manufacturer;
    private final String model;

    public ScannerDeviceInfo(String manufacturer, String model) {
        this.manufacturer = normalize(manufacturer);
        this.model = normalize(model);
    }

    public static ScannerDeviceInfo current() {
        return new ScannerDeviceInfo(Build.MANUFACTURER, Build.MODEL);
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public boolean manufacturerContains(String value) {
        return value != null && manufacturer.contains(normalize(value));
    }

    public boolean modelContains(String value) {
        return value != null && model.contains(normalize(value));
    }

    private static String normalize(String value) {
        return value != null ? value.trim().toUpperCase(Locale.ROOT) : "";
    }
}
