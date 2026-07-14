package com.pinetechs.orvix.ims.android.core.hardware.model;

/**
 * Generic symbology options. A device adapter may apply the options it supports
 * and safely ignore the rest.
 */
public enum ScannerOptionKey {
    MIN_LENGTH,
    MAX_LENGTH,
    CHECK_DIGIT_ENABLED,
    SEND_CHECK_DIGIT,
    FULL_ASCII,
    SEND_START_STOP,
    NOTIS_EDITING,
    CLSI_EDITING,
    ISBT_128_ENABLED,
    ISBT_TABLE_CHECK,
    REDUCED_QUIET_ZONE,
    CONVERT_TO_EAN13,
    CONVERT_TO_UPCA,
    SEND_SYSTEM_DIGIT,
    BOOKLAND_EAN,
    REQUIRE_TWO_CHECK_DIGITS,
    SECOND_CHECK_MOD11,
    CHECK_DIGIT_MODE
}
