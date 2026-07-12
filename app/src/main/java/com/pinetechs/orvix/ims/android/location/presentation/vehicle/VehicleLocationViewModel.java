package com.pinetechs.orvix.ims.android.location.presentation.vehicle;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.location.data.vehicle.VehicleLocationRepository;
import com.pinetechs.orvix.ims.android.location.data.vehicle.VehicleLocationResponse;
import java.util.List;

public class VehicleLocationViewModel extends AndroidViewModel {
    private final VehicleLocationRepository repository;
    private final MutableLiveData<Resource<List<VehicleLocationResponse>>> locationsState = new MutableLiveData<>(Resource.idle());

    public VehicleLocationViewModel(@NonNull Application application) {
        super(application);
        this.repository = new VehicleLocationRepository(application);
    }

    public LiveData<Resource<List<VehicleLocationResponse>>> getLocationsState() {
        return locationsState;
    }

    public void loadLocations(Long taskId) {
        if (taskId == null) {
            locationsState.setValue(Resource.error("Task ID is missing"));
            return;
        }
        locationsState.setValue(Resource.loading());
        repository.getLocations(taskId, new VehicleLocationRepository.RepositoryCallback<List<VehicleLocationResponse>>() {
            @Override
            public void onSuccess(List<VehicleLocationResponse> data) {
                locationsState.setValue(Resource.success(data));
            }
            @Override
            public void onError(String message) {
                locationsState.setValue(Resource.error(message));
            }
        });
    }
}
