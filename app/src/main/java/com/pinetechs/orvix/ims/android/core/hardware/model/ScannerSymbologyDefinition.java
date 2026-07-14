package com.pinetechs.orvix.ims.android.core.hardware.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ScannerSymbologyDefinition {

    private final BarcodeSymbology symbology;
    private final List<ScannerOptionDefinition> options;

    public ScannerSymbologyDefinition(
            BarcodeSymbology symbology,
            List<ScannerOptionDefinition> options
    ) {
        this.symbology = symbology;
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
    }

    public BarcodeSymbology getSymbology() {
        return symbology;
    }

    public List<ScannerOptionDefinition> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public boolean hasOptions() {
        return !options.isEmpty();
    }

    public ScannerOptionDefinition findOption(ScannerOptionKey key) {
        if (key == null) return null;
        for (ScannerOptionDefinition option : options) {
            if (option.getKey() == key) return option;
        }
        return null;
    }
}
