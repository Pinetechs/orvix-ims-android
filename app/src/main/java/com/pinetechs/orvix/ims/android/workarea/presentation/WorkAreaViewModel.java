package com.pinetechs.orvix.ims.android.workarea.presentation;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.workarea.data.WorkAreaRepository;
import com.pinetechs.orvix.ims.android.workarea.data.dto.WorkAreaSliceResponse;
import com.pinetechs.orvix.ims.android.workarea.data.dto.WorkAreaResponse;
import java.util.ArrayList;
import java.util.List;

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
        loadPage(taskId, 0, new ArrayList<>());
    }

    private void loadPage(Long taskId, int page, List<WorkAreaResponse> collected) {
        repository.getWorkAreas(taskId, page, 50, new WorkAreaRepository.RepositoryCallback<WorkAreaSliceResponse>() {
            @Override
            public void onSuccess(WorkAreaSliceResponse data) {
                if (data != null && data.getContent() != null) collected.addAll(data.getContent());
                if (data != null && !data.isLast()) {
                    loadPage(taskId, page + 1, collected);
                    return;
                }
                WorkAreaSliceResponse merged = data == null ? new WorkAreaSliceResponse() : data;
                merged.setContent(collected);
                merged.setLast(true);
                workAreasState.setValue(Resource.success(merged));
            }
            @Override
            public void onError(String message) {
                workAreasState.setValue(Resource.error(message));
            }
        });
    }
}
