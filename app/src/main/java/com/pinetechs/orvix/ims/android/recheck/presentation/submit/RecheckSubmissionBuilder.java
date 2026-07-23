package com.pinetechs.orvix.ims.android.recheck.presentation.submit;

import android.content.Context;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.recheck.data.RecheckRequestFactory;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckItemResponse;
import com.pinetechs.orvix.ims.android.recheck.data.dto.SubmitRecheckItemRequest;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * Builds and validates the employee's candidate result.
 *
 * <p>Domain decisions live here instead of in the Activity so the UI remains
 * responsible only for collecting input and showing feedback.</p>
 */
public final class RecheckSubmissionBuilder {

    private RecheckSubmissionBuilder() {
    }

    public static BuildResult observed(
            Context context,
            String inventoryDomain,
            RecheckItemResponse item,
            String scannedCode,
            String symbology,
            String imageSource,
            Long selectedFloorId,
            Long selectedPlaceId,
            Long selectedSpareLocationId,
            String quantityText,
            String note
    ) {
        String code = trim(scannedCode);
        if (code == null) {
            return BuildResult.error(
                    context.getString(R.string.recheck_scan_required));
        }
        if (item.getItemCode() == null
                || !code.equalsIgnoreCase(item.getItemCode().trim())) {
            return BuildResult.error(
                    context.getString(R.string.recheck_wrong_barcode));
        }

        String domain = normalize(inventoryDomain);
        String result;
        BigDecimal quantity = null;

        switch (domain) {
            case "VEHICLE" -> {
                if (item.getLocationId() == null) {
                    return BuildResult.error(context.getString(
                            R.string.recheck_location_context_missing));
                }
                result = "FOUND_MATCHED";
            }
            case "ASSET" -> {
                if (item.getLocationId() == null
                        || selectedFloorId == null
                        || selectedPlaceId == null) {
                    return BuildResult.error(context.getString(
                            R.string.recheck_asset_location_required));
                }
                result = Objects.equals(selectedFloorId, item.getFloorId())
                        && Objects.equals(selectedPlaceId, item.getPlaceId())
                        ? "FOUND_MATCHED"
                        : "FOUND_DIFFERENT_LOCATION";
            }
            case "SPARE_PART" -> {
                if (item.getBranchId() == null
                        || selectedSpareLocationId == null) {
                    return BuildResult.error(context.getString(
                            R.string.recheck_spare_location_required));
                }
                QuantityResult quantityResult = parseQuantity(context, quantityText);
                if (quantityResult.error != null) {
                    return BuildResult.error(quantityResult.error);
                }
                quantity = quantityResult.value;
                if (item.getExpectedQuantity() == null) {
                    return BuildResult.error(context.getString(
                            R.string.recheck_expected_quantity_missing));
                }
                result = quantity.compareTo(item.getExpectedQuantity()) == 0
                        ? "QUANTITY_CONFIRMED"
                        : "QUANTITY_DIFFERENT";
            }
            default -> {
                return BuildResult.error(
                        context.getString(R.string.recheck_unknown_domain));
            }
        }

        SubmitRecheckItemRequest request = RecheckRequestFactory.create(
                context,
                result,
                code,
                symbology,
                imageSource
        );
        request.setResolvedItemId(item.getReferenceItemId());
        request.setLocationId(item.getLocationId());
        request.setNote(trim(note));

        if ("ASSET".equals(domain)) {
            request.setFloorId(selectedFloorId);
            request.setPlaceId(selectedPlaceId);
        } else if ("SPARE_PART".equals(domain)) {
            request.setBranchId(item.getBranchId());
            request.setLocationId(selectedSpareLocationId);
            request.setCountedQuantity(quantity);
        }
        return BuildResult.success(request);
    }

    public static BuildResult unavailable(
            Context context,
            String result,
            String reasonCode,
            String note,
            String imageSource
    ) {
        if (trim(reasonCode) == null) {
            return BuildResult.error(
                    context.getString(R.string.recheck_reason_required));
        }
        String normalizedNote = trim(note);
        if (normalizedNote == null) {
            return BuildResult.error(
                    context.getString(R.string.recheck_note_required));
        }

        SubmitRecheckItemRequest request = RecheckRequestFactory.create(
                context,
                result,
                null,
                "MANUAL",
                imageSource
        );
        request.setReasonCode(reasonCode);
        request.setNote(normalizedNote);
        return BuildResult.success(request);
    }

    private static QuantityResult parseQuantity(
            Context context,
            String text
    ) {
        String value = trim(text);
        if (value == null) {
            return QuantityResult.error(
                    context.getString(R.string.recheck_quantity_required));
        }
        try {
            BigDecimal quantity = new BigDecimal(value);
            if (quantity.signum() < 0 || quantity.scale() > 3) {
                return QuantityResult.error(
                        context.getString(R.string.recheck_quantity_invalid));
            }
            return QuantityResult.success(quantity);
        } catch (NumberFormatException exception) {
            return QuantityResult.error(
                    context.getString(R.string.recheck_quantity_invalid));
        }
    }

    private static String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String trim(String value) {
        return value == null || value.trim().isEmpty()
                ? null
                : value.trim();
    }

    public static final class BuildResult {
        private final SubmitRecheckItemRequest request;
        private final String error;

        private BuildResult(SubmitRecheckItemRequest request, String error) {
            this.request = request;
            this.error = error;
        }

        public static BuildResult success(SubmitRecheckItemRequest request) {
            return new BuildResult(request, null);
        }

        public static BuildResult error(String error) {
            return new BuildResult(null, error);
        }

        public boolean isSuccess() {
            return request != null;
        }

        public SubmitRecheckItemRequest getRequest() {
            return request;
        }

        public String getError() {
            return error;
        }
    }

    private static final class QuantityResult {
        private final BigDecimal value;
        private final String error;

        private QuantityResult(BigDecimal value, String error) {
            this.value = value;
            this.error = error;
        }

        private static QuantityResult success(BigDecimal value) {
            return new QuantityResult(value, null);
        }

        private static QuantityResult error(String error) {
            return new QuantityResult(null, error);
        }
    }
}
