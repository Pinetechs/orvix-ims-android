package com.pinetechs.orvix.ims.android.recheck.data;

import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckPageResponse;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckRequestResponse;
import com.pinetechs.orvix.ims.android.workarea.data.dto.HierarchyOptionResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RecheckApi {

    @GET("rechecks")
    Call<RecheckPageResponse> requests(
            @Query("status") String status,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("rechecks/{requestId}")
    Call<RecheckRequestResponse> request(
            @Path("requestId") Long requestId
    );

    @POST("rechecks/{requestId}/start")
    Call<RecheckRequestResponse> start(
            @Path("requestId") Long requestId
    );

    @GET("rechecks/{requestId}/items/{itemId}/floors")
    Call<List<HierarchyOptionResponse>> assetFloors(
            @Path("requestId") Long requestId,
            @Path("itemId") Long itemId,
            @Query("search") String search
    );

    @GET("rechecks/{requestId}/items/{itemId}/floors/{floorId}/places")
    Call<List<HierarchyOptionResponse>> assetPlaces(
            @Path("requestId") Long requestId,
            @Path("itemId") Long itemId,
            @Path("floorId") Long floorId,
            @Query("search") String search
    );

    @GET("rechecks/{requestId}/items/{itemId}/locations")
    Call<List<HierarchyOptionResponse>> sparePartLocations(
            @Path("requestId") Long requestId,
            @Path("itemId") Long itemId,
            @Query("search") String search
    );

    @Multipart
    @POST("rechecks/{requestId}/items/{itemId}/submit")
    Call<RecheckRequestResponse> submit(
            @Path("requestId") Long requestId,
            @Path("itemId") Long itemId,
            @Part("request") RequestBody request,
            @Part MultipartBody.Part image
    );
}
