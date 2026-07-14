package com.pinetechs.orvix.ims.android.scan.data;

import com.google.gson.Gson;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanRequest;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class ScanMultipartFactory {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType JPEG = MediaType.parse("image/jpeg");
    private static final Gson GSON = new Gson();

    private ScanMultipartFactory() {
    }

    public static RequestBody requestBody(ScanRequest request) {
        return RequestBody.create(JSON, GSON.toJson(request));
    }

    public static MultipartBody.Part imagePart(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) return null;

        RequestBody imageBody = RequestBody.create(JPEG, imageBytes);
        String filename = "scan-" + System.currentTimeMillis() + ".jpg";
        return MultipartBody.Part.createFormData("image", filename, imageBody);
    }
}
