package com.pinetechs.orvix.ims.android.location.presentation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.task.data.InventoryTaskRepository;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryLocationResponse;

import java.util.List;

public class LocationSelectionViewModel extends AndroidViewModel {

    private final InventoryTaskRepository repository;
    private final MutableLiveData<Resource<List<AppInventoryLocationResponse>>> locationsState = new MutableLiveData<>(Resource.idle());

    public LocationSelectionViewModel(@NonNull Application application) {
        super(application);
        this.repository = new InventoryTaskRepository(application);
    }

    public LiveData<Resource<List<AppInventoryLocationResponse>>> getLocationsState() {
        return locationsState;
    }

    public void loadLocations(Long taskId) {
        if (taskId == null) {
            locationsState.setValue(Resource.error("Task ID is missing"));
            return;
        }

        locationsState.setValue(Resource.loading());

        repository.getTaskLocations(taskId, new InventoryTaskRepository.RepositoryCallback<List<AppInventoryLocationResponse>>() {
            @Override
            public void onSuccess(List<AppInventoryLocationResponse> data) {
                locationsState.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                locationsState.setValue(Resource.error(message));
            }
        });
    }
}
