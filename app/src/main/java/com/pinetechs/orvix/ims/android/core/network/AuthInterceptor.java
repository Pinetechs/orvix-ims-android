package com.pinetechs.orvix.ims.android.core.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pinetechs.orvix.ims.android.core.storage.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = sessionManager.getAccessToken();

        Request request = token == null || token.trim().isEmpty()
                ? original
                : original.newBuilder().header("Authorization", "Bearer " + token).build();
        Response response = chain.proceed(request);
        if (response.code() == 401) sessionManager.clearLoginSession();
        return response;
    }
}
