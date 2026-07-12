package com.pinetechs.orvix.ims.android.task.presentation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.task.data.InventoryTaskRepository;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskSliceResponse;

public class TaskListViewModel extends AndroidViewModel {

    private final InventoryTaskRepository repository;
    private final MutableLiveData<Resource<AppInventoryTaskSliceResponse>> tasksState = new MutableLiveData<>(Resource.idle());

    public TaskListViewModel(@NonNull Application application) {
        super(application);
        this.repository = new InventoryTaskRepository(application);
    }

    public LiveData<Resource<AppInventoryTaskSliceResponse>> getTasksState() {
        return tasksState;
    }

    public void loadTasks() {
        loadTasks(false);
    }

    public void loadTasks(boolean includeCompleted) {
        tasksState.setValue(Resource.loading());

        repository.getMyTasks(0, 50, includeCompleted, new InventoryTaskRepository.RepositoryCallback<AppInventoryTaskSliceResponse>() {
            @Override
            public void onSuccess(AppInventoryTaskSliceResponse data) {
                tasksState.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                tasksState.setValue(Resource.error(message));
            }
        });
    }
}
