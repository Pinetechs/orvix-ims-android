package com.pinetechs.orvix.ims.android.task.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pinetechs.orvix.ims.android.OrvixApplication;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskSliceResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryTaskRepository {

    private final InventoryTaskApi api;

    public InventoryTaskRepository(Context context) {
        this.api = ApiClient.getClient(context).create(InventoryTaskApi.class);
    }

    public void getMyTasks(int page, int size, boolean includeCompleted, RepositoryCallback<AppInventoryTaskSliceResponse> callback) {
        api.getMyTasks(page, size, includeCompleted).enqueue(new Callback<AppInventoryTaskSliceResponse>() {
            @Override
            public void onResponse(@NonNull Call<AppInventoryTaskSliceResponse> call,
                                   @NonNull Response<AppInventoryTaskSliceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AppInventoryTaskSliceResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : OrvixApplication.getInstance().getString(R.string.err_connection_failed));
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
