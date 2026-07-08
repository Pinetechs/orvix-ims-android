package com.pinetechs.orvix.ims.android.task.presentation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.task.data.InventoryTaskRepository;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskResponse;

import java.util.List;

public class TaskListViewModel extends AndroidViewModel {

    private final InventoryTaskRepository repository;
    private final MutableLiveData<Resource<List<AppInventoryTaskResponse>>> tasksState = new MutableLiveData<>(Resource.idle());

    public TaskListViewModel(@NonNull Application application) {
        super(application);
        this.repository = new InventoryTaskRepository(application);
    }

    public LiveData<Resource<List<AppInventoryTaskResponse>>> getTasksState() {
        return tasksState;
    }

    public void loadTasks() {
        tasksState.setValue(Resource.loading());

        repository.getMyTasks(new InventoryTaskRepository.RepositoryCallback<List<AppInventoryTaskResponse>>() {
            @Override
            public void onSuccess(List<AppInventoryTaskResponse> data) {
                tasksState.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                tasksState.setValue(Resource.error(message));
            }
        });
    }
}
