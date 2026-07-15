package com.pinetechs.orvix.ims.android.scan.data;

import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ScanApi {
    @Multipart
    @POST("tasks/{taskId}/scans")
    Call<ScanResponse> scan(
            @Path("taskId") Long taskId,
            @Part("request") RequestBody request,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("tasks/{taskId}/scans/{currentScanId}/corrections")
    Call<ScanResponse> correct(
            @Path("taskId") Long taskId,
            @Path("currentScanId") Long currentScanId,
            @Part("request") RequestBody request,
            @Part MultipartBody.Part image
    );
}
