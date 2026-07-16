package com.pinetechs.orvix.ims.android.scan.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pinetechs.orvix.ims.android.OrvixApplication;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanCorrectionRequest;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanRequest;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanRepository {
    private final ScanApi api;

    public ScanRepository(Context context) {
        api = ApiClient.getClient(context).create(ScanApi.class);
    }

    public void scan(Long taskId, ScanRequest request, byte[] image, CallbackResult callback) {
        enqueue(api.scan(taskId, ScanMultipartFactory.requestBody(request),
                ScanMultipartFactory.imagePart(image)), callback);
    }

    public void correct(Long taskId, Long currentScanId, ScanCorrectionRequest request,
                        byte[] image, CallbackResult callback) {
        enqueue(api.correct(taskId, currentScanId,
                ScanMultipartFactory.requestBody(request), ScanMultipartFactory.imagePart(image)), callback);
    }

    private void enqueue(Call<ScanResponse> call, CallbackResult callback) {
        call.enqueue(new Callback<ScanResponse>() {
            @Override
            public void onResponse(@NonNull Call<ScanResponse> call,
                                   @NonNull Response<ScanResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(), ApiErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ScanResponse> call, @NonNull Throwable error) {
                callback.onError(0, error.getMessage() == null ? OrvixApplication.getInstance().getString(R.string.err_connection_failed) : error.getMessage());
            }
        });
    }

    public interface CallbackResult {
        void onSuccess(ScanResponse response);
        void onError(int httpCode, String message);
    }
}
