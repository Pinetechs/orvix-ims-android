package com.pinetechs.orvix.ims.android.location.data.sparepart;

import android.content.Context;
import androidx.annotation.NonNull;
import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SparePartBranchRepository {
    private final SparePartBranchApi api;

    public SparePartBranchRepository(Context context) {
        this.api = ApiClient.getClient(context).create(SparePartBranchApi.class);
    }

    public void getBranches(Long taskId, RepositoryCallback<List<SparePartBranchResponse>> callback) {
        api.getBranches(taskId).enqueue(new Callback<List<SparePartBranchResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<SparePartBranchResponse>> call, @NonNull Response<List<SparePartBranchResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SparePartBranchResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Connection failed");
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
