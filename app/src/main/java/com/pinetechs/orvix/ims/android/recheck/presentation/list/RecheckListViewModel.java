package com.pinetechs.orvix.ims.android.recheck.presentation.list;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.recheck.data.RecheckRepository;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckPageResponse;

public class RecheckListViewModel extends AndroidViewModel {

    private final RecheckRepository repository;
    private final MutableLiveData<Resource<RecheckPageResponse>> state =
            new MutableLiveData<>(Resource.idle());

    public RecheckListViewModel(@NonNull Application application) {
        super(application);
        repository = new RecheckRepository(application);
    }

    public LiveData<Resource<RecheckPageResponse>> getState() {
        return state;
    }

    public void load() {
        state.setValue(Resource.loading());
        repository.getActiveRequests(
                0,
                50,
                new RecheckRepository.RepositoryCallback<RecheckPageResponse>() {
                    @Override
                    public void onSuccess(RecheckPageResponse data) {
                        state.setValue(Resource.success(data));
                    }

                    @Override
                    public void onError(int httpCode, String message) {
                        state.setValue(Resource.error(message));
                    }
                }
        );
    }
}
