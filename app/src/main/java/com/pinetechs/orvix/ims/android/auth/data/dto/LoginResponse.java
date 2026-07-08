package com.pinetechs.orvix.ims.android.auth.data.dto;

public class LoginResponse {

    private String accessToken;
    private AppUserResponse user;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public AppUserResponse getUser() {
        return user;
    }

    public void setUser(AppUserResponse user) {
        this.user = user;
    }
}
