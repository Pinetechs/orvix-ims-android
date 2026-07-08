package com.pinetechs.orvix.ims.android.task.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryLocationResponse;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryTaskRepository {

    private final InventoryTaskApi api;

    public InventoryTaskRepository(Context context) {
        this.api = ApiClient.getClient(context).create(InventoryTaskApi.class);
    }

    public void getMyTasks(RepositoryCallback<List<AppInventoryTaskResponse>> callback) {
        api.getMyTasks().enqueue(new Callback<List<AppInventoryTaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<AppInventoryTaskResponse>> call,
                                   @NonNull Response<List<AppInventoryTaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AppInventoryTaskResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Connection failed");
            }
        });
    }

    public void getTaskLocations(Long taskId, RepositoryCallback<List<AppInventoryLocationResponse>> callback) {
        api.getTaskLocations(taskId).enqueue(new Callback<List<AppInventoryLocationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<AppInventoryLocationResponse>> call,
                                   @NonNull Response<List<AppInventoryLocationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AppInventoryLocationResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Connection failed");
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
