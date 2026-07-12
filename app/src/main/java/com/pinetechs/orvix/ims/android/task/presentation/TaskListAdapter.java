package com.pinetechs.orvix.ims.android.task.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

        private final TextView taskNameTextView;
        private final TextView taskNumberTextView;
        private final TextView companyTextView;
        private final TextView domainChipTextView;
        private final TextView taskStatusChipTextView;
        private final TextView progressTextView;
        private final TextView recordsCountTextView;
        private final TextView descriptionTextView;
        private final ImageView taskIconView;
        private final com.google.android.material.progressindicator.LinearProgressIndicator taskProgressBar;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNameTextView = itemView.findViewById(R.id.taskNameTextView);
            taskNumberTextView = itemView.findViewById(R.id.taskNumberTextView);
            companyTextView = itemView.findViewById(R.id.companyTextView);
            domainChipTextView = itemView.findViewById(R.id.domainChipTextView);
            taskStatusChipTextView = itemView.findViewById(R.id.taskStatusChipTextView);
            progressTextView = itemView.findViewById(R.id.progressTextView);
            recordsCountTextView = itemView.findViewById(R.id.recordsCountTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            taskIconView = itemView.findViewById(R.id.taskIconView);
            taskProgressBar = itemView.findViewById(R.id.taskProgressBar);
        }

        void bind(AppInventoryTaskResponse item) {
            taskNameTextView.setText(item.getTaskName() != null ? item.getTaskName() : "-");
            taskNumberTextView.setText(item.getTaskNumber() != null ? item.getTaskNumber() : "-");
            companyTextView.setText(item.getCompanyName() != null ? item.getCompanyName() : "-");
            domainChipTextView.setText(item.getInventoryDomain() != null ? item.getInventoryDomain() : "-");
            descriptionTextView.setText(item.getDescription() != null ? item.getDescription() : "");
            
            int progress = (int) item.getProgress();
            progressTextView.setText(progress + "%");
            taskProgressBar.setProgress(progress);

            String records = item.getProcessedRecords() + " / " + item.getTotalRecords();
            recordsCountTextView.setText(records);

            String status = item.getStatus() != null ? item.getStatus() : "UNKNOWN";
            taskStatusChipTextView.setText(status.replace('_', ' '));
            applyStatusStyle(taskStatusChipTextView, status);
            applyDomainIcon(taskIconView, item.getInventoryDomain());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(item);
                }
            });
        }

        private void applyDomainIcon(ImageView iconView, String domain) {
            if (domain == null) return;
            switch (domain.toUpperCase()) {
                case "VEHICLE":
                    iconView.setImageResource(R.drawable.ic_car_24);
                    break;
                case "SPARE_PART":
                    iconView.setImageResource(R.drawable.ic_inventory_24);
                    break;
                case "ASSET":
                    iconView.setImageResource(R.drawable.ic_business_24);
                    break;
                default:
                    iconView.setImageResource(R.drawable.ic_inventory_24);
                    break;
            }
        }

        private void applyStatusStyle(TextView chip, String status) {
            String normalized = status != null ? status.toUpperCase() : "";
            if (normalized.contains("COMPLETED")) {
                chip.setBackgroundResource(R.drawable.bg_chip_success);
                chip.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success));
            } else if (normalized.contains("IN_PROGRESS")) {
                chip.setBackgroundResource(R.drawable.bg_chip_purple);
                chip.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple));
            } else if (normalized.contains("READY")) {
                chip.setBackgroundResource(R.drawable.bg_chip_blue);
                chip.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.orvix_primary));
            } else if (normalized.contains("CANCEL") || normalized.contains("REJECT")) {
                chip.setBackgroundResource(R.drawable.bg_chip_danger);
                chip.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.danger));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_warning);
                chip.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.warning));
            }
        }
    }

    public interface OnTaskClickListener {
        void onTaskClick(AppInventoryTaskResponse item);
    }
}
