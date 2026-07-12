package com.pinetechs.orvix.ims.android.task.data;

import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskSliceResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface InventoryTaskApi {

    @GET("tasks")
    Call<AppInventoryTaskSliceResponse> getMyTasks(
            @Query("page") int page,
            @Query("size") int size,
            @Query("includeCompleted") boolean includeCompleted
    );
}
