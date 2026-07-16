package com.pinetechs.orvix.ims.android.core.hardware.model;

import com.pinetechs.orvix.ims.android.R;

/**
 * Logical scanner profiles used by Orvix IMS.
 *
 * The profile is vendor-neutral. Each scanner implementation translates it to
 * the hardware-specific configuration required by that device.
 */
public enum ScannerProfile {
    VEHICLE(R.string.vehicle_label, R.string.scan_vehicles),
    SPARE_PART(R.string.spare_part_label, R.string.profile_spare_part_desc),
    ASSET(R.string.asset_label, R.string.profile_asset_desc),
    GENERAL(R.string.profile_general, R.string.profile_general_desc);

    private final int displayNameRes;
    private final int descriptionRes;

    ScannerProfile(int displayNameRes, int descriptionRes) {
        this.displayNameRes = displayNameRes;
        this.descriptionRes = descriptionRes;
    }

    public String getDisplayName(android.content.Context context) {
        return context.getString(displayNameRes);
    }

    public String getDescription(android.content.Context context) {
        return context.getString(descriptionRes);
    }

    public String getDisplayName() { return name(); }
    public String getDescription() { return ""; }
}
