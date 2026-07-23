package com.pinetechs.orvix.ims.android.recheck.presentation.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.recheck.data.RecheckRepository;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckRequestResponse;

public class RecheckDetailViewModel extends AndroidViewModel {

    private final RecheckRepository repository;
    private final MutableLiveData<Resource<RecheckRequestResponse>> state =
            new MutableLiveData<>(Resource.idle());
    private Long requestId;

    public RecheckDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new RecheckRepository(application);
    }

    public LiveData<Resource<RecheckRequestResponse>> getState() {
        return state;
    }

    public void load(Long requestId) {
        if (requestId == null) {
            state.setValue(Resource.error("Recheck request id is required"));
            return;
        }
        this.requestId = requestId;
        state.setValue(Resource.loading());
        repository.getRequest(requestId, callback());
    }

    public void refresh() {
        if (requestId != null) {
            load(requestId);
        }
    }

    public void start() {
        if (requestId == null) {
            state.setValue(Resource.error("Recheck request id is required"));
            return;
        }
        state.setValue(Resource.loading());
        repository.start(requestId, callback());
    }

    private RecheckRepository.RepositoryCallback<RecheckRequestResponse> callback() {
        return new RecheckRepository.RepositoryCallback<RecheckRequestResponse>() {
            @Override
            public void onSuccess(RecheckRequestResponse data) {
                state.setValue(Resource.success(data));
            }

            @Override
            public void onError(int httpCode, String message) {
                state.setValue(Resource.error(message));
            }
        };
    }
}
