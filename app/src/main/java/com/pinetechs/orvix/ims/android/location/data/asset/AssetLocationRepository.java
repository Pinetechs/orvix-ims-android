package com.pinetechs.orvix.ims.android.location.data.asset;

import android.content.Context;
import androidx.annotation.NonNull;
import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetLocationRepository {
    private final AssetLocationApi api;

    public AssetLocationRepository(Context context) {
        this.api = ApiClient.getClient(context).create(AssetLocationApi.class);
    }

    public void getLocations(Long taskId, RepositoryCallback<List<AssetLocationResponse>> callback) {
        api.getLocations(taskId).enqueue(new Callback<List<AssetLocationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<AssetLocationResponse>> call, @NonNull Response<List<AssetLocationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AssetLocationResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Connection failed");
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
