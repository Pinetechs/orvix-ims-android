package com.pinetechs.orvix.ims.android.workarea.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.workarea.data.dto.WorkAreaResponse;
import java.util.ArrayList;
import java.util.List;

public class WorkAreaAdapter extends RecyclerView.Adapter<WorkAreaAdapter.ViewHolder> {
    private final List<WorkAreaResponse> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public WorkAreaAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<WorkAreaResponse> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_work_area, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView, codeTextView, progressTextView, recordsCountTextView;
        private final LinearProgressIndicator progressBar;
        private final ImageView iconView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            codeTextView = itemView.findViewById(R.id.codeTextView);
            progressTextView = itemView.findViewById(R.id.progressTextView);
            recordsCountTextView = itemView.findViewById(R.id.recordsCountTextView);
            progressBar = itemView.findViewById(R.id.progressBar);
            iconView = itemView.findViewById(R.id.locationIcon);
        }

        void bind(WorkAreaResponse item) {
            nameTextView.setText(item.getName());
            codeTextView.setText(item.getCode());
            
            int progress = item.getProgress() != null ? item.getProgress() : 0;
            progressTextView.setText(progress + "%");
            progressBar.setProgress(progress);

            String records = (item.getProcessedRecords() != null ? item.getProcessedRecords() : 0) 
                           + " / " + (item.getTotalRecords() != null ? item.getTotalRecords() : 0);
            recordsCountTextView.setText(records);

            // Set icon based on type/domain if needed
            applyIcon(item.getType());

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        private void applyIcon(String type) {
            if (type == null) return;
            switch (type.toUpperCase()) {
                case "SPARE_PART":
                    iconView.setImageResource(R.drawable.ic_business_24);
                    break;
                case "ASSET":
                    iconView.setImageResource(R.drawable.ic_shield_24);
                    break;
                default:
                    iconView.setImageResource(R.drawable.ic_location_24);
                    break;
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(WorkAreaResponse item);
    }
}
