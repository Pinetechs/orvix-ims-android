package com.pinetechs.orvix.ims.android.recheck.presentation;

import android.content.Context;

import com.pinetechs.orvix.ims.android.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class RecheckUiText {

    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault());

    private RecheckUiText() {
    }

    public static String status(Context context, String value) {
        if (value == null) return context.getString(R.string.recheck_status_unknown);
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "PENDING" -> context.getString(R.string.recheck_status_pending);
            case "IN_PROGRESS" -> context.getString(R.string.recheck_status_in_progress);
            case "SUBMITTED" -> context.getString(R.string.recheck_status_submitted);
            case "COMPLETED" -> context.getString(R.string.recheck_status_completed);
            case "CANCELLED" -> context.getString(R.string.recheck_status_cancelled);
            case "ACCEPTED" -> context.getString(R.string.recheck_item_accepted);
            case "REJECTED" -> context.getString(R.string.recheck_item_rejected);
            default -> humanize(value);
        };
    }

    public static String domain(Context context, String value) {
        if (value == null) return "-";
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "VEHICLE" -> context.getString(R.string.domain_vehicle);
            case "ASSET" -> context.getString(R.string.domain_asset);
            case "SPARE_PART" -> context.getString(R.string.domain_spare_part);
            default -> humanize(value);
        };
    }

    public static String issue(Context context, String value) {
        if (value == null) return context.getString(R.string.recheck_issue);
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "NOT_PROCESSED" -> context.getString(R.string.recheck_issue_not_processed);
            case "LOCATION_MISMATCH" -> context.getString(R.string.recheck_issue_location_mismatch);
            case "QUANTITY_SHORTAGE" -> context.getString(R.string.recheck_issue_shortage);
            case "QUANTITY_OVERAGE" -> context.getString(R.string.recheck_issue_overage);
            case "CONFLICT" -> context.getString(R.string.recheck_issue_conflict);
            case "EXTRA" -> context.getString(R.string.recheck_issue_extra);
            case "AMBIGUOUS" -> context.getString(R.string.recheck_issue_ambiguous);
            default -> humanize(value);
        };
    }

    public static String result(Context context, String value) {
        if (value == null) return "-";
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "FOUND_MATCHED" -> context.getString(R.string.recheck_result_found_matched);
            case "FOUND_DIFFERENT_LOCATION" ->
                    context.getString(R.string.recheck_result_different_location);
            case "QUANTITY_CONFIRMED" ->
                    context.getString(R.string.recheck_result_quantity_confirmed);
            case "QUANTITY_DIFFERENT" ->
                    context.getString(R.string.recheck_result_quantity_different);
            case "NOT_FOUND" -> context.getString(R.string.recheck_result_not_found);
            case "UNABLE_TO_VERIFY" ->
                    context.getString(R.string.recheck_result_unable);
            default -> humanize(value);
        };
    }

    public static String date(String value) {
        if (value == null || value.trim().isEmpty()) return "-";
        try {
            return LocalDateTime.parse(value).format(DISPLAY_DATE);
        } catch (DateTimeParseException ignored) {
            return value.replace('T', ' ');
        }
    }

    public static String humanize(String value) {
        if (value == null) return "-";
        String normalized = value.trim().replace('_', ' ').toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return "-";
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    public static String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
