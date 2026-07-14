package com.pinetechs.orvix.ims.android.workarea.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.pinetechs.orvix.ims.android.workarea.data.dto.WorkAreaResponse;
import com.pinetechs.orvix.ims.android.scan.presentation.vehicle.VehicleScanActivity;
import com.pinetechs.orvix.ims.android.scan.presentation.sparepart.SparePartScanActivity;
import com.pinetechs.orvix.ims.android.scan.presentation.asset.AssetScanActivity;

public class WorkAreaActivity extends AppCompatActivity {

    private WorkAreaViewModel viewModel;
    private WorkAreaAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    private Long taskId;
    private String taskNumber, taskName, companyName, inventoryDomain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_area);

        initIntentData();
        initViews();
        setupHeader();

        viewModel = new ViewModelProvider(this).get(WorkAreaViewModel.class);
        observeWorkAreas();
        viewModel.loadWorkAreas(taskId);
    }

    private void initIntentData() {
        taskId = getIntent().getLongExtra("task_id", -1L);
        if (taskId == -1L) taskId = null;
        taskNumber = getIntent().getStringExtra("task_number");
        taskName = getIntent().getStringExtra("task_name");
        companyName = getIntent().getStringExtra("company_name");
        inventoryDomain = getIntent().getStringExtra("inventory_domain");
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.workAreasRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkAreaAdapter(this::openScanScreen);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadWorkAreas(taskId));
    }

    private void setupHeader() {
        TextView titleTv = findViewById(R.id.titleTextView);
        TextView taskNameTv = findViewById(R.id.taskNameTextView);
        TextView companyTv = findViewById(R.id.companyNameTextView);
        TextView domainChip = findViewById(R.id.domainChipTextView);
        ImageView taskIcon = findViewById(R.id.taskIconView);

        titleTv.setText(taskNumber != null ? taskNumber : "Task Details");
        taskNameTv.setText(taskName != null ? taskName : "Inventory Session");
        companyTv.setText(companyName != null ? companyName : "Orvix IMS");
        
        if (inventoryDomain != null) {
            domainChip.setText(inventoryDomain.replace("_", " "));
            applyDomainIcon(taskIcon, inventoryDomain);
        } else {
            domainChip.setVisibility(View.GONE);
        }
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
                iconView.setImageResource(R.drawable.ic_shield_24);
                break;
        }
    }

    private void observeWorkAreas() {
        viewModel.getWorkAreasState().observe(this, state -> {
            if (state == null) return;

            if (state.getStatus() == Resource.Status.LOADING) {
                if (!swipeRefreshLayout.isRefreshing()) progressBar.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            } else if (state.getStatus() == Resource.Status.SUCCESS) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (state.getData() != null && state.getData().getContent() != null) {
                    adapter.submitList(state.getData().getContent());
                    boolean empty = state.getData().getContent().isEmpty();
                    emptyTextView.setVisibility(empty ? View.VISIBLE : View.GONE);
                }
            } else if (state.getStatus() == Resource.Status.ERROR) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Failed to load work areas");
            }
        });
    }

    private void openScanScreen(WorkAreaResponse workArea) {
        String domain = inventoryDomain != null ? inventoryDomain.toUpperCase() : "";
        Intent intent;

        switch (domain) {
            case "SPARE_PART":
                intent = new Intent(this, SparePartScanActivity.class);
                break;
            case "ASSET":
                intent = new Intent(this, AssetScanActivity.class);
                break;
            case "VEHICLE":
            default:
                intent = new Intent(this, VehicleScanActivity.class);
                break;
        }

        intent.putExtra("task_id", taskId);
        intent.putExtra("task_number", taskNumber);
        intent.putExtra("location_code", workArea.getCode());
        intent.putExtra("location_name", workArea.getName());
        startActivity(intent);
    }
}
