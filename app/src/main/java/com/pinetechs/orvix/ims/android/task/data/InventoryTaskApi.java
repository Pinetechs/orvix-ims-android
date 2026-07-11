package com.pinetechs.orvix.ims.android.task.data;

import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryLocationResponse;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface InventoryTaskApi {

    @GET("app/inventory/tasks")
    Call<List<AppInventoryTaskResponse>> getMyTasks();

    @GET("app/inventory/tasks/{taskId}/locations")
    Call<List<AppInventoryLocationResponse>> getTaskLocations(@Path("taskId") Long taskId);
}
