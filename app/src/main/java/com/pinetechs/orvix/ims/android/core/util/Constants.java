package com.pinetechs.orvix.ims.android.core.util;

public final class Constants {

    private Constants() {
    }

    /**
     * Change this value to the public Bootstrap Service URL when deployed.
     * Emulator localhost uses 10.0.2.2.
     */
    public static final String BOOTSTRAP_BASE_URL = "http://192.168.0.102:8081/";

    public static final String PREF_NAME = "orvix_ims_session";

    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_USER_TYPE = "user_type";
    public static final String KEY_PERMISSIONS = "user_permissions";
    public static final String KEY_COMPANY_NAMES = "company_names";

    public static final String KEY_CLIENT_CODE = "client_code";
    public static final String KEY_CLIENT_NAME = "client_name";
    public static final String KEY_API_BASE_URL = "api_base_url";
    public static final String KEY_LOGO_URL = "logo_url";
    public static final String KEY_MIN_SUPPORTED_ANDROID_VERSION_CODE = "min_supported_android_version_code";
    public static final String KEY_LATEST_ANDROID_VERSION_CODE = "latest_android_version_code";
    public static final String KEY_FORCE_UPDATE = "force_update";
    public static final String KEY_ANDROID_APK_URL = "android_apk_url";
    public static final String KEY_RELEASE_NOTES = "release_notes";
    public static final String KEY_LANGUAGE = "app_language";

    // Scanner Settings
    public static final String KEY_SCANNER_BEEP = "scanner_beep";
    public static final String KEY_SCANNER_VIBRATE = "scanner_vibrate";
    public static final String KEY_UROVO_INTENT_ACTION = "urovo_intent_action";
    public static final String KEY_UROVO_DATA_TAG = "urovo_data_tag";
    public static final String KEY_UROVO_TYPE_TAG = "urovo_type_tag";

    // Defaults
    public static final String DEFAULT_UROVO_ACTION = "android.intent.ACTION_DECODE_DATA";
    public static final String DEFAULT_UROVO_DATA_TAG = "barcode_string";
    public static final String DEFAULT_UROVO_TYPE_TAG = "barcodeType";
}
