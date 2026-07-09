package com.pinetechs.orvix.ims.android.bootstrap.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pinetechs.orvix.ims.android.bootstrap.data.dto.BootstrapResolveRequest;
import com.pinetechs.orvix.ims.android.bootstrap.data.dto.BootstrapResolveResponse;
import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import com.pinetechs.orvix.ims.android.core.util.DeviceUtils;
import com.pinetechs.orvix.ims.android.core.util.VersionUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BootstrapRepository {

    private final Context context;

    public BootstrapRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public void loadByClientCode(String clientCode, RepositoryCallback<BootstrapResolveResponse> callback) {
        BootstrapApi api = ApiClient.getBootstrapClient(context).create(BootstrapApi.class);
        int versionCode = VersionUtils.getCurrentVersionCode(context);
        String deviceId = DeviceUtils.getDeviceId(context);

        BootstrapResolveRequest request = new BootstrapResolveRequest(clientCode, versionCode, deviceId);

        api.getClientConfig(request).enqueue(new Callback<BootstrapResolveResponse>() {
            @Override
            public void onResponse(@NonNull Call<BootstrapResolveResponse> call, @NonNull Response<BootstrapResolveResponse> response) {
                handleResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<BootstrapResolveResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Bootstrap service connection failed");
            }
        });
    }

    private void handleResponse(Response<BootstrapResolveResponse> response, RepositoryCallback<BootstrapResolveResponse> callback) {
        if (!response.isSuccessful() || response.body() == null) {
            callback.onError(ApiErrorUtils.getErrorMessage(response));
            return;
        }

        BootstrapResolveResponse body = response.body();
        if (body.getData() == null || body.getData().getClient() == null) {
            callback.onError("Invalid bootstrap response");
            return;
        }

        if (body.getData().getClient().getActive() != null && !body.getData().getClient().getActive()) {
            callback.onError("Client is inactive. Please contact support.");
            return;
        }

        callback.onSuccess(body);
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
