package com.pinetechs.orvix.ims.android.scan.data.sparepart;

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

public class SparePartScanRepository {

    private final SparePartScanApi api;

    public SparePartScanRepository(Context context) {
        this.api = ApiClient.getClient(context).create(SparePartScanApi.class);
    }

    public void scanSparePart(
            Long taskId,
            String barcode,
            String branchCode,
            byte[] scanImage,
            RepositoryCallback<ScanResponse> callback
    ) {
        ScanRequest request = new ScanRequest(barcode, branchCode, "SPARE_PART");
        Call<ScanResponse> call;

        if (scanImage != null && scanImage.length > 0) {
            MultipartBody.Part imagePart = ScanMultipartFactory.imagePart(scanImage);
            call = api.scanWithImage(taskId, ScanMultipartFactory.requestBody(request), imagePart);
        } else {
            call = api.scan(taskId, request);
        }

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
