package com.pinetechs.orvix.ims.android.location.presentation.vehicle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.location.data.vehicle.VehicleLocationResponse;
import java.util.ArrayList;
import java.util.List;

public class VehicleLocationAdapter extends RecyclerView.Adapter<VehicleLocationAdapter.ViewHolder> {
    private final List<VehicleLocationResponse> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public VehicleLocationAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<VehicleLocationResponse> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_vehicle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView, codeTextView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            codeTextView = itemView.findViewById(R.id.codeTextView);
        }
        void bind(VehicleLocationResponse item) {
            nameTextView.setText(item.getName());
            codeTextView.setText(item.getCode());
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(VehicleLocationResponse item);
    }
}
