package com.pinetechs.orvix.ims.android.core.hardware.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Settings for one symbology inside one scanner profile. */
public class ScannerSymbologySettings {

    private boolean enabled;
    private LinkedHashMap<String, String> options = new LinkedHashMap<>();

    public ScannerSymbologySettings() {
        // Required for Gson and future persistence migrations.
    }

    public ScannerSymbologySettings(boolean enabled, Map<String, String> options) {
        this.enabled = enabled;
        if (options != null) this.options.putAll(options);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, String> getOptions() {
        if (options == null) options = new LinkedHashMap<>();
        return options;
    }

    public String getOption(ScannerOptionKey key, String defaultValue) {
        if (key == null) return defaultValue;
        String value = getOptions().get(key.name());
        return value != null ? value : defaultValue;
    }

    public void setOption(ScannerOptionKey key, String value) {
        if (key == null) return;
        if (value == null) {
            getOptions().remove(key.name());
        } else {
            getOptions().put(key.name(), value);
        }
    }

    public boolean getBoolean(ScannerOptionKey key, boolean defaultValue) {
        return Boolean.parseBoolean(getOption(key, Boolean.toString(defaultValue)));
    }

    public int getInt(ScannerOptionKey key, int defaultValue) {
        try {
            return Integer.parseInt(getOption(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    public ScannerSymbologySettings copy() {
        return new ScannerSymbologySettings(enabled, getOptions());
    }
}
