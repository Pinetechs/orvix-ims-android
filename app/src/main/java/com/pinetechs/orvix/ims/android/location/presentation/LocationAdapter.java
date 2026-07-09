package com.pinetechs.orvix.ims.android.location.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryLocationResponse;

import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private final List<AppInventoryLocationResponse> items = new ArrayList<>();
    private final OnLocationClickListener listener;

    public LocationAdapter(OnLocationClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AppInventoryLocationResponse> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {

        private final TextView codeTextView;
        private final TextView nameTextView;
        private final TextView descriptionTextView;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            codeTextView = itemView.findViewById(R.id.locationCodeTextView);
            nameTextView = itemView.findViewById(R.id.locationNameTextView);
            descriptionTextView = itemView.findViewById(R.id.locationDescriptionTextView);
        }

        void bind(AppInventoryLocationResponse item) {
            String code = item.getCode() != null ? item.getCode() : "-";
            String name = item.getName() != null ? item.getName() : "Location";
            String description = item.getDescription() != null && !item.getDescription().trim().isEmpty()
                    ? item.getDescription()
                    : "Ready for inventory scanning";

            codeTextView.setText(code);
            nameTextView.setText(name);
            descriptionTextView.setText(description);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLocationClick(item);
                }
            });
        }
    }

    public interface OnLocationClickListener {
        void onLocationClick(AppInventoryLocationResponse item);
    }
}
