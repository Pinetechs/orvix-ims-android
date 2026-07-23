package com.pinetechs.orvix.ims.android.recheck.data;

import com.google.gson.Gson;
import com.pinetechs.orvix.ims.android.recheck.data.dto.SubmitRecheckItemRequest;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class RecheckMultipartFactory {

    private static final MediaType JSON =
            MediaType.parse("application/json; charset=utf-8");
    private static final MediaType JPEG = MediaType.parse("image/jpeg");
    private static final Gson GSON = new Gson();

    private RecheckMultipartFactory() {
    }

    public static RequestBody requestBody(SubmitRecheckItemRequest request) {
        return RequestBody.create(JSON, GSON.toJson(request));
    }

    public static MultipartBody.Part imagePart(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        RequestBody image = RequestBody.create(JPEG, imageBytes);
        String filename = "recheck-" + System.currentTimeMillis() + ".jpg";
        return MultipartBody.Part.createFormData("image", filename, image);
    }
}
