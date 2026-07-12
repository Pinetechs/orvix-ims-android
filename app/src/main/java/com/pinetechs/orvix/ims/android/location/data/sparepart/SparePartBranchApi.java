package com.pinetechs.orvix.ims.android.location.data.sparepart;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SparePartBranchApi {
    @GET("inventory/tasks/{taskId}/branches")
    Call<List<SparePartBranchResponse>> getBranches(@Path("taskId") Long taskId);
}
