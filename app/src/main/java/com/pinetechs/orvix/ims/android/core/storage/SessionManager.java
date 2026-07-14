package com.pinetechs.orvix.ims.android.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pinetechs.orvix.ims.android.auth.data.dto.AppUserResponse;
import com.pinetechs.orvix.ims.android.bootstrap.data.dto.BootstrapResolveResponse;
import com.pinetechs.orvix.ims.android.core.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveClientConfig(BootstrapResolveResponse response) {
        if (response == null || response.getData() == null) return;
        
        BootstrapResolveResponse.Data data = response.getData();
        BootstrapResolveResponse.Client client = data.getClient();
        BootstrapResolveResponse.Theme theme = data.getTheme();
        BootstrapResolveResponse.Update update = data.getUpdate();

        SharedPreferences.Editor editor = preferences.edit();

        if (client != null) {
            editor.putString(Constants.KEY_CLIENT_CODE, client.getClientCode());
            editor.putString(Constants.KEY_CLIENT_NAME, client.getClientName());
            editor.putString(Constants.KEY_API_BASE_URL, normalizeBaseUrl(client.getApiBaseUrl()));
        }

        if (theme != null) {
            String logoUrl = theme.getLogoUrl();
            if (logoUrl != null && !logoUrl.trim().isEmpty()) {
                if (!logoUrl.startsWith("http")) {
                    logoUrl = Constants.BOOTSTRAP_BASE_URL + (logoUrl.startsWith("/") ? logoUrl.substring(1) : logoUrl);
                }
                editor.putString(Constants.KEY_LOGO_URL, logoUrl);
            }
        }

        if (update != null) {
            editor.putString(Constants.KEY_ANDROID_APK_URL, update.getApkUrl());
            editor.putString(Constants.KEY_RELEASE_NOTES, update.getReleaseNotes());
            editor.putInt(Constants.KEY_MIN_SUPPORTED_ANDROID_VERSION_CODE, 
                    update.getMinSupportedVersionCode() != null ? update.getMinSupportedVersionCode() : 1);
            editor.putInt(Constants.KEY_LATEST_ANDROID_VERSION_CODE,
                    update.getLatestVersionCode() != null ? update.getLatestVersionCode() : 1);
            editor.putBoolean(Constants.KEY_FORCE_UPDATE,
                    update.getForce() != null && update.getForce());
        }

        editor.apply();
    }

    public void saveSession(String accessToken, AppUserResponse user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.KEY_ACCESS_TOKEN, accessToken);

        if (user != null) {
            editor.putLong(Constants.KEY_USER_ID, user.getId() != null ? user.getId() : -1L);
            editor.putString(Constants.KEY_USERNAME, user.getUsername());
            editor.putString(Constants.KEY_FULL_NAME, user.getFullName());
            editor.putString(Constants.KEY_USER_TYPE, user.getUserType());

            if (user.getPermissions() != null) {
                editor.putString(Constants.KEY_PERMISSIONS, gson.toJson(user.getPermissions()));
            }
            if (user.getCompanyNames() != null) {
                editor.putString(Constants.KEY_COMPANY_NAMES, gson.toJson(user.getCompanyNames()));
            }
        }

        editor.apply();
    }

    public String getAccessToken() {
        return preferences.getString(Constants.KEY_ACCESS_TOKEN, null);
    }

    public Long getUserId() {
        return preferences.getLong(Constants.KEY_USER_ID, -1L);
    }

    public String getUsername() {
        return preferences.getString(Constants.KEY_USERNAME, null);
    }

    public String getFullName() {
        return preferences.getString(Constants.KEY_FULL_NAME, null);
    }

    public String getUserType() {
        return preferences.getString(Constants.KEY_USER_TYPE, null);
    }

    public List<String> getPermissions() {
        String json = preferences.getString(Constants.KEY_PERMISSIONS, null);
        if (json == null) return new ArrayList<>();
        return gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
    }

    public List<String> getCompanyNames() {
        String json = preferences.getString(Constants.KEY_COMPANY_NAMES, null);
        if (json == null) return new ArrayList<>();
        return gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
    }

    public String getClientCode() {
        return preferences.getString(Constants.KEY_CLIENT_CODE, null);
    }

    public String getClientName() {
        return preferences.getString(Constants.KEY_CLIENT_NAME, null);
    }

    public String getApiBaseUrl() {
        return preferences.getString(Constants.KEY_API_BASE_URL, null);
    }

    public String getLogoUrl() {
        return preferences.getString(Constants.KEY_LOGO_URL, null);
    }

    public int getMinSupportedAndroidVersionCode() {
        return preferences.getInt(Constants.KEY_MIN_SUPPORTED_ANDROID_VERSION_CODE, 1);
    }

    public int getLatestAndroidVersionCode() {
        return preferences.getInt(Constants.KEY_LATEST_ANDROID_VERSION_CODE, 1);
    }

    public boolean isForceUpdate() {
        return preferences.getBoolean(Constants.KEY_FORCE_UPDATE, false);
    }

    public String getAndroidApkUrl() {
        return preferences.getString(Constants.KEY_ANDROID_APK_URL, null);
    }

    public String getReleaseNotes() {
        return preferences.getString(Constants.KEY_RELEASE_NOTES, null);
    }

    public boolean hasClientConfig() {
        String apiBaseUrl = getApiBaseUrl();
        return apiBaseUrl != null && !apiBaseUrl.trim().isEmpty();
    }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearLoginSession() {
        preferences.edit()
                .remove(Constants.KEY_ACCESS_TOKEN)
                .remove(Constants.KEY_USERNAME)
                .remove(Constants.KEY_USER_TYPE)
                .apply();
    }

    public void clearClientConfigAndSession() {
        preferences.edit().clear().apply();
    }

    public void setLanguage(String languageCode) {
        preferences.edit().putString(Constants.KEY_LANGUAGE, languageCode).apply();
    }

    public String getLanguage() {
        return preferences.getString(Constants.KEY_LANGUAGE, "en");
    }

    // --- Scanner Settings ---

    public boolean isScannerBeepEnabled() {
        return preferences.getBoolean(Constants.KEY_SCANNER_BEEP, true);
    }

    public void setScannerBeepEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.KEY_SCANNER_BEEP, enabled).apply();
    }

    public String getUrovoIntentAction() {
        return preferences.getString(Constants.KEY_UROVO_INTENT_ACTION, Constants.DEFAULT_UROVO_ACTION);
    }

    public void setUrovoIntentAction(String action) {
        preferences.edit().putString(Constants.KEY_UROVO_INTENT_ACTION, action).apply();
    }

    public String getUrovoDataTag() {
        return preferences.getString(Constants.KEY_UROVO_DATA_TAG, Constants.DEFAULT_UROVO_DATA_TAG);
    }

    public void setUrovoDataTag(String tag) {
        preferences.edit().putString(Constants.KEY_UROVO_DATA_TAG, tag).apply();
    }

    public String getUrovoTypeTag() {
        return preferences.getString(Constants.KEY_UROVO_TYPE_TAG, Constants.DEFAULT_UROVO_TYPE_TAG);
    }

    public void setUrovoTypeTag(String tag) {
        preferences.edit().putString(Constants.KEY_UROVO_TYPE_TAG, tag).apply();
    }

    public void resetScannerSettings() {
        preferences.edit()
                .putBoolean(Constants.KEY_SCANNER_BEEP, true)
                .putString(Constants.KEY_UROVO_INTENT_ACTION, Constants.DEFAULT_UROVO_ACTION)
                .putString(Constants.KEY_UROVO_DATA_TAG, Constants.DEFAULT_UROVO_DATA_TAG)
                .putString(Constants.KEY_UROVO_TYPE_TAG, Constants.DEFAULT_UROVO_TYPE_TAG)
                .apply();
    }

    /**
     * Backward-compatible clear method. Prefer clearLoginSession or clearClientConfigAndSession.
     */
    public void clear() {
        clearClientConfigAndSession();
    }

    private String normalizeBaseUrl(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return normalized;
        }

        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }

        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }

        return normalized;
    }
}
