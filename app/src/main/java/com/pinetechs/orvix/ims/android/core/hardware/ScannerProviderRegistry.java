package com.pinetechs.orvix.ims.android.core.hardware;

import android.content.Context;

import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.hardware.urovo.UrovoScannerProvider;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provider registry. Adding another scanner SDK only requires registering a new
 * provider; domain screens and profile settings remain unchanged.
 */
public final class ScannerProviderRegistry {

    private static final List<ScannerProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    static {
        register(new UrovoScannerProvider());
    }

    private ScannerProviderRegistry() {
    }

    public static void register(ScannerProvider provider) {
        if (provider == null) return;
        for (ScannerProvider existing : PROVIDERS) {
            if (existing.getVendorName().equalsIgnoreCase(provider.getVendorName())) return;
        }
        PROVIDERS.add(provider);
    }

    static ScannerInterface create(
            Context context,
            ScannerProfile profile,
            ScannerDeviceInfo deviceInfo
    ) {
        for (ScannerProvider provider : PROVIDERS) {
            if (provider.supports(deviceInfo)) {
                return provider.create(context, profile);
            }
        }
        return new UnsupportedScannerManager(profile);
    }

    public static String resolveVendorName(ScannerDeviceInfo deviceInfo) {
        for (ScannerProvider provider : PROVIDERS) {
            if (provider.supports(deviceInfo)) return provider.getVendorName();
        }
        return "UNSUPPORTED";
    }
}
