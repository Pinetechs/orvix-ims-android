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

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNumberTextView = itemView.findViewById(R.id.taskNumberTextView);
            companyTextView = itemView.findViewById(R.id.companyTextView);
            domainStatusTextView = itemView.findViewById(R.id.domainStatusTextView);
            countersTextView = itemView.findViewById(R.id.countersTextView);
        }

        void bind(AppInventoryTaskResponse item) {
            taskNumberTextView.setText(item.getTaskNumber() != null ? item.getTaskNumber() : "Task");
            companyTextView.setText(item.getCompanyName() != null ? item.getCompanyName() : "-");
            domainStatusTextView.setText((item.getInventoryDomain() != null ? item.getInventoryDomain() : "-")
                    + " • "
                    + (item.getStatus() != null ? item.getStatus() : "-"));
            countersTextView.setText("Planned: " + item.getPlannedRecords()
                    + " | Scanned: " + item.getScannedRecords()
                    + " | Mismatch: " + item.getMismatchRecords());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(item);
                }
            });
        }
    }

    public interface OnTaskClickListener {
        void onTaskClick(AppInventoryTaskResponse item);
    }
}
