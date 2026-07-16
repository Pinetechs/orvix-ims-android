package com.pinetechs.orvix.ims.android.scan.presentation.common;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanItemSummary;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanLocationSummary;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ScanResultDialog {
    public interface Callback {
        void onCorrect();
        void onDismiss();
    }

    private ScanResultDialog() { }

    public static Dialog show(Activity activity, ScanResponse response, String message, Callback callback) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_scan_result);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        String result = response.getResultCode() == null ? activity.getString(R.string.recorded_status) : response.getResultCode();
        boolean attention = needsAttention(result) || response.isCorrectionAllowed();
        int color = colorFor(result);

        TextView icon = dialog.findViewById(R.id.resultIconTextView);
        TextView status = dialog.findViewById(R.id.resultStatusTextView);
        TextView title = dialog.findViewById(R.id.resultTitleTextView);
        TextView details = dialog.findViewById(R.id.resultDetailsTextView);
        TextView location = dialog.findViewById(R.id.resultLocationTextView);
        TextView countdown = dialog.findViewById(R.id.autoCloseTextView);
        Button correct = dialog.findViewById(R.id.correctResultButton);
        Button close = dialog.findViewById(R.id.closeResultButton);

        icon.setText(attention ? "!" : "✓");
        icon.setTextColor(ContextCompat.getColor(activity, color));
        status.setText(result.replace('_', ' '));
        status.setTextColor(ContextCompat.getColor(activity, color));
        title.setText(itemTitle(activity, response.getItem()));
        details.setText(joinNonBlank(message, itemDetails(activity, response.getItem())));
        String locationText = locationDetails(activity, response.getActualLocation());
        location.setText(locationText);
        location.setVisibility(locationText.isEmpty() ? View.GONE : View.VISIBLE);

        boolean canCorrect = response.isCorrectionAllowed() && response.getCurrentAcceptedScanId() != null;
        correct.setVisibility(canCorrect ? View.VISIBLE : View.GONE);
        correct.setOnClickListener(v -> {
            callback.onCorrect();
            dialog.dismiss();
        });
        
        close.setText(attention ? activity.getString(R.string.close_label) : activity.getString(R.string.continue_label));
        close.setOnClickListener(v -> dialog.dismiss());
        countdown.setVisibility(attention ? View.GONE : View.VISIBLE);
        countdown.setText(activity.getString(R.string.ready_next_scan, 3));
        dialog.setOnDismissListener(ignored -> callback.onDismiss());

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.72f;
            window.setAttributes(params);
        }

        if (!attention) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!activity.isFinishing() && dialog.isShowing()) dialog.dismiss();
            }, 2800L);
        }
        return dialog;
    }

    private static boolean needsAttention(String result) {
        String value = result.toUpperCase(Locale.ROOT);
        return value.contains("MISMATCH") || value.contains("CONFLICT")
                || value.contains("DUPLICATE") || value.contains("ALREADY")
                || value.contains("REVIEW") || value.contains("NOT_IN_TASK")
                || value.contains("WARNING") || value.contains("FAIL");
    }

    private static int colorFor(String result) {
        String value = result.toUpperCase(Locale.ROOT);
        if (value.contains("MISMATCH") || value.contains("FAIL")) return R.color.danger;
        if (needsAttention(value)) return R.color.warning;
        return R.color.success;
    }

    public static String itemTitle(android.content.Context context, ScanItemSummary item) {
        if (item == null) return context.getString(R.string.item_found);
        if (notBlank(item.getDisplayName())) return item.getDisplayName();
        String vehicle = joinNonBlank(item.getMake(), item.getModel());
        if (!vehicle.isEmpty()) return vehicle;
        if (notBlank(item.getCode())) return item.getCode();
        if (notBlank(item.getBarcode())) return item.getBarcode();
        return context.getString(R.string.item_found);
    }

    public static String itemDetails(android.content.Context context, ScanItemSummary item) {
        if (item == null) return "";
        List<String> values = new ArrayList<>();
        add(values, item.getCode());
        if (!same(item.getBarcode(), item.getCode())) add(values, item.getBarcode());
        add(values, item.getCategory());
        add(values, item.getType());
        add(values, item.getBrand());
        add(values, item.getMake());
        add(values, item.getModel());
        if (item.getModelYear() != null) values.add(String.valueOf(item.getModelYear()));
        add(values, item.getColor());
        add(values, item.getCondition());
        if (item.getCountedQuantity() != null) {
            values.add(context.getString(R.string.quantity_label, item.getCountedQuantity().toPlainString()));
        }
        return String.join("  •  ", values);
    }

    public static String locationDetails(android.content.Context context, ScanLocationSummary location) {
        if (location == null) return "";
        List<String> values = new ArrayList<>();
        add(values, location.getBranchName());
        add(values, location.getLocationName());
        if (!same(location.getLocationCode(), location.getLocationName())) add(values, location.getLocationCode());
        add(values, location.getFloorName());
        add(values, location.getPlaceName());
        return values.isEmpty() ? "" : context.getString(R.string.location_label, String.join(" / ", values));
    }

    private static String joinNonBlank(String first, String second) {
        if (!notBlank(first)) return second == null ? "" : second;
        if (!notBlank(second)) return first;
        return first + "\n\n" + second;
    }

    private static void add(List<String> target, String value) {
        if (notBlank(value) && !target.contains(value.trim())) target.add(value.trim());
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static boolean same(String left, String right) {
        return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
    }
}
