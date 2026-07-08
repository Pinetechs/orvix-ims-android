package com.pinetechs.orvix.ims.android.core.network;

import java.io.IOException;

import retrofit2.Response;

public final class ApiErrorUtils {

    private ApiErrorUtils() {
    }

    public static String getErrorMessage(Response<?> response) {
        if (response == null) {
            return "Unexpected network error";
        }

        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                if (body != null && !body.trim().isEmpty()) {
                    return body;
                }
            }
        } catch (IOException ignored) {
        }

        return "Request failed. Code: " + response.code();
    }
}
