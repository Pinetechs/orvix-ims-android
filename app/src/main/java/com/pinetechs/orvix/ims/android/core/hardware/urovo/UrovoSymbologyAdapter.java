package com.pinetechs.orvix.ims.android.core.hardware.urovo;

import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Symbology;

import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerOptionDefinition;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerOptionKey;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerOptionType;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologyCatalog;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologyDefinition;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologySettings;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * UROVO-specific translation layer. No UROVO classes are referenced by the
 * profile model or settings UI.
 */
final class UrovoSymbologyAdapter {

    static final class PropertyValue {
        private final int propertyId;
        private final int value;
        private final ScannerOptionKey optionKey;

        PropertyValue(int propertyId, int value, ScannerOptionKey optionKey) {
            this.propertyId = propertyId;
            this.value = value;
            this.optionKey = optionKey;
        }

        int getPropertyId() {
            return propertyId;
        }

        int getValue() {
            return value;
        }

        ScannerOptionKey getOptionKey() {
            return optionKey;
        }
    }

    private static final EnumMap<BarcodeSymbology, EnumMap<ScannerOptionKey, Integer>> OPTION_PROPERTIES =
            new EnumMap<>(BarcodeSymbology.class);

    private UrovoSymbologyAdapter() {
    }

    static {
        bind(BarcodeSymbology.CODE39,
                ScannerOptionKey.MIN_LENGTH, PropertyID.CODE39_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.CODE39_LENGTH2,
                ScannerOptionKey.CHECK_DIGIT_ENABLED, PropertyID.CODE39_ENABLE_CHECK,
                ScannerOptionKey.SEND_CHECK_DIGIT, PropertyID.CODE39_SEND_CHECK,
                ScannerOptionKey.FULL_ASCII, PropertyID.CODE39_FULL_ASCII
        );
        bind(BarcodeSymbology.CODE128,
                ScannerOptionKey.MIN_LENGTH, PropertyID.CODE128_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.CODE128_LENGTH2,
                ScannerOptionKey.ISBT_128_ENABLED, PropertyID.CODE_ISBT_128,
                ScannerOptionKey.ISBT_TABLE_CHECK, PropertyID.CODE128_CHECK_ISBT_TABLE,
                ScannerOptionKey.REDUCED_QUIET_ZONE, PropertyID.CODE128_REDUCED_QUIET_ZONE
        );
        bind(BarcodeSymbology.DATAMATRIX,
                ScannerOptionKey.MIN_LENGTH, PropertyID.DATAMATRIX_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.DATAMATRIX_LENGTH2
        );
        bind(BarcodeSymbology.CODE93,
                ScannerOptionKey.MIN_LENGTH, PropertyID.CODE93_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.CODE93_LENGTH2
        );
        bind(BarcodeSymbology.CODE11,
                ScannerOptionKey.MIN_LENGTH, PropertyID.CODE11_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.CODE11_LENGTH2,
                ScannerOptionKey.CHECK_DIGIT_MODE, PropertyID.CODE11_SEND_CHECK
        );
        bind(BarcodeSymbology.DISCRETE25,
                ScannerOptionKey.MIN_LENGTH, PropertyID.D25_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.D25_LENGTH2
        );
        bind(BarcodeSymbology.MATRIX25,
                ScannerOptionKey.MIN_LENGTH, PropertyID.M25_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.M25_LENGTH2,
                ScannerOptionKey.CHECK_DIGIT_ENABLED, PropertyID.M25_ENABLE_CHECK,
                ScannerOptionKey.SEND_CHECK_DIGIT, PropertyID.M25_SEND_CHECK
        );
        bind(BarcodeSymbology.INTERLEAVED25,
                ScannerOptionKey.MIN_LENGTH, PropertyID.I25_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.I25_LENGTH2,
                ScannerOptionKey.CHECK_DIGIT_ENABLED, PropertyID.I25_ENABLE_CHECK,
                ScannerOptionKey.SEND_CHECK_DIGIT, PropertyID.I25_SEND_CHECK,
                ScannerOptionKey.CONVERT_TO_EAN13, PropertyID.I25_TO_EAN13
        );
        bind(BarcodeSymbology.CODABAR,
                ScannerOptionKey.MIN_LENGTH, PropertyID.CODABAR_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.CODABAR_LENGTH2,
                ScannerOptionKey.CHECK_DIGIT_ENABLED, PropertyID.CODABAR_ENABLE_CHECK,
                ScannerOptionKey.SEND_CHECK_DIGIT, PropertyID.CODABAR_SEND_CHECK,
                ScannerOptionKey.NOTIS_EDITING, PropertyID.CODABAR_NOTIS,
                ScannerOptionKey.CLSI_EDITING, PropertyID.CODABAR_CLSI,
                ScannerOptionKey.SEND_START_STOP, PropertyID.CODABAR_SEND_START
        );
        bind(BarcodeSymbology.MSI,
                ScannerOptionKey.MIN_LENGTH, PropertyID.MSI_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.MSI_LENGTH2,
                ScannerOptionKey.REQUIRE_TWO_CHECK_DIGITS, PropertyID.MSI_REQUIRE_2_CHECK,
                ScannerOptionKey.SEND_CHECK_DIGIT, PropertyID.MSI_SEND_CHECK,
                ScannerOptionKey.SECOND_CHECK_MOD11, PropertyID.MSI_CHECK_2_MOD_11
        );
        bind(BarcodeSymbology.UPCA,
                ScannerOptionKey.SEND_CHECK_DIGIT, PropertyID.UPCA_SEND_CHECK,
                ScannerOptionKey.SEND_SYSTEM_DIGIT, PropertyID.UPCA_SEND_SYS,
                ScannerOptionKey.CONVERT_TO_EAN13, PropertyID.UPCA_TO_EAN13
        );
        bind(BarcodeSymbology.UPCE,
                ScannerOptionKey.SEND_CHECK_DIGIT, PropertyID.UPCE_SEND_CHECK,
                ScannerOptionKey.SEND_SYSTEM_DIGIT, PropertyID.UPCE_SEND_SYS,
                ScannerOptionKey.CONVERT_TO_UPCA, PropertyID.UPCE_TO_UPCA
        );
        bind(BarcodeSymbology.UPCE1,
                ScannerOptionKey.SEND_CHECK_DIGIT, PropertyID.UPCE1_SEND_CHECK,
                ScannerOptionKey.SEND_SYSTEM_DIGIT, PropertyID.UPCE1_SEND_SYS,
                ScannerOptionKey.CONVERT_TO_UPCA, PropertyID.UPCE1_TO_UPCA
        );
        bind(BarcodeSymbology.EAN13,
                ScannerOptionKey.SEND_CHECK_DIGIT, PropertyID.EAN13_SEND_CHECK,
                ScannerOptionKey.BOOKLAND_EAN, PropertyID.EAN13_BOOKLANDEAN
        );
        bind(BarcodeSymbology.EAN8,
                ScannerOptionKey.SEND_CHECK_DIGIT, PropertyID.EAN8_SEND_CHECK,
                ScannerOptionKey.CONVERT_TO_EAN13, PropertyID.EAN8_TO_EAN13
        );
        bind(BarcodeSymbology.GS1_EXP,
                ScannerOptionKey.MIN_LENGTH, PropertyID.GS1_EXP_LENGTH1,
                ScannerOptionKey.MAX_LENGTH, PropertyID.GS1_EXP_LENGTH2
        );
    }

    static Symbology toUrovo(BarcodeSymbology symbology) {
        if (symbology == null) return null;
        switch (symbology) {
            case CODE39: return Symbology.CODE39;
            case CODE128: return Symbology.CODE128;
            case QRCODE: return Symbology.QRCODE;
            case DATAMATRIX: return Symbology.DATAMATRIX;
            case CODE93: return Symbology.CODE93;
            case CODE11: return Symbology.CODE11;
            case CODE32: return Symbology.CODE32;
            case TRIOPTIC: return Symbology.TRIOPTIC;
            case DISCRETE25: return Symbology.DISCRETE25;
            case MATRIX25: return Symbology.MATRIX25;
            case INTERLEAVED25: return Symbology.INTERLEAVED25;
            case CHINESE25: return Symbology.CHINESE25;
            case CODABAR: return Symbology.CODABAR;
            case MSI: return Symbology.MSI;
            case UPCA: return Symbology.UPCA;
            case UPCE: return Symbology.UPCE;
            case UPCE1: return Symbology.UPCE1;
            case EAN13: return Symbology.EAN13;
            case EAN8: return Symbology.EAN8;
            case GS1_14: return Symbology.GS1_14;
            case GS1_LIMIT: return Symbology.GS1_LIMIT;
            case GS1_EXP: return Symbology.GS1_EXP;
            case GS1_128: return Symbology.GS1_128;
            case PDF417: return Symbology.PDF417;
            case MICROPDF417: return Symbology.MICROPDF417;
            case MAXICODE: return Symbology.MAXICODE;
            case AZTEC: return Symbology.AZTEC;
            case HANXIN: return Symbology.HANXIN;
            case DOTCODE: return Symbology.DOTCODE;
            case COMPOSITE_CC_AB: return Symbology.COMPOSITE_CC_AB;
            case COMPOSITE_CC_C: return Symbology.COMPOSITE_CC_C;
            case COMPOSITE_TLC39: return Symbology.COMPOSITE_TLC39;
            case POSTAL_PLANET: return Symbology.POSTAL_PLANET;
            case POSTAL_POSTNET: return Symbology.POSTAL_POSTNET;
            case POSTAL_4STATE: return Symbology.POSTAL_4STATE;
            case POSTAL_UPUFICS: return Symbology.POSTAL_UPUFICS;
            case POSTAL_ROYALMAIL: return Symbology.POSTAL_ROYALMAIL;
            case POSTAL_AUSTRALIAN: return Symbology.POSTAL_AUSTRALIAN;
            case POSTAL_KIX: return Symbology.POSTAL_KIX;
            case POSTAL_JAPAN: return Symbology.POSTAL_JAPAN;
            default: return null;
        }
    }

    static BarcodeSymbology fromUrovo(Symbology symbology) {
        if (symbology == null || symbology == Symbology.NONE) return null;
        return BarcodeSymbology.fromStorageName(symbology.name());
    }

    static List<PropertyValue> resolveProperties(
            BarcodeSymbology symbology,
            ScannerSymbologySettings settings
    ) {
        List<PropertyValue> values = new ArrayList<>();
        if (symbology == null || settings == null) return values;

        EnumMap<ScannerOptionKey, Integer> bindings = OPTION_PROPERTIES.get(symbology);
        ScannerSymbologyDefinition definition = ScannerSymbologyCatalog.getDefinition(symbology);
        if (bindings == null || definition == null) return values;

        for (ScannerOptionDefinition option : definition.getOptions()) {
            Integer propertyId = bindings.get(option.getKey());
            if (propertyId == null) continue;

            // Skip length options as they are now global per profile
            if (option.getKey() == ScannerOptionKey.MIN_LENGTH || option.getKey() == ScannerOptionKey.MAX_LENGTH) {
                continue;
            }

            String rawValue = settings.getOption(option.getKey(), option.getDefaultValue());
            int value;
            if (option.getType() == ScannerOptionType.BOOLEAN) {
                value = Boolean.parseBoolean(rawValue) ? 1 : 0;
            } else {
                try {
                    value = Integer.parseInt(rawValue);
                } catch (NumberFormatException ignored) {
                    value = Integer.parseInt(option.getDefaultValue());
                }
            }
            values.add(new PropertyValue(propertyId, value, option.getKey()));
        }
        return values;
    }

    static void appendLengthProperties(
            BarcodeSymbology symbology,
            int minLength,
            int maxLength,
            List<PropertyValue> properties
    ) {
        EnumMap<ScannerOptionKey, Integer> bindings = OPTION_PROPERTIES.get(symbology);
        if (bindings == null) return;

        Integer minPropId = bindings.get(ScannerOptionKey.MIN_LENGTH);
        Integer maxPropId = bindings.get(ScannerOptionKey.MAX_LENGTH);

        if (minPropId != null) {
            properties.removeIf(p -> p.getOptionKey() == ScannerOptionKey.MIN_LENGTH);
            properties.add(new PropertyValue(minPropId, minLength, ScannerOptionKey.MIN_LENGTH));
        }

        if (maxPropId != null) {
            properties.removeIf(p -> p.getOptionKey() == ScannerOptionKey.MAX_LENGTH);
            properties.add(new PropertyValue(maxPropId, maxLength, ScannerOptionKey.MAX_LENGTH));
        }
    }

    private static void bind(BarcodeSymbology symbology, Object... values) {
        EnumMap<ScannerOptionKey, Integer> bindings = new EnumMap<>(ScannerOptionKey.class);
        for (int index = 0; index + 1 < values.length; index += 2) {
            bindings.put((ScannerOptionKey) values[index], (Integer) values[index + 1]);
        }
        OPTION_PROPERTIES.put(symbology, bindings);
    }
}
