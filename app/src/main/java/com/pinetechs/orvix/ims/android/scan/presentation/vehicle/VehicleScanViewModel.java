package com.pinetechs.orvix.ims.android.scan.presentation.vehicle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;
import com.pinetechs.orvix.ims.android.scan.data.vehicle.VehicleScanRepository;

public class VehicleScanViewModel extends AndroidViewModel {
    private final VehicleScanRepository repository;
    private final MutableLiveData<Resource<ScanResponse>> scanState = new MutableLiveData<>(Resource.idle());

    public VehicleScanViewModel(@NonNull Application application) {
        super(application);
        this.repository = new VehicleScanRepository(application);
    }

    public LiveData<Resource<ScanResponse>> getScanState() {
        return scanState;
    }

    public void scanVehicle(Long taskId, String vin, String locationCode, byte[] scanImage) {
        if (taskId == null || vin == null || vin.trim().isEmpty()) {
            scanState.setValue(Resource.error("Task ID and VIN are required"));
            return;
        }

        scanState.setValue(Resource.loading());
        repository.scanVehicle(
                taskId,
                vin.trim().toUpperCase(),
                locationCode,
                "VIN",
                scanImage,
                new VehicleScanRepository.RepositoryCallback<ScanResponse>() {
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
