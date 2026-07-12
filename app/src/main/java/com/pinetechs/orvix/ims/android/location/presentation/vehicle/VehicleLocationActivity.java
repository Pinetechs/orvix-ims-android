package com.pinetechs.orvix.ims.android.location.presentation.vehicle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.location.data.vehicle.VehicleLocationResponse;
import com.pinetechs.orvix.ims.android.scan.presentation.ScanActivity;

public class VehicleLocationActivity extends AppCompatActivity {

    private VehicleLocationViewModel viewModel;
    private VehicleLocationAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private TextView titleTextView;

    private Long taskId;
    private String taskNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_vehicle);

        taskId = getIntent().getLongExtra("task_id", -1L);
        if (taskId == -1L) taskId = null;
        taskNumber = getIntent().getStringExtra("task_number");

        viewModel = new ViewModelProvider(this).get(VehicleLocationViewModel.class);

        titleTextView = findViewById(R.id.titleTextView);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = findViewById(R.id.locationsRecyclerView);

        if (taskNumber != null) {
            titleTextView.setText(taskNumber + " - Vehicle Yards");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VehicleLocationAdapter(this::openScanScreen);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadLocations(taskId));

        observeLocations();
        viewModel.loadLocations(taskId);
    }

    private void observeLocations() {
        viewModel.getLocationsState().observe(this, state -> {
            if (state == null) return;

            if (state.getStatus() == Resource.Status.LOADING) {
                if (!swipeRefreshLayout.isRefreshing()) progressBar.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            } else if (state.getStatus() == Resource.Status.SUCCESS) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                adapter.submitList(state.getData());
                boolean empty = state.getData() == null || state.getData().isEmpty();
                emptyTextView.setVisibility(empty ? View.VISIBLE : View.GONE);
            } else if (state.getStatus() == Resource.Status.ERROR) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Failed to load yards");
            }
        });
    }

    private void openScanScreen(VehicleLocationResponse location) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("task_number", taskNumber);
        intent.putExtra("domain", "VEHICLE");
        intent.putExtra("location_code", location.getCode());
        intent.putExtra("location_name", location.getName());
        startActivity(intent);
    }
}
