package com.pinetechs.orvix.ims.android.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.pinetechs.orvix.ims.android.auth.data.dto.AppUserResponse;
import com.pinetechs.orvix.ims.android.core.util.Constants;

public class SessionManager {

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String accessToken, AppUserResponse user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.KEY_ACCESS_TOKEN, accessToken);

        if (user != null) {
            editor.putString(Constants.KEY_USERNAME, user.getUsername());
            editor.putString(Constants.KEY_USER_TYPE, user.getUserType());
        }

        editor.apply();
    }

    public String getAccessToken() {
        return preferences.getString(Constants.KEY_ACCESS_TOKEN, null);
    }

    public String getUsername() {
        return preferences.getString(Constants.KEY_USERNAME, null);
    }

    public String getUserType() {
        return preferences.getString(Constants.KEY_USER_TYPE, null);
    }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
