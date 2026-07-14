package com.pinetechs.orvix.ims.android.scan.presentation.sparepart;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

public class SparePartScanViewModel extends AndroidViewModel {
    private final MutableLiveData<Resource<ScanResponse>> scanState = new MutableLiveData<>(Resource.idle());

    public SparePartScanViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Resource<ScanResponse>> getScanState() {
        return scanState;
    }

    public void scanSparePart(Long taskId, String barcode, String branchCode) {
        // To be implemented with SparePartScanRepository
    }
}
