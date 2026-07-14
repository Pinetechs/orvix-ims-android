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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static final EnumMap<BarcodeSymbology, EnumMap<ScannerOptionKey, String>> OPTION_PROPERTY_NAMES =
            new EnumMap<>(BarcodeSymbology.class);
    private static final Map<String, Integer> PROPERTY_ID_CACHE = new HashMap<>();
    private static final Set<String> MISSING_PROPERTY_IDS = new HashSet<>();

    private UrovoSymbologyAdapter() {
    }

    static {
        bind(BarcodeSymbology.CODE39,
                ScannerOptionKey.MIN_LENGTH, "CODE39_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "CODE39_LENGTH2",
                ScannerOptionKey.CHECK_DIGIT_ENABLED, "CODE39_ENABLE_CHECK",
                ScannerOptionKey.SEND_CHECK_DIGIT, "CODE39_SEND_CHECK",
                ScannerOptionKey.FULL_ASCII, "CODE39_FULL_ASCII"
        );
        bind(BarcodeSymbology.CODE128,
                ScannerOptionKey.MIN_LENGTH, "CODE128_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "CODE128_LENGTH2",
                ScannerOptionKey.ISBT_128_ENABLED, "CODE_ISBT_128",
                ScannerOptionKey.ISBT_TABLE_CHECK, "CODE128_CHECK_ISBT_TABLE",
                ScannerOptionKey.REDUCED_QUIET_ZONE, "CODE128_REDUCED_QUIET_ZONE"
        );
        bind(BarcodeSymbology.DATAMATRIX,
                ScannerOptionKey.MIN_LENGTH, "DATAMATRIX_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "DATAMATRIX_LENGTH2"
        );
        bind(BarcodeSymbology.CODE93,
                ScannerOptionKey.MIN_LENGTH, "CODE93_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "CODE93_LENGTH2"
        );
        bind(BarcodeSymbology.CODE11,
                ScannerOptionKey.MIN_LENGTH, "CODE11_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "CODE11_LENGTH2",
                ScannerOptionKey.CHECK_DIGIT_MODE, "CODE11_SEND_CHECK"
        );
        bind(BarcodeSymbology.DISCRETE25,
                ScannerOptionKey.MIN_LENGTH, "D25_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "D25_LENGTH2"
        );
        bind(BarcodeSymbology.MATRIX25,
                ScannerOptionKey.MIN_LENGTH, "M25_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "M25_LENGTH2",
                ScannerOptionKey.CHECK_DIGIT_ENABLED, "M25_ENABLE_CHECK",
                ScannerOptionKey.SEND_CHECK_DIGIT, "M25_SEND_CHECK"
        );
        bind(BarcodeSymbology.INTERLEAVED25,
                ScannerOptionKey.MIN_LENGTH, "I25_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "I25_LENGTH2",
                ScannerOptionKey.CHECK_DIGIT_ENABLED, "I25_ENABLE_CHECK",
                ScannerOptionKey.SEND_CHECK_DIGIT, "I25_SEND_CHECK",
                ScannerOptionKey.CONVERT_TO_EAN13, "I25_TO_EAN13"
        );
        bind(BarcodeSymbology.CODABAR,
                ScannerOptionKey.MIN_LENGTH, "CODABAR_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "CODABAR_LENGTH2",
                ScannerOptionKey.CHECK_DIGIT_ENABLED, "CODABAR_ENABLE_CHECK",
                ScannerOptionKey.SEND_CHECK_DIGIT, "CODABAR_SEND_CHECK",
                ScannerOptionKey.NOTIS_EDITING, "CODABAR_NOTIS",
                ScannerOptionKey.CLSI_EDITING, "CODABAR_CLSI",
                ScannerOptionKey.SEND_START_STOP, "CODABAR_SEND_START"
        );
        bind(BarcodeSymbology.MSI,
                ScannerOptionKey.MIN_LENGTH, "MSI_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "MSI_LENGTH2",
                ScannerOptionKey.REQUIRE_TWO_CHECK_DIGITS, "MSI_REQUIRE_2_CHECK",
                ScannerOptionKey.SEND_CHECK_DIGIT, "MSI_SEND_CHECK",
                ScannerOptionKey.SECOND_CHECK_MOD11, "MSI_CHECK_2_MOD_11"
        );
        bind(BarcodeSymbology.UPCA,
                ScannerOptionKey.SEND_CHECK_DIGIT, "UPCA_SEND_CHECK",
                ScannerOptionKey.SEND_SYSTEM_DIGIT, "UPCA_SEND_SYS",
                ScannerOptionKey.CONVERT_TO_EAN13, "UPCA_TO_EAN13"
        );
        bind(BarcodeSymbology.UPCE,
                ScannerOptionKey.SEND_CHECK_DIGIT, "UPCE_SEND_CHECK",
                ScannerOptionKey.SEND_SYSTEM_DIGIT, "UPCE_SEND_SYS",
                ScannerOptionKey.CONVERT_TO_UPCA, "UPCE_TO_UPCA"
        );
        bind(BarcodeSymbology.UPCE1,
                ScannerOptionKey.SEND_CHECK_DIGIT, "UPCE1_SEND_CHECK",
                ScannerOptionKey.SEND_SYSTEM_DIGIT, "UPCE1_SEND_SYS",
                ScannerOptionKey.CONVERT_TO_UPCA, "UPCE1_TO_UPCA"
        );
        bind(BarcodeSymbology.EAN13,
                ScannerOptionKey.SEND_CHECK_DIGIT, "EAN13_SEND_CHECK",
                ScannerOptionKey.BOOKLAND_EAN, "EAN13_BOOKLANDEAN"
        );
        bind(BarcodeSymbology.EAN8,
                ScannerOptionKey.SEND_CHECK_DIGIT, "EAN8_SEND_CHECK",
                ScannerOptionKey.CONVERT_TO_EAN13, "EAN8_TO_EAN13"
        );
        bind(BarcodeSymbology.GS1_EXP,
                ScannerOptionKey.MIN_LENGTH, "GS1_EXP_LENGTH1",
                ScannerOptionKey.MAX_LENGTH, "GS1_EXP_LENGTH2"
        );
    }

    /**
     * Resolves a vendor-neutral type against the Symbology enum that is
     * actually installed on the device. UROVO devices expose the platform SDK
     * from /system/framework, which may be older than the compile-time JAR.
     * Referencing optional enum constants directly would therefore cause a
     * NoSuchFieldError before isSymbologySupported() can be called.
     */
    static Symbology toUrovo(BarcodeSymbology symbology) {
        if (symbology == null) return null;

        try {
            return Symbology.valueOf(symbology.getStorageName());
        } catch (IllegalArgumentException | LinkageError ignored) {
            // The runtime UROVO platform does not define this symbology.
            return null;
        }
    }

    static BarcodeSymbology fromUrovo(Symbology symbology) {
        if (symbology == null || "NONE".equals(symbology.name())) return null;
        return BarcodeSymbology.fromStorageName(symbology.name());
    }

    static List<PropertyValue> resolveProperties(
            BarcodeSymbology symbology,
            ScannerSymbologySettings settings
    ) {
        List<PropertyValue> values = new ArrayList<>();
        if (symbology == null || settings == null) return values;

        EnumMap<ScannerOptionKey, String> bindings = OPTION_PROPERTY_NAMES.get(symbology);
        ScannerSymbologyDefinition definition = ScannerSymbologyCatalog.getDefinition(symbology);
        if (bindings == null || definition == null) return values;

        for (ScannerOptionDefinition option : definition.getOptions()) {
            String propertyName = bindings.get(option.getKey());
            Integer propertyId = resolvePropertyId(propertyName);
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
        EnumMap<ScannerOptionKey, String> bindings = OPTION_PROPERTY_NAMES.get(symbology);
        if (bindings == null) return;

        Integer minPropId = resolvePropertyId(bindings.get(ScannerOptionKey.MIN_LENGTH));
        Integer maxPropId = resolvePropertyId(bindings.get(ScannerOptionKey.MAX_LENGTH));

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
        EnumMap<ScannerOptionKey, String> bindings = new EnumMap<>(ScannerOptionKey.class);
        for (int index = 0; index + 1 < values.length; index += 2) {
            bindings.put((ScannerOptionKey) values[index], (String) values[index + 1]);
        }
        OPTION_PROPERTY_NAMES.put(symbology, bindings);
    }

    /**
     * Resolves optional PropertyID fields against the platform SDK installed on
     * the device. This avoids NoSuchFieldError when the compile-time UROVO JAR
     * is newer than /system/framework/com.ubx.platform.jar.
     */
    private static Integer resolvePropertyId(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) return null;

        synchronized (PROPERTY_ID_CACHE) {
            Integer cached = PROPERTY_ID_CACHE.get(fieldName);
            if (cached != null) return cached;
            if (MISSING_PROPERTY_IDS.contains(fieldName)) return null;

            try {
                Field field = PropertyID.class.getField(fieldName);
                int propertyId = field.getInt(null);
                PROPERTY_ID_CACHE.put(fieldName, propertyId);
                return propertyId;
            } catch (ReflectiveOperationException | LinkageError exception) {
                MISSING_PROPERTY_IDS.add(fieldName);
                return null;
            }
        }
    }
}
