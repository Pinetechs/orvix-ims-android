package com.pinetechs.orvix.ims.android.scan.data;

import android.content.Context;

import com.pinetechs.orvix.ims.android.core.util.DeviceUtils;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanCorrectionRequest;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanRequest;

import java.time.LocalDateTime;
import java.util.UUID;

public final class ScanRequestFactory {
    private ScanRequestFactory() {}

    public static ScanRequest create(Context context, String code, String symbology, byte[] image) {
        ScanRequest request = new ScanRequest();
        request.setClientScanId(UUID.randomUUID().toString());
        request.setCode(code);
        request.setDeviceScannedAt(LocalDateTime.now().toString());
        request.setDeviceId(DeviceUtils.getDeviceId(context));
        request.setSymbology(valueOrUnknown(symbology));
        request.setImageSource(hasImage(image) ? "UROVO_SCANNER_SENSOR" : "UNKNOWN");
        return request;
    }

    public static ScanCorrectionRequest correction(Context context, String reason,
                                                    String symbology, byte[] image) {
        ScanCorrectionRequest request = new ScanCorrectionRequest();
        request.setClientScanId(UUID.randomUUID().toString());
        request.setReason(reason);
        request.setDeviceScannedAt(LocalDateTime.now().toString());
        request.setDeviceId(DeviceUtils.getDeviceId(context));
        request.setSymbology(valueOrUnknown(symbology));
        request.setImageSource(hasImage(image) ? "UROVO_SCANNER_SENSOR" : "UNKNOWN");
        return request;
    }

    private static boolean hasImage(byte[] image) { return image != null && image.length > 0; }
    private static String valueOrUnknown(String value) {
        return value == null || value.trim().isEmpty() ? "UNKNOWN" : value.trim();
    }
}
