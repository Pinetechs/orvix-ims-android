package com.pinetechs.orvix.ims.android.location.presentation.asset;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.location.data.asset.AssetLocationRepository;
import com.pinetechs.orvix.ims.android.location.data.asset.AssetLocationResponse;
import java.util.List;

public class AssetLocationViewModel extends AndroidViewModel {
    private final AssetLocationRepository repository;
    private final MutableLiveData<Resource<List<AssetLocationResponse>>> locationsState = new MutableLiveData<>(Resource.idle());

    public AssetLocationViewModel(@NonNull Application application) {
        super(application);
        this.repository = new AssetLocationRepository(application);
    }

    public LiveData<Resource<List<AssetLocationResponse>>> getLocationsState() {
        return locationsState;
    }

    public void loadLocations(Long taskId) {
        if (taskId == null) {
            locationsState.setValue(Resource.error("Task ID is missing"));
            return;
        }
        locationsState.setValue(Resource.loading());
        repository.getLocations(taskId, new AssetLocationRepository.RepositoryCallback<List<AssetLocationResponse>>() {
            @Override
            public void onSuccess(List<AssetLocationResponse> data) {
                locationsState.setValue(Resource.success(data));
            }
            @Override
            public void onError(String message) {
                locationsState.setValue(Resource.error(message));
            }
        });
    }
}
