package com.pinetechs.orvix.ims.android.workarea.data;

import com.pinetechs.orvix.ims.android.workarea.data.dto.WorkAreaSliceResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WorkAreaApi {
    @GET("tasks/{taskId}/work-areas")
    Call<WorkAreaSliceResponse> getWorkAreas(
            @Path("taskId") Long taskId,
            @Query("page") int page,
            @Query("size") int size
    );
}
