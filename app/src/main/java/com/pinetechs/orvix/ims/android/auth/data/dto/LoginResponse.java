package com.pinetechs.orvix.ims.android.auth.data.dto;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("token")
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
