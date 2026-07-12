package com.pinetechs.orvix.ims.android.location.data.vehicle;

import android.content.Context;
import androidx.annotation.NonNull;
import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleLocationRepository {
    private final VehicleLocationApi api;

    public VehicleLocationRepository(Context context) {
        this.api = ApiClient.getClient(context).create(VehicleLocationApi.class);
    }

    public void getLocations(Long taskId, RepositoryCallback<List<VehicleLocationResponse>> callback) {
        api.getLocations(taskId).enqueue(new Callback<List<VehicleLocationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<VehicleLocationResponse>> call, @NonNull Response<List<VehicleLocationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<VehicleLocationResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Connection failed");
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
