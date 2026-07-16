package com.pinetechs.orvix.ims.android.workarea.presentation;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.workarea.data.dto.HierarchyOptionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HierarchyOptionAdapter extends RecyclerView.Adapter<HierarchyOptionAdapter.OptionHolder> {
    public interface Listener {
        void onOpen(HierarchyOptionResponse item);
        void onComplete(HierarchyOptionResponse item);
    }

    private final Listener listener;
    private final List<HierarchyOptionResponse> items = new ArrayList<>();

    public HierarchyOptionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<HierarchyOptionResponse> values) {
        items.clear();
        if (values != null) items.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OptionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hierarchy_option, parent, false);
        return new OptionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class OptionHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView code;
        private final TextView status;
        private final TextView count;
        private final View dot;
        private final Button complete;

        OptionHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.optionNameTextView);
            code = itemView.findViewById(R.id.optionCodeTextView);
            status = itemView.findViewById(R.id.progressStatusTextView);
            count = itemView.findViewById(R.id.scanCountTextView);
            dot = itemView.findViewById(R.id.statusDotView);
            complete = itemView.findViewById(R.id.completeLocationButton);
        }

        void bind(HierarchyOptionResponse item) {
            android.content.Context context = itemView.getContext();
            name.setText(nonBlank(item.getName(), nonBlank(item.getCode(), context.getString(R.string.unnamed_location))));
            String itemCode = item.getCode();
            code.setText(itemCode == null || itemCode.trim().isEmpty() ? "" : itemCode);
            code.setVisibility(itemCode == null || itemCode.trim().isEmpty() ? View.GONE : View.VISIBLE);
            
            if (item.getScanCount() == 1) {
                count.setText(context.getString(R.string.scan_count_single));
            } else {
                count.setText(context.getString(R.string.scan_count_plural, item.getScanCount()));
            }

            String progress = item.getProgressStatus() == null
                    ? "NOT_STARTED" : item.getProgressStatus().toUpperCase(Locale.ROOT);
            int color;
            int background;
            int labelRes;
            switch (progress) {
                case "COMPLETED":
                    color = R.color.success;
                    background = R.drawable.bg_chip_success;
                    labelRes = R.string.status_completed;
                    break;
                case "REVIEW_REQUIRED":
                    color = R.color.warning;
                    background = R.drawable.bg_chip_warning;
                    labelRes = R.string.status_review_required;
                    break;
                case "IN_PROGRESS":
                    color = R.color.orvix_primary;
                    background = R.drawable.bg_chip_blue;
                    labelRes = R.string.status_in_progress;
                    break;
                default:
                    color = R.color.orvix_text_muted;
                    background = R.drawable.bg_card_soft;
                    labelRes = R.string.status_not_started;
                    break;
            }
            status.setText(labelRes);
            status.setTextColor(ContextCompat.getColor(context, color));
            status.setBackgroundResource(background);
            dot.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, color)));

            boolean canComplete = item.isCompletionEnabled() && item.isCanComplete();
            complete.setVisibility(canComplete ? View.VISIBLE : View.GONE);
            complete.setOnClickListener(v -> listener.onComplete(item));
            itemView.setOnClickListener(v -> listener.onOpen(item));
        }

        private String nonBlank(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
