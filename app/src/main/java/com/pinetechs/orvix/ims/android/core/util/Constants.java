package com.pinetechs.orvix.ims.android.core.util;

public final class Constants {

    private Constants() {
    }

    // Emulator uses 10.0.2.2 to access your computer localhost.
    // For real device/UROVO, replace with your PC/server IP, e.g. http://192.168.1.50:8080/
    public static final String BASE_URL = "http://10.0.2.2:8080/";

    public static final String PREF_NAME = "orvix_ims_session";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USER_TYPE = "user_type";
}
