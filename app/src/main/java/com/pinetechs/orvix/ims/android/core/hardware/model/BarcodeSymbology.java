package com.pinetechs.orvix.ims.android.core.hardware.model;

import java.util.Locale;

/**
 * Vendor-neutral barcode symbologies supported by Orvix scanner profiles.
 *
 * Device integrations map these values to their own SDK constants. Stable storage
 * names intentionally preserve the original UROVO names for backward compatibility.
 */
public enum BarcodeSymbology {
    CODE39("CODE39", "Code 39", "1D"),
    CODE128("CODE128", "Code 128", "1D"),
    QRCODE("QRCODE", "QR Code", "2D"),
    DATAMATRIX("DATAMATRIX", "Data Matrix", "2D"),

    CODE93("CODE93", "Code 93", "1D"),
    CODE11("CODE11", "Code 11", "1D"),
    CODE32("CODE32", "Code 32", "1D"),
    TRIOPTIC("TRIOPTIC", "Trioptic Code 39", "1D"),
    DISCRETE25("DISCRETE25", "Discrete 2 of 5", "1D"),
    MATRIX25("MATRIX25", "Matrix 2 of 5", "1D"),
    INTERLEAVED25("INTERLEAVED25", "Interleaved 2 of 5", "1D"),
    CHINESE25("CHINESE25", "Chinese 2 of 5", "1D"),
    CODABAR("CODABAR", "Codabar", "1D"),
    MSI("MSI", "MSI", "1D"),

    UPCA("UPCA", "UPC-A", "Retail"),
    UPCE("UPCE", "UPC-E", "Retail"),
    UPCE1("UPCE1", "UPC-E1", "Retail"),
    EAN13("EAN13", "EAN-13", "Retail"),
    EAN8("EAN8", "EAN-8", "Retail"),
    GS1_14("GS1_14", "GS1 DataBar-14", "GS1"),
    GS1_LIMIT("GS1_LIMIT", "GS1 DataBar Limited", "GS1"),
    GS1_EXP("GS1_EXP", "GS1 DataBar Expanded", "GS1"),
    GS1_128("GS1_128", "GS1-128", "GS1"),

    PDF417("PDF417", "PDF417", "2D"),
    MICROPDF417("MICROPDF417", "MicroPDF417", "2D"),
    MAXICODE("MAXICODE", "MaxiCode", "2D"),
    AZTEC("AZTEC", "Aztec", "2D"),
    HANXIN("HANXIN", "Han Xin", "2D"),
    DOTCODE("DOTCODE", "DotCode", "2D"),

    COMPOSITE_CC_AB("COMPOSITE_CC_AB", "Composite CC-A/B", "Composite"),
    COMPOSITE_CC_C("COMPOSITE_CC_C", "Composite CC-C", "Composite"),
    COMPOSITE_TLC39("COMPOSITE_TLC39", "Composite TLC-39", "Composite"),

    POSTAL_PLANET("POSTAL_PLANET", "US Planet", "Postal"),
    POSTAL_POSTNET("POSTAL_POSTNET", "US Postnet", "Postal"),
    POSTAL_4STATE("POSTAL_4STATE", "USPS 4-State", "Postal"),
    POSTAL_UPUFICS("POSTAL_UPUFICS", "UPU FICS", "Postal"),
    POSTAL_ROYALMAIL("POSTAL_ROYALMAIL", "Royal Mail", "Postal"),
    POSTAL_AUSTRALIAN("POSTAL_AUSTRALIAN", "Australian Postal", "Postal"),
    POSTAL_KIX("POSTAL_KIX", "KIX Postal", "Postal"),
    POSTAL_JAPAN("POSTAL_JAPAN", "Japan Postal", "Postal");

    private final String storageName;
    private final String displayName;
    private final String category;

    BarcodeSymbology(String storageName, String displayName, String category) {
        this.storageName = storageName;
        this.displayName = displayName;
        this.category = category;
    }

    public String getStorageName() {
        return storageName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayName(android.content.Context context) {
        // We could map these to string resources if needed, but symbology names 
        // like "QR Code" are generally universal.
        return displayName;
    }

    public String getCategory() {
        return category;
    }

    public static BarcodeSymbology fromStorageName(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase(Locale.ROOT)
                .replace("QR_CODE", "QRCODE")
                .replace("DATA_MATRIX", "DATAMATRIX");
        for (BarcodeSymbology symbology : values()) {
            if (symbology.storageName.equals(normalized) || symbology.name().equals(normalized)) {
                return symbology;
            }
        }
        return null;
    }
}
