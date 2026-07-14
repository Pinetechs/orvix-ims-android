package com.pinetechs.orvix.ims.android.core.hardware.model;

/**
 * Logical scanner profiles used by Orvix IMS.
 *
 * The profile is vendor-neutral. Each scanner implementation translates it to
 * the hardware-specific configuration required by that device.
 */
public enum ScannerProfile {
    GENERAL("General", "All supported barcode types"),
    VEHICLE("Vehicles", "VIN labels; optimized for 17-character values"),
    SPARE_PART("Spare Parts", "Part numbers and manufacturer labels"),
    ASSET("Assets", "Asset tags and internal labels");

    private final String displayName;
    private final String description;

    ScannerProfile(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
