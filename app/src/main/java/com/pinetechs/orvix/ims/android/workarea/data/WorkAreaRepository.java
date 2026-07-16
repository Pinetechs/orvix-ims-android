package com.pinetechs.orvix.ims.android.workarea.data;

import android.content.Context;
import androidx.annotation.NonNull;
import com.pinetechs.orvix.ims.android.OrvixApplication;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.network.ApiErrorUtils;
import com.pinetechs.orvix.ims.android.workarea.data.dto.WorkAreaSliceResponse;
import com.pinetechs.orvix.ims.android.workarea.data.dto.HierarchyOptionResponse;
import java.util.List;
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
                callback.onError(t.getMessage() != null ? t.getMessage() : OrvixApplication.getInstance().getString(R.string.err_connection_failed));
            }
        });
    }

    public void getFloors(Long taskId, Long locationId, RepositoryCallback<List<HierarchyOptionResponse>> callback) {
        getFloors(taskId, locationId, null, callback);
    }

    public void getFloors(Long taskId, Long locationId, String search,
                          RepositoryCallback<List<HierarchyOptionResponse>> callback) {
        enqueueOptions(api.getFloors(taskId, locationId, normalizeSearch(search)), callback);
    }

    public void getPlaces(Long taskId, Long floorId, RepositoryCallback<List<HierarchyOptionResponse>> callback) {
        getPlaces(taskId, floorId, null, callback);
    }

    public void getPlaces(Long taskId, Long floorId, String search,
                          RepositoryCallback<List<HierarchyOptionResponse>> callback) {
        enqueueOptions(api.getPlaces(taskId, floorId, normalizeSearch(search)), callback);
    }

    public void getSpareLocations(Long taskId, Long branchId, RepositoryCallback<List<HierarchyOptionResponse>> callback) {
        getSpareLocations(taskId, branchId, null, callback);
    }

    public void getSpareLocations(Long taskId, Long branchId, String search,
                                  RepositoryCallback<List<HierarchyOptionResponse>> callback) {
        enqueueOptions(api.getSpareLocations(taskId, branchId, normalizeSearch(search)), callback);
    }

    public void completeSpareLocation(Long taskId, Long branchId, Long locationId,
                                      RepositoryCallback<HierarchyOptionResponse> callback) {
        api.completeSpareLocation(taskId, branchId, locationId)
                .enqueue(new Callback<HierarchyOptionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<HierarchyOptionResponse> call,
                                           @NonNull Response<HierarchyOptionResponse> response) {
                        if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                        else callback.onError(ApiErrorUtils.getErrorMessage(response));
                    }

                    @Override
                    public void onFailure(@NonNull Call<HierarchyOptionResponse> call, @NonNull Throwable error) {
                        callback.onError(error.getMessage() == null ? OrvixApplication.getInstance().getString(R.string.err_connection_failed) : error.getMessage());
                    }
                });
    }

    private String normalizeSearch(String search) {
        if (search == null || search.trim().isEmpty()) return null;
        return search.trim();
    }

    private void enqueueOptions(Call<List<HierarchyOptionResponse>> call,
                                RepositoryCallback<List<HierarchyOptionResponse>> callback) {
        call.enqueue(new Callback<List<HierarchyOptionResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<HierarchyOptionResponse>> call,
                                   @NonNull Response<List<HierarchyOptionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                else callback.onError(ApiErrorUtils.getErrorMessage(response));
            }

            @Override
            public void onFailure(@NonNull Call<List<HierarchyOptionResponse>> call, @NonNull Throwable error) {
                callback.onError(error.getMessage() == null ? OrvixApplication.getInstance().getString(R.string.err_connection_failed) : error.getMessage());
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
