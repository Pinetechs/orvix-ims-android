package com.pinetechs.orvix.ims.android.scan.data.sparepart;

import com.pinetechs.orvix.ims.android.scan.data.dto.ScanRequest;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface SparePartScanApi {

    @POST("inventory/tasks/{taskId}/scan-sparepart")
    Call<ScanResponse> scan(@Path("taskId") Long taskId, @Body ScanRequest request);

    @Multipart
    @POST("inventory/tasks/{taskId}/scan-sparepart")
    Call<ScanResponse> scanWithImage(
            @Path("taskId") Long taskId,
            @Part("request") RequestBody request,
            @Part MultipartBody.Part image
    );
}
