package com.pinetechs.orvix.ims.android.scan.presentation.sparepart;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;
import com.pinetechs.orvix.ims.android.scan.data.sparepart.SparePartScanRepository;

public class SparePartScanViewModel extends AndroidViewModel {
    private final SparePartScanRepository repository;
    private final MutableLiveData<Resource<ScanResponse>> scanState = new MutableLiveData<>(Resource.idle());

    public SparePartScanViewModel(@NonNull Application application) {
        super(application);
        this.repository = new SparePartScanRepository(application);
    }

    public LiveData<Resource<ScanResponse>> getScanState() {
        return scanState;
    }

    public void scanSparePart(Long taskId, String barcode, String branchCode, byte[] scanImage) {
        if (taskId == null || barcode == null || barcode.trim().isEmpty()) {
            scanState.setValue(Resource.error("Task ID and spare-part barcode are required"));
            return;
        }

        scanState.setValue(Resource.loading());
        repository.scanSparePart(
                taskId,
                barcode.trim().toUpperCase(),
                branchCode,
                scanImage,
                new SparePartScanRepository.RepositoryCallback<ScanResponse>() {
                    @Override
                    public void onSuccess(ScanResponse data) {
                        scanState.setValue(Resource.success(data));
                    }

                    @Override
                    public void onError(String message) {
                        scanState.setValue(Resource.error(message));
                    }
                }
        );
    }
}
