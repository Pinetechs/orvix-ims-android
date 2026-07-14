package com.pinetechs.orvix.ims.android.scan.presentation.asset;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.asset.AssetScanRepository;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

public class AssetScanViewModel extends AndroidViewModel {
    private final AssetScanRepository repository;
    private final MutableLiveData<Resource<ScanResponse>> scanState = new MutableLiveData<>(Resource.idle());

    public AssetScanViewModel(@NonNull Application application) {
        super(application);
        this.repository = new AssetScanRepository(application);
    }

    public LiveData<Resource<ScanResponse>> getScanState() {
        return scanState;
    }

    public void scanAsset(Long taskId, String barcode, String locationCode, byte[] scanImage) {
        if (taskId == null || barcode == null || barcode.trim().isEmpty()) {
            scanState.setValue(Resource.error("Task ID and asset barcode are required"));
            return;
        }

        scanState.setValue(Resource.loading());
        repository.scanAsset(
                taskId,
                barcode.trim(),
                locationCode,
                scanImage,
                new AssetScanRepository.RepositoryCallback<ScanResponse>() {
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
