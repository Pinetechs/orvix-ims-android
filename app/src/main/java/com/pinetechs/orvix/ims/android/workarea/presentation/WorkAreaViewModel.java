package com.pinetechs.orvix.ims.android.workarea.presentation;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.workarea.data.WorkAreaRepository;
import com.pinetechs.orvix.ims.android.workarea.data.dto.WorkAreaSliceResponse;

public class WorkAreaViewModel extends AndroidViewModel {
    private final WorkAreaRepository repository;
    private final MutableLiveData<Resource<WorkAreaSliceResponse>> workAreasState = new MutableLiveData<>(Resource.idle());

    public WorkAreaViewModel(@NonNull Application application) {
        super(application);
        this.repository = new WorkAreaRepository(application);
    }

    public LiveData<Resource<WorkAreaSliceResponse>> getWorkAreasState() {
        return workAreasState;
    }

    public void loadWorkAreas(Long taskId) {
        if (taskId == null) {
            workAreasState.setValue(Resource.error("Task ID is missing"));
            return;
        }
        workAreasState.setValue(Resource.loading());
        repository.getWorkAreas(taskId, 0, 100, new WorkAreaRepository.RepositoryCallback<WorkAreaSliceResponse>() {
            @Override
            public void onSuccess(WorkAreaSliceResponse data) {
                workAreasState.setValue(Resource.success(data));
            }
            @Override
            public void onError(String message) {
                workAreasState.setValue(Resource.error(message));
            }
        });
    }
}
