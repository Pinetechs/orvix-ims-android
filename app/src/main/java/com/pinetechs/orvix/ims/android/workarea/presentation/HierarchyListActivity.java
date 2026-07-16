package com.pinetechs.orvix.ims.android.workarea.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.scan.presentation.asset.AssetScanActivity;
import com.pinetechs.orvix.ims.android.scan.presentation.sparepart.SparePartScanActivity;
import com.pinetechs.orvix.ims.android.workarea.data.WorkAreaRepository;
import com.pinetechs.orvix.ims.android.core.presentation.BaseActivity;
import com.pinetechs.orvix.ims.android.workarea.data.dto.HierarchyOptionResponse;

import java.util.List;

public class HierarchyListActivity extends BaseActivity implements HierarchyOptionAdapter.Listener {
    public static final String MODE_SPARE_LOCATIONS = "SPARE_LOCATIONS";
    public static final String MODE_ASSET_FLOORS = "ASSET_FLOORS";
    public static final String MODE_ASSET_PLACES = "ASSET_PLACES";

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = this::loadOptions;
    private WorkAreaRepository repository;
    private HierarchyOptionAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextInputEditText searchEditText;
    private String mode;
    private Long taskId;
    private Long parentId;
    private Long workAreaId;
    private String taskNumber;
    private String workAreaCode;
    private String workAreaName;
    private String floorName;
    private boolean scanImageRequired;
    private String spareProgressMode;
    private int requestGeneration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hierarchy_list);
        readIntent();
        repository = new WorkAreaRepository(this);
        setupViews();
    }

    private void readIntent() {
        mode = getIntent().getStringExtra("hierarchy_mode");
        taskId = longExtra("task_id");
        parentId = longExtra("parent_id");
        workAreaId = longExtra("work_area_id");
        taskNumber = getIntent().getStringExtra("task_number");
        workAreaCode = getIntent().getStringExtra("location_code");
        workAreaName = getIntent().getStringExtra("location_name");
        floorName = getIntent().getStringExtra("floor_name");
        scanImageRequired = getIntent().getBooleanExtra("scan_image_required", false);
        spareProgressMode = getIntent().getStringExtra("spare_progress_mode");
    }

    private Long longExtra(String key) {
        long value = getIntent().getLongExtra(key, -1L);
        return value < 0 ? null : value;
    }

    private void setupViews() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        TextView title = findViewById(R.id.titleTextView);
        TextView context = findViewById(R.id.contextTextView);
        TextView legend = findViewById(R.id.progressLegendTextView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyTextView);
        searchEditText = findViewById(R.id.searchEditText);

        if (MODE_SPARE_LOCATIONS.equals(mode)) {
            title.setText(R.string.inventory_locations);
            context.setText(nonBlank(workAreaName, workAreaCode));
            legend.setText(getString(R.string.legend_basic)
                    + ("DETAILED".equalsIgnoreCase(spareProgressMode)
                    ? "\n" + getString(R.string.legend_detailed) : ""));
            searchEditText.setHint(R.string.search_location_hint);
        } else if (MODE_ASSET_PLACES.equals(mode)) {
            title.setText(R.string.select_place);
            context.setText(nonBlank(floorName, nonBlank(workAreaName, workAreaCode)));
            legend.setText(R.string.choose_place_legend);
            searchEditText.setHint(R.string.search_places);
        } else {
            title.setText(R.string.select_floor);
            context.setText(nonBlank(workAreaName, workAreaCode));
            legend.setText(R.string.legend_basic);
            searchEditText.setHint(R.string.search_floors);
        }

        RecyclerView recycler = findViewById(R.id.optionsRecyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HierarchyOptionAdapter(this);
        recycler.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
                searchHandler.postDelayed(searchRunnable, 300L);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void loadOptions() {
        if (taskId == null || parentId == null) {
            showError(getString(R.string.err_task_parent_missing));
            return;
        }
        final int generation = ++requestGeneration;
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        String search = searchEditText.getText() == null ? null : searchEditText.getText().toString();
        WorkAreaRepository.RepositoryCallback<List<HierarchyOptionResponse>> callback =
                new WorkAreaRepository.RepositoryCallback<List<HierarchyOptionResponse>>() {
                    @Override public void onSuccess(List<HierarchyOptionResponse> data) {
                        if (generation != requestGeneration || isFinishing()) return;
                        progressBar.setVisibility(View.GONE);
                        adapter.submitList(data);
                        boolean empty = data == null || data.isEmpty();
                        emptyView.setText(R.string.no_matching_locations);
                        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                    }

                    @Override public void onError(String message) {
                        if (generation != requestGeneration || isFinishing()) return;
                        progressBar.setVisibility(View.GONE);
                        showError(message != null ? message : getString(R.string.err_could_not_load_locations));
                    }
                };

        if (MODE_SPARE_LOCATIONS.equals(mode)) repository.getSpareLocations(taskId, parentId, search, callback);
        else if (MODE_ASSET_PLACES.equals(mode)) repository.getPlaces(taskId, parentId, search, callback);
        else repository.getFloors(taskId, parentId, search, callback);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        emptyView.setText(message == null ? "Could not load locations" : message);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onOpen(HierarchyOptionResponse item) {
        if (item == null || item.getId() == null) return;
        if (MODE_SPARE_LOCATIONS.equals(mode)) {
            Intent intent = scanIntent(SparePartScanActivity.class);
            intent.putExtra("selected_location_id", item.getId());
            intent.putExtra("selected_location_code", item.getCode());
            intent.putExtra("selected_location_name", item.getName());
            startActivity(intent);
        } else if (MODE_ASSET_FLOORS.equals(mode)) {
            Intent intent = commonIntent(HierarchyListActivity.class);
            intent.putExtra("hierarchy_mode", MODE_ASSET_PLACES);
            intent.putExtra("parent_id", item.getId());
            intent.putExtra("floor_name", item.getName());
            intent.putExtra("floor_id", item.getId());
            startActivity(intent);
        } else {
            Intent intent = scanIntent(AssetScanActivity.class);
            intent.putExtra("floor_id", longExtra("floor_id"));
            intent.putExtra("floor_name", floorName);
            intent.putExtra("place_id", item.getId());
            intent.putExtra("place_name", item.getName());
            startActivity(intent);
        }
    }

    @Override
    public void onComplete(HierarchyOptionResponse item) {
        if (!MODE_SPARE_LOCATIONS.equals(mode) || item == null || !item.isCanComplete()) return;
        new AlertDialog.Builder(this)
                .setTitle("Complete this location?")
                .setMessage("New scans will reopen it automatically. A location with pending review cannot be completed.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Complete", (dialog, which) -> completeLocation(item))
                .show();
    }

    private void completeLocation(HierarchyOptionResponse item) {
        progressBar.setVisibility(View.VISIBLE);
        repository.completeSpareLocation(taskId, parentId, item.getId(),
                new WorkAreaRepository.RepositoryCallback<HierarchyOptionResponse>() {
                    @Override public void onSuccess(HierarchyOptionResponse data) {
                        Toast.makeText(HierarchyListActivity.this, R.string.msg_location_completed, Toast.LENGTH_SHORT).show();
                        loadOptions();
                    }
                    @Override public void onError(String message) {
                        progressBar.setVisibility(View.GONE);
                        new AlertDialog.Builder(HierarchyListActivity.this)
                                .setTitle(R.string.title_location_not_completed)
                                .setMessage(message)
                                .setPositiveButton(R.string.continue_label, null)
                                .show();
                    }
                });
    }

    private Intent commonIntent(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.putExtra("task_id", taskId);
        intent.putExtra("task_number", taskNumber);
        intent.putExtra("work_area_id", workAreaId);
        intent.putExtra("location_code", workAreaCode);
        intent.putExtra("location_name", workAreaName);
        intent.putExtra("scan_image_required", scanImageRequired);
        intent.putExtra("spare_progress_mode", spareProgressMode);
        return intent;
    }

    private Intent scanIntent(Class<?> target) {
        return commonIntent(target);
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? (fallback == null ? "" : fallback) : value;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOptions();
    }

    @Override
    protected void onDestroy() {
        searchHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
