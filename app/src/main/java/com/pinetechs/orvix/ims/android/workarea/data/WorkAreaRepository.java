package com.pinetechs.orvix.ims.android.workarea.data;

import android.content.Context;
import androidx.annotation.NonNull;
import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import com.pinetechs.orvix.ims.android.workarea.data.dto.WorkAreaSliceResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkAreaRepository {
    private final WorkAreaApi api;

    public WorkAreaRepository(Context context) {
        this.api = ApiClient.getClient(context).create(WorkAreaApi.class);
    }

    public void getWorkAreas(Long taskId, int page, int size, RepositoryCallback<WorkAreaSliceResponse> callback) {
        api.getWorkAreas(taskId, page, size).enqueue(new Callback<WorkAreaSliceResponse>() {
            @Override
            public void onResponse(@NonNull Call<WorkAreaSliceResponse> call, @NonNull Response<WorkAreaSliceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<WorkAreaSliceResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Connection failed");
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
