package com.pinetechs.orvix.ims.android.location.data.asset;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AssetLocationApi {
    @GET("inventory/tasks/{taskId}/asset-points")
    Call<List<AssetLocationResponse>> getLocations(@Path("taskId") Long taskId);
}
