package com.pinetechs.orvix.ims.android.recheck.presentation.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckIssueResponse;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckItemResponse;
import com.pinetechs.orvix.ims.android.recheck.presentation.RecheckUiText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecheckItemAdapter
        extends RecyclerView.Adapter<RecheckItemAdapter.ViewHolder> {

    public interface Listener {
        void onOpen(RecheckItemResponse item);
    }

    private final List<RecheckItemResponse> items = new ArrayList<>();
    private final Listener listener;
    private boolean requestOpen;

    public RecheckItemAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<RecheckItemResponse> data, boolean requestOpen) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        this.requestOpen = requestOpen;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recheck_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView code;
        private final TextView description;
        private final TextView issue;
        private final TextView status;
        private final TextView expectedLocation;
        private final TextView previousResult;
        private final TextView result;
        private final TextView action;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.itemCodeTextView);
            description = itemView.findViewById(R.id.itemDescriptionTextView);
            issue = itemView.findViewById(R.id.issueTextView);
            status = itemView.findViewById(R.id.itemStatusChipTextView);
            expectedLocation = itemView.findViewById(R.id.expectedLocationTextView);
            previousResult = itemView.findViewById(R.id.previousResultTextView);
            result = itemView.findViewById(R.id.submittedResultTextView);
            action = itemView.findViewById(R.id.itemActionTextView);
        }

        void bind(RecheckItemResponse item) {
            code.setText(RecheckUiText.valueOrDash(item.getItemCode()));
            description.setText(RecheckUiText.valueOrDash(item.getItemDescription()));
            description.setVisibility(
                    item.getItemDescription() == null ? View.GONE : View.VISIBLE);
            issue.setText(issueLabels(item.getIssues()));
            status.setText(RecheckUiText.status(itemView.getContext(), item.getStatus()));
            styleStatus(item.getStatus());
            expectedLocation.setText(itemView.getContext().getString(
                    R.string.recheck_expected_location_value,
                    RecheckUiText.valueOrDash(item.getExpectedLocation())
            ));
            previousResult.setText(itemView.getContext().getString(
                    R.string.recheck_previous_result_value,
                    RecheckUiText.valueOrDash(item.getPreviousResult())
            ));

            boolean hasResult = item.getResult() != null;
            result.setVisibility(hasResult ? View.VISIBLE : View.GONE);
            if (hasResult) {
                result.setText(itemView.getContext().getString(
                        R.string.recheck_submitted_result_value,
                        RecheckUiText.result(itemView.getContext(), item.getResult())
                ));
            }

            boolean clickable = requestOpen && item.isPending();
            action.setVisibility(clickable ? View.VISIBLE : View.GONE);
            itemView.setAlpha(clickable || item.isPending() ? 1f : 0.78f);
            itemView.setOnClickListener(clickable ? v -> {
                if (listener != null) listener.onOpen(item);
            } : null);
        }

        private String issueLabels(List<RecheckIssueResponse> issues) {
            if (issues == null || issues.isEmpty()) {
                return itemView.getContext().getString(R.string.recheck_issue);
            }
            StringBuilder value = new StringBuilder();
            for (RecheckIssueResponse issueItem : issues) {
                if (issueItem == null) continue;
                if (value.length() > 0) value.append(" • ");
                value.append(RecheckUiText.issue(
                        itemView.getContext(), issueItem.getIssueType()));
            }
            return value.length() == 0
                    ? itemView.getContext().getString(R.string.recheck_issue)
                    : value.toString();
        }

        private void styleStatus(String value) {
            String normalized = value == null
                    ? ""
                    : value.toUpperCase(Locale.ROOT);
            if ("ACCEPTED".equals(normalized)) {
                status.setBackgroundResource(R.drawable.bg_chip_success);
                status.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.success));
            } else if ("REJECTED".equals(normalized)
                    || "CANCELLED".equals(normalized)) {
                status.setBackgroundResource(R.drawable.bg_chip_danger);
                status.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.danger));
            } else if ("SUBMITTED".equals(normalized)) {
                status.setBackgroundResource(R.drawable.bg_chip_blue);
                status.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.orvix_primary));
            } else {
                status.setBackgroundResource(R.drawable.bg_chip_warning);
                status.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.warning));
            }
        }
    }
}
