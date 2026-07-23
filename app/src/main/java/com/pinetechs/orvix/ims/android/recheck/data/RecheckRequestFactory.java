package com.pinetechs.orvix.ims.android.recheck.data;

import android.content.Context;

import com.pinetechs.orvix.ims.android.core.util.DeviceUtils;
import com.pinetechs.orvix.ims.android.recheck.data.dto.SubmitRecheckItemRequest;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

public final class RecheckRequestFactory {

    private RecheckRequestFactory() {
    }

    public static SubmitRecheckItemRequest create(
            Context context,
            String result,
            String scannedCode,
            String symbology,
            String imageSource
    ) {
        SubmitRecheckItemRequest request = new SubmitRecheckItemRequest();
        request.setClientSubmissionId(UUID.randomUUID().toString());
        request.setResult(result);
        request.setScannedCode(normalizeCode(scannedCode));
        request.setDeviceScannedAt(LocalDateTime.now().toString());
        request.setDeviceId(DeviceUtils.getDeviceId(context));
        request.setSymbology(valueOrUnknown(symbology));
        request.setImageSource(valueOrUnknown(imageSource));
        return request;
    }

    private static String normalizeCode(String value) {
        return value == null || value.trim().isEmpty()
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String valueOrUnknown(String value) {
        return value == null || value.trim().isEmpty()
                ? "UNKNOWN"
                : value.trim();
    }
}
