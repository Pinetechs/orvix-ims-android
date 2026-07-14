package com.pinetechs.orvix.ims.android.core.hardware.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Central defaults for scanner profiles. */
public final class ScannerProfileDefaults {

    private ScannerProfileDefaults() {
    }

    public static ScannerProfileSettings forProfile(ScannerProfile profile) {
        ScannerProfile resolvedProfile = profile != null ? profile : ScannerProfile.GENERAL;

        int minimumLength;
        int maximumLength;
        switch (resolvedProfile) {
            case VEHICLE:
                minimumLength = 17;
                maximumLength = 17;
                break;
            case SPARE_PART:
                minimumLength = 4;
                maximumLength = 64;
                break;
            case ASSET:
                minimumLength = 1;
                maximumLength = 64;
                break;
            case GENERAL:
            default:
                minimumLength = 1;
                maximumLength = 128;
                break;
        }

        LinkedHashMap<String, ScannerSymbologySettings> values = new LinkedHashMap<>();
        for (ScannerSymbologyDefinition definition : ScannerSymbologyCatalog.getDefinitions()) {
            BarcodeSymbology symbology = definition.getSymbology();
            boolean enabled = ScannerSymbologyCatalog.isCoreDefault(symbology);
            values.put(
                    symbology.getStorageName(),
                    defaultSymbologySettings(symbology, enabled)
            );
        }

        return new ScannerProfileSettings(values, false, minimumLength, maximumLength);
    }


    /**
     * Temporary permissive settings used only while detecting a barcode type.
     * Every catalog symbology is enabled and length validation is widened to a scanner-safe range.
     * Provider adapters still skip types unsupported by the physical engine.
     */
    public static ScannerProfileSettings forSymbologyDetection() {
        LinkedHashMap<String, ScannerSymbologySettings> values = new LinkedHashMap<>();
        for (ScannerSymbologyDefinition definition : ScannerSymbologyCatalog.getDefinitions()) {
            BarcodeSymbology symbology = definition.getSymbology();
            values.put(
                    symbology.getStorageName(),
                    defaultSymbologySettings(symbology, true)
            );
        }
        return new ScannerProfileSettings(values, false, 1, 128);
    }

    static ScannerSymbologySettings defaultSymbologySettings(
            BarcodeSymbology symbology,
            boolean enabled
    ) {
        ScannerSymbologyDefinition definition = ScannerSymbologyCatalog.getDefinition(symbology);
        Map<String, String> optionValues = new LinkedHashMap<>();

        if (definition != null) {
            for (ScannerOptionDefinition option : definition.getOptions()) {
                // We no longer store individual lengths here as they are unified at profile level
                if (option.getKey() == ScannerOptionKey.MIN_LENGTH || option.getKey() == ScannerOptionKey.MAX_LENGTH) {
                    continue;
                }
                optionValues.put(option.getKey().name(), option.getDefaultValue());
            }
        }

        return new ScannerSymbologySettings(enabled, optionValues);
    }
}
