package com.pinetechs.orvix.ims.android.recheck.presentation.submit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.recheck.data.RecheckRepository;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckRequestResponse;
import com.pinetechs.orvix.ims.android.recheck.data.dto.SubmitRecheckItemRequest;
import com.pinetechs.orvix.ims.android.workarea.data.dto.HierarchyOptionResponse;

import java.util.List;

public class RecheckSubmissionViewModel extends AndroidViewModel {

    private final RecheckRepository repository;
    private final MutableLiveData<Resource<RecheckRequestResponse>> requestState =
            new MutableLiveData<>(Resource.idle());
    private final MutableLiveData<Resource<List<HierarchyOptionResponse>>> floorState =
            new MutableLiveData<>(Resource.idle());
    private final MutableLiveData<Resource<List<HierarchyOptionResponse>>> placeState =
            new MutableLiveData<>(Resource.idle());
    private final MutableLiveData<Resource<List<HierarchyOptionResponse>>> locationState =
            new MutableLiveData<>(Resource.idle());
    private final MutableLiveData<Resource<RecheckRequestResponse>> submitState =
            new MutableLiveData<>(Resource.idle());

    private Long requestId;
    private Long itemId;
    private SubmitRecheckItemRequest pendingRequest;
    private byte[] pendingImage;

    public RecheckSubmissionViewModel(@NonNull Application application) {
        super(application);
        repository = new RecheckRepository(application);
    }

    public LiveData<Resource<RecheckRequestResponse>> getRequestState() {
        return requestState;
    }

    public LiveData<Resource<List<HierarchyOptionResponse>>> getFloorState() {
        return floorState;
    }

    public LiveData<Resource<List<HierarchyOptionResponse>>> getPlaceState() {
        return placeState;
    }

    public LiveData<Resource<List<HierarchyOptionResponse>>> getLocationState() {
        return locationState;
    }

    public LiveData<Resource<RecheckRequestResponse>> getSubmitState() {
        return submitState;
    }

    public void load(Long requestId, Long itemId) {
        if (requestId == null || itemId == null) {
            requestState.setValue(Resource.error("Recheck request and item are required"));
            return;
        }
        this.requestId = requestId;
        this.itemId = itemId;
        requestState.setValue(Resource.loading());
        repository.getRequest(
                requestId,
                new RecheckRepository.RepositoryCallback<RecheckRequestResponse>() {
                    @Override
                    public void onSuccess(RecheckRequestResponse data) {
                        requestState.setValue(Resource.success(data));
                    }

                    @Override
                    public void onError(int httpCode, String message) {
                        requestState.setValue(Resource.error(message));
                    }
                }
        );
    }

    public void loadAssetFloors() {
        if (!hasIds()) return;
        floorState.setValue(Resource.loading());
        repository.assetFloors(requestId, itemId, optionCallback(floorState));
    }

    public void loadAssetPlaces(Long floorId) {
        if (!hasIds() || floorId == null) return;
        placeState.setValue(Resource.loading());
        repository.assetPlaces(
                requestId,
                itemId,
                floorId,
                optionCallback(placeState)
        );
    }

    public void loadSparePartLocations() {
        if (!hasIds()) return;
        locationState.setValue(Resource.loading());
        repository.sparePartLocations(
                requestId,
                itemId,
                optionCallback(locationState)
        );
    }

    public void submit(
            SubmitRecheckItemRequest request,
            byte[] image
    ) {
        if (!hasIds() || request == null) {
            submitState.setValue(Resource.error("Recheck submission is required"));
            return;
        }
        pendingRequest = request;
        pendingImage = image;
        sendPending();
    }

    public void retry() {
        if (pendingRequest != null && hasIds()) {
            sendPending();
        }
    }

    public boolean canRetry() {
        return pendingRequest != null;
    }

    private void sendPending() {
        submitState.setValue(Resource.loading());
        repository.submit(
                requestId,
                itemId,
                pendingRequest,
                pendingImage,
                new RecheckRepository.RepositoryCallback<RecheckRequestResponse>() {
                    @Override
                    public void onSuccess(RecheckRequestResponse data) {
                        pendingRequest = null;
                        pendingImage = null;
                        submitState.setValue(Resource.success(data));
                    }

                    @Override
                    public void onError(int httpCode, String message) {
                        if (httpCode > 0) {
                            pendingRequest = null;
                            pendingImage = null;
                        }
                        submitState.setValue(Resource.error(message));
                    }
                }
        );
    }

    private RecheckRepository.RepositoryCallback<List<HierarchyOptionResponse>>
    optionCallback(
            MutableLiveData<Resource<List<HierarchyOptionResponse>>> target
    ) {
        return new RecheckRepository.RepositoryCallback<List<HierarchyOptionResponse>>() {
            @Override
            public void onSuccess(List<HierarchyOptionResponse> data) {
                target.setValue(Resource.success(data));
            }

            @Override
            public void onError(int httpCode, String message) {
                target.setValue(Resource.error(message));
            }
        };
    }

    private boolean hasIds() {
        return requestId != null && itemId != null;
    }
}
