package com.pinetechs.orvix.ims.android.location.data.vehicle;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface VehicleLocationApi {
    @GET("inventory/tasks/{taskId}/locations")
    Call<List<VehicleLocationResponse>> getLocations(@Path("taskId") Long taskId);
}
