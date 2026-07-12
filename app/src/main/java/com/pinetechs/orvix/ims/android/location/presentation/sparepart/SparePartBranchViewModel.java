package com.pinetechs.orvix.ims.android.location.presentation.sparepart;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.location.data.sparepart.SparePartBranchRepository;
import com.pinetechs.orvix.ims.android.location.data.sparepart.SparePartBranchResponse;
import java.util.List;

public class SparePartBranchViewModel extends AndroidViewModel {
    private final SparePartBranchRepository repository;
    private final MutableLiveData<Resource<List<SparePartBranchResponse>>> branchesState = new MutableLiveData<>(Resource.idle());

    public SparePartBranchViewModel(@NonNull Application application) {
        super(application);
        this.repository = new SparePartBranchRepository(application);
    }

    public LiveData<Resource<List<SparePartBranchResponse>>> getBranchesState() {
        return branchesState;
    }

    public void loadBranches(Long taskId) {
        if (taskId == null) {
            branchesState.setValue(Resource.error("Task ID is missing"));
            return;
        }
        branchesState.setValue(Resource.loading());
        repository.getBranches(taskId, new SparePartBranchRepository.RepositoryCallback<List<SparePartBranchResponse>>() {
            @Override
            public void onSuccess(List<SparePartBranchResponse> data) {
                branchesState.setValue(Resource.success(data));
            }
            @Override
            public void onError(String message) {
                branchesState.setValue(Resource.error(message));
            }
        });
    }
}
