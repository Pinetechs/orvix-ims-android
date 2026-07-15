package com.pinetechs.orvix.ims.android.scan.presentation.sparepart;

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
import com.pinetechs.orvix.ims.android.workarea.data.WorkAreaRepository;
import com.pinetechs.orvix.ims.android.workarea.data.dto.HierarchyOptionResponse;

import java.math.BigDecimal;
import java.util.List;

public class SparePartScanViewModel extends AndroidViewModel {
    private final ScanRepository scanRepository;
    private final WorkAreaRepository hierarchyRepository;
    private final MutableLiveData<Resource<ScanResponse>> scanState = new MutableLiveData<>(Resource.idle());
    private final MutableLiveData<Resource<List<HierarchyOptionResponse>>> locationsState = new MutableLiveData<>(Resource.idle());
    private Long pendingTaskId;
    private ScanRequest pendingRequest;
    private byte[] pendingImage;
    private Long pendingCorrectionTaskId;
    private Long pendingCurrentScanId;
    private ScanCorrectionRequest pendingCorrection;
    private byte[] pendingCorrectionImage;

    public SparePartScanViewModel(@NonNull Application app) {
        super(app);
        scanRepository = new ScanRepository(app);
        hierarchyRepository = new WorkAreaRepository(app);
    }

    public LiveData<Resource<ScanResponse>> getScanState() { return scanState; }
    public LiveData<Resource<List<HierarchyOptionResponse>>> getLocationsState() { return locationsState; }

    public void loadLocations(Long taskId, Long branchId) {
        locationsState.setValue(Resource.loading());
        hierarchyRepository.getSpareLocations(taskId, branchId,
                new WorkAreaRepository.RepositoryCallback<List<HierarchyOptionResponse>>() {
                    @Override public void onSuccess(List<HierarchyOptionResponse> data) { locationsState.setValue(Resource.success(data)); }
                    @Override public void onError(String message) { locationsState.setValue(Resource.error(message)); }
                });
    }

    public void scanSparePart(Long taskId, String code, Long branchId, Long locationId,
                              BigDecimal quantity, String symbology, byte[] image) {
        if (taskId == null || branchId == null || locationId == null || quantity == null
                || code == null || code.trim().isEmpty()) {
            scanState.setValue(Resource.error("Task, barcode, branch, location and quantity are required"));
            return;
        }
        ScanRequest request = ScanRequestFactory.create(getApplication(), code.trim().toUpperCase(), symbology, image);
        request.setBranchId(branchId); request.setLocationId(locationId); request.setCountedQty(quantity);
        pendingTaskId = taskId; pendingRequest = request; pendingImage = image;
        sendPending();
    }

    public void retryLastScan() {
        if (pendingCorrection != null) sendPendingCorrection();
        else if (pendingRequest != null) sendPending();
    }

    public void correctSparePart(Long taskId, Long currentScanId, Long branchId, Long locationId,
                                 BigDecimal quantity, String reason, String symbology, byte[] image) {
        ScanCorrectionRequest request = ScanRequestFactory.correction(getApplication(), reason, symbology, image);
        request.setBranchId(branchId); request.setLocationId(locationId); request.setCountedQty(quantity);
        pendingCorrectionTaskId = taskId; pendingCurrentScanId = currentScanId;
        pendingCorrection = request; pendingCorrectionImage = image;
        sendPendingCorrection();
    }

    private void sendPending() {
        scanState.setValue(Resource.loading());
        scanRepository.scan(pendingTaskId, pendingRequest, pendingImage, callback(true));
    }

    private void sendPendingCorrection() {
        scanState.setValue(Resource.loading());
        scanRepository.correct(pendingCorrectionTaskId, pendingCurrentScanId, pendingCorrection,
                pendingCorrectionImage, callback(false));
    }

    private ScanRepository.CallbackResult callback(boolean clearOnSuccess) {
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
