package com.pinetechs.orvix.ims.android.core.hardware.model;

import com.pinetechs.orvix.ims.android.R;

/**
 * Native UROVO good-read beep setting for Intent/Broadcast output mode.
 */
public enum ScannerBeepMode {

    NONE(0, R.string.none_label, R.string.none_desc),
    NATIVE(1, R.string.native_label, R.string.native_desc);

    private final int urovoValue;
    private final int displayNameRes;
    private final int descriptionRes;

    ScannerBeepMode(int urovoValue, int displayNameRes, int descriptionRes) {
        this.urovoValue = urovoValue;
        this.displayNameRes = displayNameRes;
        this.descriptionRes = descriptionRes;
    }

    public int getUrovoValue() {
        return urovoValue;
    }

    public String getDisplayName(android.content.Context context) {
        return context.getString(displayNameRes);
    }

    public String getDescription(android.content.Context context) {
        return context.getString(descriptionRes);
    }
    
    // Legacy support
    public String getDisplayName() { return name(); }

    public static ScannerBeepMode fromName(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NATIVE;
        }

        String normalized = value.trim().toUpperCase();

        // Migrate values saved by the previous implementation. In Intent mode,
        // both old sound variants resolve to the single native UROVO beep.
        if ("SHORT".equals(normalized) || "SHARP".equals(normalized)) {
            return NATIVE;
        }

        try {
            return ScannerBeepMode.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return NATIVE;
        }
    }
}
