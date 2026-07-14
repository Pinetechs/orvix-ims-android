package com.pinetechs.orvix.ims.android.core.hardware.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScannerOptionDefinition {

    private final ScannerOptionKey key;
    private final String displayName;
    private final String description;
    private final ScannerOptionType type;
    private final String defaultValue;
    private final Integer minimumValue;
    private final Integer maximumValue;
    private final LinkedHashMap<String, String> choices;

    private ScannerOptionDefinition(
            ScannerOptionKey key,
            String displayName,
            String description,
            ScannerOptionType type,
            String defaultValue,
            Integer minimumValue,
            Integer maximumValue,
            Map<String, String> choices
    ) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.defaultValue = defaultValue;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.choices = new LinkedHashMap<>();
        if (choices != null) this.choices.putAll(choices);
    }

    public static ScannerOptionDefinition booleanOption(
            ScannerOptionKey key,
            String displayName,
            String description,
            boolean defaultValue
    ) {
        return new ScannerOptionDefinition(
                key, displayName, description, ScannerOptionType.BOOLEAN,
                Boolean.toString(defaultValue), null, null, null
        );
    }

    public static ScannerOptionDefinition integerOption(
            ScannerOptionKey key,
            String displayName,
            String description,
            int defaultValue,
            int minimumValue,
            int maximumValue
    ) {
        return new ScannerOptionDefinition(
                key, displayName, description, ScannerOptionType.INTEGER,
                Integer.toString(defaultValue), minimumValue, maximumValue, null
        );
    }

    public static ScannerOptionDefinition choiceOption(
            ScannerOptionKey key,
            String displayName,
            String description,
            String defaultValue,
            Map<String, String> choices
    ) {
        return new ScannerOptionDefinition(
                key, displayName, description, ScannerOptionType.CHOICE,
                defaultValue, null, null, choices
        );
    }

    public ScannerOptionKey getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public ScannerOptionType getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Integer getMinimumValue() {
        return minimumValue;
    }

    public Integer getMaximumValue() {
        return maximumValue;
    }

    public Map<String, String> getChoices() {
        return Collections.unmodifiableMap(choices);
    }
}
