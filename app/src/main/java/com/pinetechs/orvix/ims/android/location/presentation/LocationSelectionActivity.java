package com.pinetechs.orvix.ims.android.location.presentation;

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

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.presentation.ScanActivity;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryLocationResponse;

public class LocationSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_NUMBER = "task_number";
    public static final String EXTRA_INVENTORY_DOMAIN = "inventory_domain";

    private LocationSelectionViewModel viewModel;
    private LocationAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private TextView titleTextView;

    private Long taskId;
    private String taskNumber;
    private String inventoryDomain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selection);

        taskId = getIntent().hasExtra(EXTRA_TASK_ID) ? getIntent().getLongExtra(EXTRA_TASK_ID, -1L) : null;
        if (taskId != null && taskId == -1L) {
            taskId = null;
        }
        taskNumber = getIntent().getStringExtra(EXTRA_TASK_NUMBER);
        inventoryDomain = getIntent().getStringExtra(EXTRA_INVENTORY_DOMAIN);

        viewModel = new ViewModelProvider(this).get(LocationSelectionViewModel.class);

        titleTextView = findViewById(R.id.titleTextView);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        RecyclerView recyclerView = findViewById(R.id.locationsRecyclerView);

        titleTextView.setText((taskNumber != null ? taskNumber : "Task") + " - Select Location");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationAdapter(this::openScanScreen);
        recyclerView.setAdapter(adapter);

        observeLocations();
        viewModel.loadLocations(taskId);
    }

    private void observeLocations() {
        viewModel.getLocationsState().observe(this, state -> {
            if (state == null) {
                return;
            }

            if (state.getStatus() == Resource.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            } else if (state.getStatus() == Resource.Status.SUCCESS) {
                progressBar.setVisibility(View.GONE);
                adapter.submitList(state.getData());
                boolean empty = state.getData() == null || state.getData().isEmpty();
                emptyTextView.setVisibility(empty ? View.VISIBLE : View.GONE);
            } else if (state.getStatus() == Resource.Status.ERROR) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Failed to load locations");
            }
        });
    }

    private void openScanScreen(AppInventoryLocationResponse location) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanActivity.EXTRA_TASK_ID, taskId);
        intent.putExtra(ScanActivity.EXTRA_TASK_NUMBER, taskNumber);
        intent.putExtra(ScanActivity.EXTRA_INVENTORY_DOMAIN, inventoryDomain);
        intent.putExtra(ScanActivity.EXTRA_LOCATION_CODE, location.getCode());
        intent.putExtra(ScanActivity.EXTRA_LOCATION_NAME, location.getName());
        startActivity(intent);
    }
}
