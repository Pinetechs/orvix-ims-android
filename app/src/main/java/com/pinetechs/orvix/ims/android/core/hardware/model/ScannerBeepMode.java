package com.pinetechs.orvix.ims.android.core.hardware.model;

/**
 * Native UROVO good-read beep setting for Intent/Broadcast output mode.
 *
 * <p>When Orvix uses Intent output, UROVO exposes the beep as an enabled/disabled
 * setting through PropertyID.SEND_GOOD_READ_BEEP_ENABLE. The Short/Sharp values
 * belong to the keyboard-wedge property and do not apply to Intent output.</p>
 */
public enum ScannerBeepMode {

    NONE(0, "None", "No sound after a successful barcode decode"),
    NATIVE(1, "Native", "Use the UROVO scanner's standard confirmation beep");

    private final int urovoValue;
    private final String displayName;
    private final String description;

    ScannerBeepMode(int urovoValue, String displayName, String description) {
        this.urovoValue = urovoValue;
        this.displayName = displayName;
        this.description = description;
    }

    public int getUrovoValue() {
        return urovoValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

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
