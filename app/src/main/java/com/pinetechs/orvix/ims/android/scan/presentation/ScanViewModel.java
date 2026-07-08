package com.pinetechs.orvix.ims.android.scan.presentation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.ScanRepository;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

public class ScanViewModel extends AndroidViewModel {

    private final ScanRepository repository;
    private final MutableLiveData<Resource<ScanResponse>> scanState = new MutableLiveData<>(Resource.idle());

    public ScanViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ScanRepository(application);
    }

    public LiveData<Resource<ScanResponse>> getScanState() {
        return scanState;
    }

    public void scan(Long taskId, String barcode, String locationCode, String scanType) {
        if (taskId == null) {
            scanState.setValue(Resource.error("Task ID is missing"));
            return;
        }

        if (barcode == null || barcode.trim().isEmpty()) {
            scanState.setValue(Resource.error("Barcode is required"));
            return;
        }

        if (locationCode == null || locationCode.trim().isEmpty()) {
            scanState.setValue(Resource.error("Location is required"));
            return;
        }

        scanState.setValue(Resource.loading());

        repository.scan(taskId, barcode.trim(), locationCode, scanType, new ScanRepository.RepositoryCallback<ScanResponse>() {
            @Override
            public void onSuccess(ScanResponse data) {
                scanState.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                scanState.setValue(Resource.error(message));
            }
        });
    }
}
