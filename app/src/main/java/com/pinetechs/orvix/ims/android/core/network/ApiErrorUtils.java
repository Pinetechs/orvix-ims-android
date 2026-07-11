package com.pinetechs.orvix.ims.android.core.network;

import com.google.gson.Gson;
import com.pinetechs.orvix.ims.android.core.dto.ErrorResponse;

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


                    try {
                        Gson gson = new Gson();
                        ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
                        if (errorResponse != null && errorResponse.getMessage() != null) {
                            return errorResponse.getMessage();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    return body;
                }
            }
        } catch (IOException ignored) {
        }

        return "Request failed. Code: " + response.code();
    }
}
