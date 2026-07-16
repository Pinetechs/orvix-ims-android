package com.pinetechs.orvix.ims.android.scan.presentation.asset;

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

public class AssetScanViewModel extends AndroidViewModel {
    private final ScanRepository scanRepository;
    private final MutableLiveData<Resource<ScanResponse>> scanState = new MutableLiveData<>(Resource.idle());
    private Long pendingTaskId;
    private ScanRequest pendingRequest;
    private byte[] pendingImage;
    private Long pendingCorrectionTaskId;
    private Long pendingCurrentScanId;
    private ScanCorrectionRequest pendingCorrection;
    private byte[] pendingCorrectionImage;

    public AssetScanViewModel(@NonNull Application app) {
        super(app);
        scanRepository = new ScanRepository(app);
    }

    public LiveData<Resource<ScanResponse>> getScanState() { return scanState; }
    public void scanAsset(Long taskId, String code, Long locationId, Long floorId, Long placeId,
                          String symbology, byte[] image) {
        if (taskId == null || locationId == null || floorId == null || placeId == null
                || code == null || code.trim().isEmpty()) {
            scanState.setValue(Resource.error("Task, barcode, location, floor and place are required"));
            return;
        }
        ScanRequest request = ScanRequestFactory.create(getApplication(), code.trim().toUpperCase(), symbology, image);
        request.setLocationId(locationId);
        request.setFloorId(floorId);
        request.setPlaceId(placeId);
        pendingTaskId = taskId; pendingRequest = request; pendingImage = image;
        sendPending();
    }

    public void retryLastScan() {
        if (pendingCorrection != null) sendPendingCorrection();
        else if (pendingRequest != null) sendPending();
    }

    public void correctAsset(Long taskId, Long currentScanId, Long locationId, Long floorId, Long placeId,
                             String reason, String symbology, byte[] image) {
        ScanCorrectionRequest request = ScanRequestFactory.correction(getApplication(), reason, symbology, image);
        request.setLocationId(locationId); request.setFloorId(floorId); request.setPlaceId(placeId);
        pendingCorrectionTaskId = taskId; pendingCurrentScanId = currentScanId;
        pendingCorrection = request; pendingCorrectionImage = image;
        sendPendingCorrection();
    }

    private void sendPending() {
        scanState.setValue(Resource.loading());
        scanRepository.scan(pendingTaskId, pendingRequest, pendingImage, scanCallback(true));
    }

    private void sendPendingCorrection() {
        scanState.setValue(Resource.loading());
        scanRepository.correct(pendingCorrectionTaskId, pendingCurrentScanId, pendingCorrection,
                pendingCorrectionImage, scanCallback(false));
    }

    private ScanRepository.CallbackResult scanCallback(boolean clearOnSuccess) {
        return new ScanRepository.CallbackResult() {
            @Override public void onSuccess(ScanResponse response) {
                if (clearOnSuccess) { pendingTaskId = null; pendingRequest = null; pendingImage = null; }
                else { pendingCorrectionTaskId = null; pendingCurrentScanId = null; pendingCorrection = null; pendingCorrectionImage = null; }
                scanState.setValue(Resource.success(response));
            }
            @Override public void onError(int code, String message) {
                if (code > 0) {
                    if (clearOnSuccess) { pendingTaskId = null; pendingRequest = null; pendingImage = null; }
                    else { pendingCorrectionTaskId = null; pendingCurrentScanId = null; pendingCorrection = null; pendingCorrectionImage = null; }
                }
                scanState.setValue(Resource.error((code > 0 ? "[" + code + "] " : "") + message));
            }
        };
    }

}
