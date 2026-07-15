package com.pinetechs.orvix.ims.android.scan.presentation.vehicle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.ScanRepository;
import com.pinetechs.orvix.ims.android.scan.data.ScanRequestFactory;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanCorrectionRequest;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanRequest;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

public class VehicleScanViewModel extends AndroidViewModel {
    private final ScanRepository repository;
    private final MutableLiveData<Resource<ScanResponse>> scanState = new MutableLiveData<>(Resource.idle());
    private Long pendingTaskId;
    private ScanRequest pendingRequest;
    private byte[] pendingImage;
    private Long pendingCorrectionTaskId;
    private Long pendingCurrentScanId;
    private ScanCorrectionRequest pendingCorrection;
    private byte[] pendingCorrectionImage;

    public VehicleScanViewModel(@NonNull Application application) {
        super(application);
        repository = new ScanRepository(application);
    }

    public LiveData<Resource<ScanResponse>> getScanState() { return scanState; }

    public void scanVehicle(Long taskId, String vin, Long locationId, String symbology, byte[] image) {
        if (taskId == null || locationId == null || vin == null || vin.trim().isEmpty()) {
            scanState.setValue(Resource.error("Task, VIN and location are required"));
            return;
        }
        ScanRequest request = ScanRequestFactory.create(getApplication(), vin.trim().toUpperCase(), symbology, image);
        request.setLocationId(locationId);
        pendingTaskId = taskId;
        pendingRequest = request;
        pendingImage = image;
        sendPending();
    }

    public void retryLastScan() {
        if (pendingCorrection != null) sendPendingCorrection();
        else if (pendingRequest != null) sendPending();
    }

    public void correctVehicle(Long taskId, Long currentScanId, Long locationId, String reason,
                               String symbology, byte[] image) {
        ScanCorrectionRequest request = ScanRequestFactory.correction(getApplication(), reason, symbology, image);
        request.setLocationId(locationId);
        pendingCorrectionTaskId = taskId;
        pendingCurrentScanId = currentScanId;
        pendingCorrection = request;
        pendingCorrectionImage = image;
        sendPendingCorrection();
    }

    private void sendPending() {
        scanState.setValue(Resource.loading());
        repository.scan(pendingTaskId, pendingRequest, pendingImage, callback(true));
    }

    private void sendPendingCorrection() {
        scanState.setValue(Resource.loading());
        repository.correct(pendingCorrectionTaskId, pendingCurrentScanId, pendingCorrection,
                pendingCorrectionImage, callback(false));
    }

    private ScanRepository.CallbackResult callback(boolean clearOnSuccess) {
        return new ScanRepository.CallbackResult() {
            @Override public void onSuccess(ScanResponse response) {
                if (clearOnSuccess) { pendingTaskId = null; pendingRequest = null; pendingImage = null; }
                else { pendingCorrectionTaskId = null; pendingCurrentScanId = null; pendingCorrection = null; pendingCorrectionImage = null; }
                scanState.setValue(Resource.success(response));
            }
            @Override public void onError(int httpCode, String message) {
                if (httpCode > 0) {
                    if (clearOnSuccess) { pendingTaskId = null; pendingRequest = null; pendingImage = null; }
                    else { pendingCorrectionTaskId = null; pendingCurrentScanId = null; pendingCorrection = null; pendingCorrectionImage = null; }
                }
                scanState.setValue(Resource.error((httpCode > 0 ? "[" + httpCode + "] " : "") + message));
            }
        };
    }
}
