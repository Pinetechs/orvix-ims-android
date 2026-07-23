package com.pinetechs.orvix.ims.android.recheck.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pinetechs.orvix.ims.android.OrvixApplication;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckPageResponse;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckRequestResponse;
import com.pinetechs.orvix.ims.android.recheck.data.dto.SubmitRecheckItemRequest;
import com.pinetechs.orvix.ims.android.workarea.data.dto.HierarchyOptionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecheckRepository {

    private final RecheckApi api;

    public RecheckRepository(Context context) {
        api = ApiClient.getClient(context).create(RecheckApi.class);
    }

    public void getActiveRequests(
            int page,
            int size,
            RepositoryCallback<RecheckPageResponse> callback
    ) {
        enqueue(api.requests(null, page, size), callback);
    }

    public void getRequests(
            String status,
            int page,
            int size,
            RepositoryCallback<RecheckPageResponse> callback
    ) {
        enqueue(api.requests(status, page, size), callback);
    }

    public void getRequest(
            Long requestId,
            RepositoryCallback<RecheckRequestResponse> callback
    ) {
        enqueue(api.request(requestId), callback);
    }

    public void start(
            Long requestId,
            RepositoryCallback<RecheckRequestResponse> callback
    ) {
        enqueue(api.start(requestId), callback);
    }

    public void assetFloors(
            Long requestId,
            Long itemId,
            RepositoryCallback<List<HierarchyOptionResponse>> callback
    ) {
        enqueue(api.assetFloors(requestId, itemId, null), callback);
    }

    public void assetPlaces(
            Long requestId,
            Long itemId,
            Long floorId,
            RepositoryCallback<List<HierarchyOptionResponse>> callback
    ) {
        enqueue(api.assetPlaces(requestId, itemId, floorId, null), callback);
    }

    public void sparePartLocations(
            Long requestId,
            Long itemId,
            RepositoryCallback<List<HierarchyOptionResponse>> callback
    ) {
        enqueue(api.sparePartLocations(requestId, itemId, null), callback);
    }

    public void submit(
            Long requestId,
            Long itemId,
            SubmitRecheckItemRequest request,
            byte[] image,
            RepositoryCallback<RecheckRequestResponse> callback
    ) {
        enqueue(
                api.submit(
                        requestId,
                        itemId,
                        RecheckMultipartFactory.requestBody(request),
                        RecheckMultipartFactory.imagePart(image)
                ),
                callback
        );
    }

    private <T> void enqueue(
            Call<T> call,
            RepositoryCallback<T> callback
    ) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(
                    @NonNull Call<T> call,
                    @NonNull Response<T> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(
                            response.code(),
                            ApiErrorUtils.getErrorMessage(response)
                    );
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<T> call,
                    @NonNull Throwable error
            ) {
                String message = error.getMessage();
                if (message == null || message.trim().isEmpty()) {
                    message = OrvixApplication.getInstance()
                            .getString(R.string.err_connection_failed);
                }
                callback.onError(0, message);
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(int httpCode, String message);
    }
}
