package com.pinetechs.orvix.ims.android.scan.data.vehicle;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import com.pinetechs.orvix.ims.android.scan.data.ScanMultipartFactory;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanRequest;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleScanRepository {
    private final VehicleScanApi api;

    public VehicleScanRepository(Context context) {
        this.api = ApiClient.getClient(context).create(VehicleScanApi.class);
    }

    public void scanVehicle(
            Long taskId,
            String barcode,
            String locationCode,
            String type,
            byte[] scanImage,
            RepositoryCallback<ScanResponse> callback
    ) {
        ScanRequest request = new ScanRequest(barcode, locationCode, type);
        Call<ScanResponse> call;

        if (scanImage != null && scanImage.length > 0) {
            MultipartBody.Part imagePart = ScanMultipartFactory.imagePart(scanImage);
            call = api.scanWithImage(taskId, ScanMultipartFactory.requestBody(request), imagePart);
        } else {
            call = api.scan(taskId, request);
        }

        enqueue(call, callback);
    }

    private void enqueue(Call<ScanResponse> call, RepositoryCallback<ScanResponse> callback) {
        call.enqueue(new Callback<ScanResponse>() {
            @Override
            public void onResponse(@NonNull Call<ScanResponse> call, @NonNull Response<ScanResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ScanResponse> call, @NonNull Throwable throwable) {
                callback.onError(throwable.getMessage() != null ? throwable.getMessage() : "Connection failed");
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);

        void onError(String message);
    }
}
