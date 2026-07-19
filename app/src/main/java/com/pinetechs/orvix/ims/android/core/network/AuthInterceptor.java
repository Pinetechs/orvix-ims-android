package com.pinetechs.orvix.ims.android.core.network;

import android.content.Context;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pinetechs.orvix.ims.android.OrvixApplication;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.auth.presentation.LoginActivity;
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
        if (response.code() == 401) {
            sessionManager.clearLoginSession();
            redirectToLogin();
        }
        return response;
    }

    private void redirectToLogin() {
        OrvixApplication app = OrvixApplication.getInstance();
        if (app != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(app, R.string.msg_session_expired, Toast.LENGTH_LONG).show();
            });

            Intent intent = new Intent(app, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            app.startActivity(intent);
        }
    }
}
