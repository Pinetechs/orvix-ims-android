package com.pinetechs.orvix.ims.android.task.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskResponse;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {

    private final List<AppInventoryTaskResponse> items = new ArrayList<>();
    private final OnTaskClickListener listener;

    public TaskListAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AppInventoryTaskResponse> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        AppInventoryTaskResponse item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final TextView taskNumberTextView;
        private final TextView companyTextView;
        private final TextView domainStatusTextView;
        private final TextView countersTextView;
        private final TextView taskStatusChipTextView;
        private final TextView plannedCountTextView;
        private final TextView scannedCountTextView;
        private final TextView mismatchCountTextView;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNumberTextView = itemView.findViewById(R.id.taskNumberTextView);
            companyTextView = itemView.findViewById(R.id.companyTextView);
            domainStatusTextView = itemView.findViewById(R.id.domainStatusTextView);
            countersTextView = itemView.findViewById(R.id.countersTextView);
            taskStatusChipTextView = itemView.findViewById(R.id.taskStatusChipTextView);
            plannedCountTextView = itemView.findViewById(R.id.plannedCountTextView);
            scannedCountTextView = itemView.findViewById(R.id.scannedCountTextView);
            mismatchCountTextView = itemView.findViewById(R.id.mismatchCountTextView);
        }

        void bind(AppInventoryTaskResponse item) {
            String taskNumber = item.getTaskNumber() != null ? item.getTaskNumber() : "Task";
            String company = item.getCompanyName() != null ? item.getCompanyName() : "-";
            String domain = item.getInventoryDomain() != null ? item.getInventoryDomain() : "-";
            String status = item.getStatus() != null ? item.getStatus() : "-";

            taskNumberTextView.setText(taskNumber);
            companyTextView.setText(company);
            domainStatusTextView.setText(domain);
            countersTextView.setText("Tap to select assigned locations");

            if (taskStatusChipTextView != null) {
                taskStatusChipTextView.setText(formatStatus(status));
                applyStatusStyle(taskStatusChipTextView, status);
            }
            if (plannedCountTextView != null) {
                plannedCountTextView.setText("Planned\n" + item.getPlannedRecords());
            }
            if (scannedCountTextView != null) {
                scannedCountTextView.setText("Scanned\n" + item.getScannedRecords());
            }
            if (mismatchCountTextView != null) {
                mismatchCountTextView.setText("Mismatch\n" + item.getMismatchRecords());
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(item);
                }
            });
        }

        private String formatStatus(String status) {
            if (status == null || status.trim().isEmpty()) {
                return "-";
            }
            return status.replace('_', ' ');
        }

        private void applyStatusStyle(TextView chip, String status) {
            String normalized = status != null ? status.toUpperCase() : "";
            if (normalized.contains("COMPLETED")) {
                chip.setBackgroundResource(R.drawable.bg_chip_success);
                chip.setTextColor(itemView.getResources().getColor(R.color.success));
            } else if (normalized.contains("IN_PROGRESS")) {
                chip.setBackgroundResource(R.drawable.bg_chip_purple);
                chip.setTextColor(itemView.getResources().getColor(R.color.purple));
            } else if (normalized.contains("READY")) {
                chip.setBackgroundResource(R.drawable.bg_chip_blue);
                chip.setTextColor(itemView.getResources().getColor(R.color.orvix_primary));
            } else if (normalized.contains("CANCEL") || normalized.contains("REJECT")) {
                chip.setBackgroundResource(R.drawable.bg_chip_danger);
                chip.setTextColor(itemView.getResources().getColor(R.color.danger));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_warning);
                chip.setTextColor(itemView.getResources().getColor(R.color.warning));
            }
        }
    }

    public interface OnTaskClickListener {
        void onTaskClick(AppInventoryTaskResponse item);
    }
}
