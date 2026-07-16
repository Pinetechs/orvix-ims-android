package com.pinetechs.orvix.ims.android.workarea.data;

import com.pinetechs.orvix.ims.android.workarea.data.dto.WorkAreaSliceResponse;
import com.pinetechs.orvix.ims.android.workarea.data.dto.HierarchyOptionResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WorkAreaApi {
    @GET("tasks/{taskId}/work-areas")
    Call<WorkAreaSliceResponse> getWorkAreas(
            @Path("taskId") Long taskId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("tasks/{taskId}/work-areas/{locationId}/floors")
    Call<List<HierarchyOptionResponse>> getFloors(
            @Path("taskId") Long taskId,
            @Path("locationId") Long locationId,
            @Query("search") String search
    );

    @GET("tasks/{taskId}/floors/{floorId}/places")
    Call<List<HierarchyOptionResponse>> getPlaces(
            @Path("taskId") Long taskId,
            @Path("floorId") Long floorId,
            @Query("search") String search
    );

    @GET("tasks/{taskId}/work-areas/{branchId}/locations")
    Call<List<HierarchyOptionResponse>> getSpareLocations(
            @Path("taskId") Long taskId,
            @Path("branchId") Long branchId,
            @Query("search") String search
    );

    @POST("tasks/{taskId}/work-areas/{branchId}/locations/{locationId}/complete")
    Call<HierarchyOptionResponse> completeSpareLocation(
            @Path("taskId") Long taskId,
            @Path("branchId") Long branchId,
            @Path("locationId") Long locationId
    );
}
