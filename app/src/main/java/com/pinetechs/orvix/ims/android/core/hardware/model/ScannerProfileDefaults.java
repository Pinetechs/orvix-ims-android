package com.pinetechs.orvix.ims.android.core.hardware.model;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Central, vendor-neutral defaults for Orvix scanner profiles.
 *
 * Hardware adapters translate these logical defaults to their SDK-specific
 * properties. Keep vendor-specific choices out of this class.
 */
public final class ScannerProfileDefaults {

    public static final int MIN_SUPPORTED_SCAN_LENGTH = 1;
    public static final int MAX_SUPPORTED_SCAN_LENGTH = 255;

    private static final Set<BarcodeSymbology> DEFAULT_SYMBOLOGIES = EnumSet.of(
            BarcodeSymbology.CODE39,
            BarcodeSymbology.CODE128,
            BarcodeSymbology.QRCODE,
            BarcodeSymbology.DATAMATRIX
    );

    private ScannerProfileDefaults() {
    }

    public static ScannerProfileSettings forProfile(ScannerProfile profile) {
        ScannerProfile resolvedProfile = profile != null ? profile : ScannerProfile.GENERAL;
        ProfilePreset preset = presetFor(resolvedProfile);

        LinkedHashMap<String, ScannerSymbologySettings> values = new LinkedHashMap<>();
        for (ScannerSymbologyDefinition definition : ScannerSymbologyCatalog.getDefinitions()) {
            BarcodeSymbology symbology = definition.getSymbology();
            boolean enabled = preset.enabledSymbologies.contains(symbology);

            ScannerSymbologySettings settings = defaultSymbologySettings(symbology, enabled);
            applySafeSymbologyDefaults(symbology, settings);
            values.put(symbology.getStorageName(), settings);
        }

        return new ScannerProfileSettings(
                values,
                preset.showCapturedImage,
                preset.minimumLength,
                preset.maximumLength
        );
    }

    /**
     * Base defaults for one symbology. Specialized or risky decoder behavior is
     * intentionally disabled unless a profile explicitly overrides it.
     */
    static ScannerSymbologySettings defaultSymbologySettings(
            BarcodeSymbology symbology,
            boolean enabled
    ) {
        ScannerSymbologyDefinition definition = ScannerSymbologyCatalog.getDefinition(symbology);
        Map<String, String> optionValues = new LinkedHashMap<>();

        if (definition != null) {
            for (ScannerOptionDefinition option : definition.getOptions()) {
                // Length is controlled once at profile level and translated by each vendor adapter.
                if (option.getKey() == ScannerOptionKey.MIN_LENGTH
                        || option.getKey() == ScannerOptionKey.MAX_LENGTH) {
                    continue;
                }
                optionValues.put(option.getKey().name(), option.getDefaultValue());
            }
        }

        return new ScannerSymbologySettings(enabled, optionValues);
    }

    public static boolean isDefaultEnabled(
            ScannerProfile profile,
            BarcodeSymbology symbology
    ) {
        if (symbology == null) return false;
        ScannerProfile resolvedProfile = profile != null ? profile : ScannerProfile.GENERAL;
        return presetFor(resolvedProfile).enabledSymbologies.contains(symbology);
    }

    public static boolean isValidLengthRange(int minimumLength, int maximumLength) {
        return minimumLength >= MIN_SUPPORTED_SCAN_LENGTH
                && maximumLength <= MAX_SUPPORTED_SCAN_LENGTH
                && minimumLength <= maximumLength;
    }

    private static ProfilePreset presetFor(ScannerProfile profile) {
        switch (profile) {
            case VEHICLE:
                // VIN is exactly 17 characters. App/backend validation remains the final authority.
                return new ProfilePreset(17, 17, false, DEFAULT_SYMBOLOGIES);

            case SPARE_PART:
                // Manufacturer part numbers vary considerably; keep hardware filtering permissive.
                return new ProfilePreset(1, 64, false, DEFAULT_SYMBOLOGIES);

            case ASSET:
                // Supports short internal tags and longer QR/Data Matrix identifiers.
                return new ProfilePreset(1, 64, false, DEFAULT_SYMBOLOGIES);

            case GENERAL:
            default:
                // Diagnostic/fallback profile; broad but still bounded.
                return new ProfilePreset(1, 128, false, DEFAULT_SYMBOLOGIES);
        }
    }

    private static void applySafeSymbologyDefaults(
            BarcodeSymbology symbology,
            ScannerSymbologySettings settings
    ) {
        if (settings == null || symbology == null) return;

        switch (symbology) {
            case CODE39:
                // Orvix values use standard uppercase Code 39 data. The VIN check digit is
                // part of the VIN itself, not a Code 39 Mod-43 check character.
                settings.setOption(ScannerOptionKey.CHECK_DIGIT_ENABLED, Boolean.FALSE.toString());
                settings.setOption(ScannerOptionKey.SEND_CHECK_DIGIT, Boolean.FALSE.toString());
                settings.setOption(ScannerOptionKey.FULL_ASCII, Boolean.FALSE.toString());
                break;

            case CODE128:
                // ISBT is a blood-product standard and is unrelated to inventory labels.
                // Reduced quiet-zone decoding is left off for stricter, safer decoding;
                // it can be enabled per profile when real labels require it.
                settings.setOption(ScannerOptionKey.ISBT_128_ENABLED, Boolean.FALSE.toString());
                settings.setOption(ScannerOptionKey.ISBT_TABLE_CHECK, Boolean.FALSE.toString());
                settings.setOption(ScannerOptionKey.REDUCED_QUIET_ZONE, Boolean.FALSE.toString());
                break;

            default:
                // Catalog defaults are already appropriate for disabled/non-core symbologies.
                break;
        }

    }

    private static final class ProfilePreset {
        private final int minimumLength;
        private final int maximumLength;
        private final boolean showCapturedImage;
        private final Set<BarcodeSymbology> enabledSymbologies;

        private ProfilePreset(
                int minimumLength,
                int maximumLength,
                boolean showCapturedImage,
                Set<BarcodeSymbology> enabledSymbologies
        ) {
            if (!isValidLengthRange(minimumLength, maximumLength)) {
                throw new IllegalArgumentException(
                        "Invalid scanner default range: " + minimumLength + "-" + maximumLength
                );
            }
            this.minimumLength = minimumLength;
            this.maximumLength = maximumLength;
            this.showCapturedImage = showCapturedImage;
            this.enabledSymbologies = EnumSet.copyOf(enabledSymbologies);
        }
    }
}
