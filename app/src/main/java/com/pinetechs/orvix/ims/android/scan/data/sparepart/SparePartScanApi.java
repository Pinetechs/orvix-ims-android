package com.pinetechs.orvix.ims.android.scan.data.sparepart;

import com.pinetechs.orvix.ims.android.scan.data.dto.ScanRequest;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SparePartScanApi {
    @POST("inventory/tasks/{taskId}/scan-sparepart")
    Call<ScanResponse> scan(@Path("taskId") Long taskId, @Body ScanRequest request);
}
