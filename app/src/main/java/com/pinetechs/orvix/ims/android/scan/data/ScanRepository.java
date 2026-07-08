package com.pinetechs.orvix.ims.android.scan.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanRequest;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanRepository {

    private final ScanApi api;

    public ScanRepository(Context context) {
        this.api = ApiClient.getClient(context).create(ScanApi.class);
    }

    public void scan(Long taskId, String barcode, String locationCode, String scanType, RepositoryCallback<ScanResponse> callback) {
        ScanRequest request = new ScanRequest(barcode, locationCode, scanType);

        api.scan(taskId, request).enqueue(new Callback<ScanResponse>() {
            @Override
            public void onResponse(@NonNull Call<ScanResponse> call, @NonNull Response<ScanResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ScanResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Connection failed");
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
