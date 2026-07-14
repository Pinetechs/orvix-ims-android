package com.pinetechs.orvix.ims.android.core.hardware.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vendor-neutral catalog used by settings UI and scanner adapters.
 *
 * Core defaults are listed first. Additional symbologies are disabled by default
 * but can be enabled per inventory profile.
 */
public final class ScannerSymbologyCatalog {

    private static final List<ScannerSymbologyDefinition> DEFINITIONS;
    private static final EnumMap<BarcodeSymbology, ScannerSymbologyDefinition> BY_TYPE =
            new EnumMap<>(BarcodeSymbology.class);

    private ScannerSymbologyCatalog() {
    }

    static {
        List<ScannerSymbologyDefinition> values = new ArrayList<>();

        values.add(def(BarcodeSymbology.CODE39,
                lengthOptions(),
                bool(ScannerOptionKey.CHECK_DIGIT_ENABLED, "Enable check digit", "Validate the Code 39 check digit.", false),
                bool(ScannerOptionKey.SEND_CHECK_DIGIT, "Send check digit", "Include the check digit in decoded data.", false),
                bool(ScannerOptionKey.FULL_ASCII, "Full ASCII", "Decode Code 39 Full ASCII character pairs.", false)
        ));
        values.add(def(BarcodeSymbology.CODE128,
                lengthOptions(),
                bool(ScannerOptionKey.ISBT_128_ENABLED, "ISBT 128", "Enable ISBT 128 processing.", false),
                bool(ScannerOptionKey.ISBT_TABLE_CHECK, "Check ISBT table", "Validate decoded data using the ISBT table.", false),
                bool(ScannerOptionKey.REDUCED_QUIET_ZONE, "Reduced quiet zone", "Allow Code 128 labels with reduced quiet zones.", false)
        ));
        values.add(def(BarcodeSymbology.QRCODE));
        values.add(def(BarcodeSymbology.DATAMATRIX, lengthOptions()));

        values.add(def(BarcodeSymbology.CODE93, lengthOptions()));
        values.add(def(BarcodeSymbology.CODE11,
                lengthOptions(),
                choice(
                        ScannerOptionKey.CHECK_DIGIT_MODE,
                        "Check digit mode",
                        "Code 11 check digit behavior.",
                        "0",
                        choices(
                                "0", "Two check digits",
                                "1", "One check digit",
                                "2", "Two check digits, strip",
                                "3", "One check digit, strip"
                        )
                )
        ));
        values.add(def(BarcodeSymbology.CODE32));
        values.add(def(BarcodeSymbology.TRIOPTIC));
        values.add(def(BarcodeSymbology.DISCRETE25, lengthOptions()));
        values.add(def(BarcodeSymbology.MATRIX25,
                lengthOptions(),
                bool(ScannerOptionKey.CHECK_DIGIT_ENABLED, "Enable check digit", "Validate the Matrix 2 of 5 check digit.", false),
                bool(ScannerOptionKey.SEND_CHECK_DIGIT, "Send check digit", "Include the check digit in decoded data.", false)
        ));
        values.add(def(BarcodeSymbology.INTERLEAVED25,
                lengthOptions(),
                bool(ScannerOptionKey.CHECK_DIGIT_ENABLED, "Enable check digit", "Validate the Interleaved 2 of 5 check digit.", false),
                bool(ScannerOptionKey.SEND_CHECK_DIGIT, "Send check digit", "Include the check digit in decoded data.", false),
                bool(ScannerOptionKey.CONVERT_TO_EAN13, "Convert to EAN-13", "Convert eligible Interleaved 2 of 5 values to EAN-13.", false)
        ));
        values.add(def(BarcodeSymbology.CHINESE25));
        values.add(def(BarcodeSymbology.CODABAR,
                lengthOptions(),
                bool(ScannerOptionKey.CHECK_DIGIT_ENABLED, "Enable check digit", "Validate the Codabar check digit.", false),
                bool(ScannerOptionKey.SEND_CHECK_DIGIT, "Send check digit", "Include the check digit in decoded data.", false),
                bool(ScannerOptionKey.NOTIS_EDITING, "NOTIS editing", "Apply NOTIS editing rules.", false),
                bool(ScannerOptionKey.CLSI_EDITING, "CLSI editing", "Apply CLSI editing rules.", false),
                bool(ScannerOptionKey.SEND_START_STOP, "Send start/stop", "Include Codabar start and stop characters.", false)
        ));
        values.add(def(BarcodeSymbology.MSI,
                lengthOptions(),
                bool(ScannerOptionKey.REQUIRE_TWO_CHECK_DIGITS, "Require two check digits", "Require two MSI check digits.", false),
                bool(ScannerOptionKey.SEND_CHECK_DIGIT, "Send check digit", "Include MSI check digits in decoded data.", false),
                bool(ScannerOptionKey.SECOND_CHECK_MOD11, "Second check Mod 11", "Use Mod 11 for the second MSI check digit.", false)
        ));

        values.add(def(BarcodeSymbology.UPCA,
                bool(ScannerOptionKey.SEND_CHECK_DIGIT, "Send check digit", "Include the UPC-A check digit.", true),
                bool(ScannerOptionKey.SEND_SYSTEM_DIGIT, "Send system digit", "Include the UPC-A number system digit.", true),
                bool(ScannerOptionKey.CONVERT_TO_EAN13, "Convert to EAN-13", "Convert UPC-A output to EAN-13.", false)
        ));
        values.add(def(BarcodeSymbology.UPCE,
                bool(ScannerOptionKey.SEND_CHECK_DIGIT, "Send check digit", "Include the UPC-E check digit.", true),
                bool(ScannerOptionKey.SEND_SYSTEM_DIGIT, "Send system digit", "Include the UPC-E number system digit.", true),
                bool(ScannerOptionKey.CONVERT_TO_UPCA, "Convert to UPC-A", "Expand UPC-E output to UPC-A.", false)
        ));
        values.add(def(BarcodeSymbology.UPCE1,
                bool(ScannerOptionKey.SEND_CHECK_DIGIT, "Send check digit", "Include the UPC-E1 check digit.", true),
                bool(ScannerOptionKey.SEND_SYSTEM_DIGIT, "Send system digit", "Include the UPC-E1 number system digit.", true),
                bool(ScannerOptionKey.CONVERT_TO_UPCA, "Convert to UPC-A", "Expand UPC-E1 output to UPC-A.", false)
        ));
        values.add(def(BarcodeSymbology.EAN13,
                bool(ScannerOptionKey.SEND_CHECK_DIGIT, "Send check digit", "Include the EAN-13 check digit.", true),
                bool(ScannerOptionKey.BOOKLAND_EAN, "Bookland EAN", "Enable ISBN Bookland processing.", false)
        ));
        values.add(def(BarcodeSymbology.EAN8,
                bool(ScannerOptionKey.SEND_CHECK_DIGIT, "Send check digit", "Include the EAN-8 check digit.", true),
                bool(ScannerOptionKey.CONVERT_TO_EAN13, "Convert to EAN-13", "Expand EAN-8 output to EAN-13.", false)
        ));
        values.add(def(BarcodeSymbology.GS1_14));
        values.add(def(BarcodeSymbology.GS1_LIMIT));
        values.add(def(BarcodeSymbology.GS1_EXP, lengthOptions()));
        values.add(def(BarcodeSymbology.GS1_128));

        values.add(def(BarcodeSymbology.PDF417));
        values.add(def(BarcodeSymbology.MICROPDF417));
        values.add(def(BarcodeSymbology.MAXICODE));
        values.add(def(BarcodeSymbology.AZTEC));
        values.add(def(BarcodeSymbology.HANXIN));
        values.add(def(BarcodeSymbology.DOTCODE));

        values.add(def(BarcodeSymbology.COMPOSITE_CC_AB));
        values.add(def(BarcodeSymbology.COMPOSITE_CC_C));
        values.add(def(BarcodeSymbology.COMPOSITE_TLC39));

        values.add(def(BarcodeSymbology.POSTAL_PLANET));
        values.add(def(BarcodeSymbology.POSTAL_POSTNET));
        values.add(def(BarcodeSymbology.POSTAL_4STATE));
        values.add(def(BarcodeSymbology.POSTAL_UPUFICS));
        values.add(def(BarcodeSymbology.POSTAL_ROYALMAIL));
        values.add(def(BarcodeSymbology.POSTAL_AUSTRALIAN));
        values.add(def(BarcodeSymbology.POSTAL_KIX));
        values.add(def(BarcodeSymbology.POSTAL_JAPAN));

        DEFINITIONS = Collections.unmodifiableList(values);
        for (ScannerSymbologyDefinition definition : DEFINITIONS) {
            BY_TYPE.put(definition.getSymbology(), definition);
        }
    }

    public static List<ScannerSymbologyDefinition> getDefinitions() {
        return DEFINITIONS;
    }

    public static ScannerSymbologyDefinition getDefinition(BarcodeSymbology symbology) {
        return BY_TYPE.get(symbology);
    }

    private static ScannerSymbologyDefinition def(
            BarcodeSymbology symbology,
            ScannerOptionDefinition... options
    ) {
        return new ScannerSymbologyDefinition(symbology, Arrays.asList(options));
    }

    private static ScannerOptionDefinition[] lengthOptions() {
        return new ScannerOptionDefinition[]{
                integer(ScannerOptionKey.MIN_LENGTH, "Minimum length", "Minimum decoded character count.", 1, 1, 255),
                integer(ScannerOptionKey.MAX_LENGTH, "Maximum length", "Maximum decoded character count.", 128, 1, 255)
        };
    }

    private static ScannerSymbologyDefinition def(
            BarcodeSymbology symbology,
            ScannerOptionDefinition[] initial,
            ScannerOptionDefinition... additional
    ) {
        List<ScannerOptionDefinition> options = new ArrayList<>();
        options.addAll(Arrays.asList(initial));
        options.addAll(Arrays.asList(additional));
        return new ScannerSymbologyDefinition(symbology, options);
    }

    private static ScannerOptionDefinition bool(
            ScannerOptionKey key,
            String name,
            String description,
            boolean defaultValue
    ) {
        return ScannerOptionDefinition.booleanOption(key, name, description, defaultValue);
    }

    private static ScannerOptionDefinition integer(
            ScannerOptionKey key,
            String name,
            String description,
            int defaultValue,
            int minimum,
            int maximum
    ) {
        return ScannerOptionDefinition.integerOption(
                key, name, description, defaultValue, minimum, maximum
        );
    }

    private static ScannerOptionDefinition choice(
            ScannerOptionKey key,
            String name,
            String description,
            String defaultValue,
            Map<String, String> choices
    ) {
        return ScannerOptionDefinition.choiceOption(
                key, name, description, defaultValue, choices
        );
    }

    private static Map<String, String> choices(String... values) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (int index = 0; index + 1 < values.length; index += 2) {
            result.put(values[index], values[index + 1]);
        }
        return result;
    }
}
