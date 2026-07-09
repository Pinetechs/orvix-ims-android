package com.pinetechs.orvix.ims.android.bootstrap.data;

import com.pinetechs.orvix.ims.android.bootstrap.data.dto.BootstrapResolveRequest;
import com.pinetechs.orvix.ims.android.bootstrap.data.dto.BootstrapResolveResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BootstrapApi {

    @POST("api/bootstrap/resolve")
    Call<BootstrapResolveResponse> getClientConfig(
       @Body BootstrapResolveRequest request
    );
}
