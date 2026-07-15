package com.pinetechs.orvix.ims.android.core.hardware.urovo;

import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Symbology;
import android.device.scanner.configuration.Triggering;
import android.util.Log;

import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfileDefaults;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfileSettings;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologySettings;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Applies Orvix standard settings and vendor-neutral profiles to UROVO ScanManager. */
final class UrovoScannerConfigurator {

    private static final String TAG = "UrovoScannerConfig";
    private static final int OUTPUT_MODE_INTENT = 0;
    private static final Set<Integer> REJECTED_PROPERTY_IDS =
            Collections.synchronizedSet(new HashSet<>());

    private UrovoScannerConfigurator() {
    }

    static boolean applyStandardSettings(ScanManager scanManager, SessionManager sessionManager) {
        if (scanManager == null || sessionManager == null) return false;

        boolean outputModeApplied = true;
        if (scanManager.getOutputMode() != OUTPUT_MODE_INTENT) {
            outputModeApplied = scanManager.switchOutputMode(OUTPUT_MODE_INTENT);
        }

        scanManager.setTriggerMode(Triggering.HOST);
        scanManager.unlockTrigger();

        applyIntParameter(
                scanManager,
                PropertyID.SEND_GOOD_READ_BEEP_ENABLE,
                sessionManager.getScannerBeepMode().getUrovoValue(),
                "intent beep"
        );
        applyIntParameter(
                scanManager,
                PropertyID.SEND_GOOD_READ_VIBRATE_ENABLE,
                sessionManager.isScannerVibrationEnabled() ? 1 : 0,
                "intent vibration"
        );
        applyIntParameter(scanManager, PropertyID.WEDGE_KEYBOARD_ENABLE, 0, "keyboard wedge");
        applyIntParameter(scanManager, PropertyID.LABEL_APPEND_ENTER, 0, "append enter");

        boolean intentSettingsApplied = scanManager.setParameterString(
                new int[]{
                        PropertyID.WEDGE_INTENT_ACTION_NAME,
                        PropertyID.WEDGE_INTENT_DATA_STRING_TAG,
                        PropertyID.WEDGE_INTENT_LABEL_TYPE_TAG
                },
                new String[]{
                        sessionManager.getUrovoIntentAction(),
                        sessionManager.getUrovoDataTag(),
                        sessionManager.getUrovoTypeTag()
                }
        );

        return outputModeApplied && intentSettingsApplied;
    }

    static void applySymbologyDetectionMode(ScanManager scanManager) {
        if (scanManager == null) return;

        ScannerProfileSettings detectionSettings =
                ScannerProfileDefaults.forSymbologyDetection();
        applyProfile(scanManager, ScannerProfile.GENERAL, detectionSettings);
        Log.d(TAG, "Applied permissive symbology detection mode");
    }

    static void applyProfile(
            ScanManager scanManager,
            ScannerProfile profile,
            ScannerProfileSettings settings
    ) {
        if (scanManager == null || settings == null) return;

        scanManager.enableAllSymbologies(false);
        int enabledCount = 0;

        for (Map.Entry<String, ScannerSymbologySettings> entry : settings.getSymbologies().entrySet()) {
            BarcodeSymbology genericType = BarcodeSymbology.fromStorageName(entry.getKey());
            ScannerSymbologySettings symbologySettings = entry.getValue();
            if (genericType == null || symbologySettings == null || !symbologySettings.isEnabled()) {
                continue;
            }

            Symbology urovoType = UrovoSymbologyAdapter.toUrovo(genericType);
            if (urovoType == null) {
                Log.w(TAG, "No UROVO mapping for symbology: " + genericType);
                continue;
            }
            if (!scanManager.isSymbologySupported(urovoType)) {
                Log.w(TAG, "Symbology is not supported by this UROVO scanner: " + genericType);
                continue;
            }

            scanManager.enableSymbology(urovoType, true);
            applySymbologyOptions(scanManager, genericType, symbologySettings, settings.getMinScanLength(), settings.getMaxScanLength());
            enabledCount++;
        }

        Log.d(TAG, "Applied profile " + (profile != null ? profile.name() : "GENERAL")
                + " with " + enabledCount + " supported symbologies");
    }

    private static void applySymbologyOptions(
            ScanManager scanManager,
            BarcodeSymbology symbology,
            ScannerSymbologySettings settings,
            int profileMinLength,
            int profileMaxLength
    ) {
        List<UrovoSymbologyAdapter.PropertyValue> properties =
                UrovoSymbologyAdapter.resolveProperties(symbology, settings);

        // Override or Add length properties from profile defaults
        UrovoSymbologyAdapter.appendLengthProperties(symbology, profileMinLength, profileMaxLength, properties);

        if (properties.isEmpty()) return;

        // Some UROVO firmware versions reject one CODE39 property when all
        // options are submitted as a batch, causing the entire batch to fail.
        // Apply these options independently so supported values still take effect
        // and remember rejected property IDs for the rest of this app process.
        if (symbology == BarcodeSymbology.CODE39) {
            applyPropertiesIndividually(scanManager, symbology, properties);
            return;
        }

        List<Integer> ids = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (UrovoSymbologyAdapter.PropertyValue property : properties) {
            ids.add(property.getPropertyId());
            values.add(property.getValue());
        }

        int[] idArray = new int[ids.size()];
        int[] valueArray = new int[values.size()];
        for (int index = 0; index < ids.size(); index++) {
            idArray[index] = ids.get(index);
            valueArray[index] = values.get(index);
        }

        int result = scanManager.setParameterInts(idArray, valueArray);
        Log.d(TAG, "Applied " + symbology.getStorageName()
                + " options=" + properties.size() + ", result=" + result);
    }

    private static void applyPropertiesIndividually(
            ScanManager scanManager,
            BarcodeSymbology symbology,
            List<UrovoSymbologyAdapter.PropertyValue> properties
    ) {
        int appliedCount = 0;
        int rejectedCount = 0;

        for (UrovoSymbologyAdapter.PropertyValue property : properties) {
            if (REJECTED_PROPERTY_IDS.contains(property.getPropertyId())) {
                rejectedCount++;
                continue;
            }

            try {
                int result = scanManager.setParameterInts(
                        new int[]{property.getPropertyId()},
                        new int[]{property.getValue()}
                );
                if (result == 0) {
                    appliedCount++;
                    continue;
                }

                rejectedCount++;
                REJECTED_PROPERTY_IDS.add(property.getPropertyId());
                Log.w(TAG, "Firmware rejected " + symbology.getStorageName()
                        + " option=" + property.getOptionKey().name()
                        + ", propertyId=" + property.getPropertyId()
                        + ", value=" + property.getValue()
                        + ", result=" + result);
            } catch (RuntimeException exception) {
                rejectedCount++;
                REJECTED_PROPERTY_IDS.add(property.getPropertyId());
                Log.w(TAG, "Firmware failed to apply " + symbology.getStorageName()
                        + " option=" + property.getOptionKey().name()
                        + ", propertyId=" + property.getPropertyId()
                        + ", value=" + property.getValue(), exception);
            }
        }

        Log.d(TAG, "Applied " + symbology.getStorageName()
                + " options individually: applied=" + appliedCount
                + ", rejectedOrSkipped=" + rejectedCount);
    }

    private static void applyIntParameter(
            ScanManager scanManager,
            int propertyId,
            int requestedValue,
            String label
    ) {
        int result = scanManager.setParameterInts(
                new int[]{propertyId},
                new int[]{requestedValue}
        );

        int[] actualValues = scanManager.getParameterInts(new int[]{propertyId});
        Integer actualValue = actualValues != null && actualValues.length > 0
                ? actualValues[0]
                : null;

        Log.d(TAG, "Applied " + label
                + ": requested=" + requestedValue
                + ", actual=" + actualValue
                + ", result=" + result);
    }
}
