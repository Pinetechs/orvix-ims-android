package com.pinetechs.orvix.ims.android.recheck.presentation.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckRequestResponse;
import com.pinetechs.orvix.ims.android.recheck.presentation.RecheckUiText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecheckRequestAdapter
        extends RecyclerView.Adapter<RecheckRequestAdapter.ViewHolder> {

    public interface Listener {
        void onOpen(RecheckRequestResponse request);
    }

    private final List<RecheckRequestResponse> items = new ArrayList<>();
    private final Listener listener;

    public RecheckRequestAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<RecheckRequestResponse> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recheck_request, parent, false);
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

        private final TextView requestNumber;
        private final TextView taskName;
        private final TextView taskReference;
        private final TextView domain;
        private final TextView status;
        private final TextView workArea;
        private final TextView dueAt;
        private final TextView itemProgress;
        private final TextView imageRequired;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestNumber = itemView.findViewById(R.id.requestNumberTextView);
            taskName = itemView.findViewById(R.id.taskNameTextView);
            taskReference = itemView.findViewById(R.id.taskReferenceTextView);
            domain = itemView.findViewById(R.id.domainChipTextView);
            status = itemView.findViewById(R.id.statusChipTextView);
            workArea = itemView.findViewById(R.id.workAreaTextView);
            dueAt = itemView.findViewById(R.id.dueAtTextView);
            itemProgress = itemView.findViewById(R.id.itemProgressTextView);
            imageRequired = itemView.findViewById(R.id.imageRequiredTextView);
        }

        void bind(RecheckRequestResponse item) {
            requestNumber.setText(RecheckUiText.valueOrDash(item.getRequestNumber()));
            taskName.setText(RecheckUiText.valueOrDash(item.getTaskName()));
            taskReference.setText(itemView.getContext().getString(
                    R.string.recheck_task_reference,
                    RecheckUiText.valueOrDash(item.getTaskNumber())
            ));
            domain.setText(RecheckUiText.domain(
                    itemView.getContext(), item.getInventoryDomain()));
            status.setText(RecheckUiText.status(itemView.getContext(), item.getStatus()));
            applyStatusStyle(item.getStatus());
            workArea.setText(RecheckUiText.valueOrDash(item.getWorkAreaLabel()));
            dueAt.setText(itemView.getContext().getString(
                    R.string.recheck_due_value,
                    RecheckUiText.date(item.getDueAt())
            ));
            itemProgress.setText(itemView.getContext().getString(
                    R.string.recheck_items_progress,
                    item.submittedItemCount(),
                    item.getItems().size()
            ));
            imageRequired.setVisibility(
                    item.isImageRequired() ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOpen(item);
            });
        }

        private void applyStatusStyle(String value) {
            String normalized = value == null
                    ? ""
                    : value.toUpperCase(Locale.ROOT);
            if ("SUBMITTED".equals(normalized)) {
                status.setBackgroundResource(R.drawable.bg_chip_success);
                status.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.success));
            } else if ("IN_PROGRESS".equals(normalized)) {
                status.setBackgroundResource(R.drawable.bg_chip_purple);
                status.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.purple));
            } else {
                status.setBackgroundResource(R.drawable.bg_chip_warning);
                status.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.warning));
            }
        }
    }
}
