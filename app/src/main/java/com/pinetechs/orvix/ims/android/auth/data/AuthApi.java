package com.pinetechs.orvix.ims.android.auth.data;

import com.pinetechs.orvix.ims.android.auth.data.dto.LoginRequest;
import com.pinetechs.orvix.ims.android.auth.data.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/app/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
