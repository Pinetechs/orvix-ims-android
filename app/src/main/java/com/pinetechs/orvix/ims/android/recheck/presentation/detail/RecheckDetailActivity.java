package com.pinetechs.orvix.ims.android.recheck.presentation.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.presentation.BaseActivity;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckItemResponse;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckRequestResponse;
import com.pinetechs.orvix.ims.android.recheck.presentation.RecheckUiText;
import com.pinetechs.orvix.ims.android.recheck.presentation.submit.RecheckSubmissionActivity;

public class RecheckDetailActivity extends BaseActivity {

    public static final String EXTRA_REQUEST_ID = "recheck_request_id";

    private RecheckDetailViewModel viewModel;
    private RecheckItemAdapter adapter;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView requestNumberTextView;
    private TextView taskTextView;
    private TextView domainTextView;
    private TextView statusTextView;
    private TextView workAreaTextView;
    private TextView instructionsTextView;
    private TextView dueAtTextView;
    private TextView imageRuleTextView;
    private TextView itemProgressTextView;
    private Button startButton;
    private Long requestId;
    private RecheckRequestResponse currentRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recheck_detail);

        requestId = readRequestId();
        bindViews();
        setupList();

        viewModel = new ViewModelProvider(this).get(RecheckDetailViewModel.class);
        observeState();
        viewModel.load(requestId);
    }

    private Long readRequestId() {
        long value = getIntent().getLongExtra(EXTRA_REQUEST_ID, -1L);
        return value < 0 ? null : value;
    }

    private void bindViews() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        requestNumberTextView = findViewById(R.id.requestNumberTextView);
        taskTextView = findViewById(R.id.taskTextView);
        domainTextView = findViewById(R.id.domainChipTextView);
        statusTextView = findViewById(R.id.statusChipTextView);
        workAreaTextView = findViewById(R.id.workAreaTextView);
        instructionsTextView = findViewById(R.id.instructionsTextView);
        dueAtTextView = findViewById(R.id.dueAtTextView);
        imageRuleTextView = findViewById(R.id.imageRuleTextView);
        itemProgressTextView = findViewById(R.id.itemProgressTextView);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> viewModel.start());
    }

    private void setupList() {
        RecyclerView recyclerView = findViewById(R.id.itemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new RecheckItemAdapter(this::openItem);
        recyclerView.setAdapter(adapter);
    }

    private void observeState() {
        viewModel.getState().observe(this, state -> {
            if (state == null) return;
            boolean loading = state.getStatus() == Resource.Status.LOADING;
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            startButton.setEnabled(!loading);

            if (state.getStatus() == Resource.Status.ERROR) {
                errorTextView.setText(state.getMessage());
                errorTextView.setVisibility(View.VISIBLE);
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
            } else if (state.getStatus() == Resource.Status.SUCCESS
                    && state.getData() != null) {
                errorTextView.setVisibility(View.GONE);
                render(state.getData());
            }
        });
    }

    private void render(RecheckRequestResponse request) {
        currentRequest = request;
        requestNumberTextView.setText(
                RecheckUiText.valueOrDash(request.getRequestNumber()));
        taskTextView.setText(getString(
                R.string.recheck_task_title_value,
                RecheckUiText.valueOrDash(request.getTaskName()),
                RecheckUiText.valueOrDash(request.getTaskNumber())
        ));
        domainTextView.setText(RecheckUiText.domain(this, request.getInventoryDomain()));
        statusTextView.setText(RecheckUiText.status(this, request.getStatus()));
        workAreaTextView.setText(getString(
                R.string.recheck_work_area_value,
                RecheckUiText.valueOrDash(request.getWorkAreaLabel())
        ));
        instructionsTextView.setText(
                RecheckUiText.valueOrDash(request.getInstructions()));
        dueAtTextView.setText(getString(
                R.string.recheck_due_value,
                RecheckUiText.date(request.getDueAt())
        ));
        imageRuleTextView.setText(request.isImageRequired()
                ? R.string.recheck_image_required
                : R.string.recheck_image_optional);
        imageRuleTextView.setBackgroundResource(request.isImageRequired()
                ? R.drawable.bg_chip_warning
                : R.drawable.bg_chip_blue);
        itemProgressTextView.setText(getString(
                R.string.recheck_items_progress,
                request.submittedItemCount(),
                request.getItems().size()
        ));

        startButton.setVisibility(request.canStart() ? View.VISIBLE : View.GONE);
        adapter.submitList(request.getItems(), request.canWork());
    }

    private void openItem(RecheckItemResponse item) {
        if (currentRequest == null
                || item == null
                || item.getId() == null
                || !currentRequest.canWork()) {
            return;
        }
        Intent intent = new Intent(this, RecheckSubmissionActivity.class);
        intent.putExtra(RecheckSubmissionActivity.EXTRA_REQUEST_ID, currentRequest.getId());
        intent.putExtra(RecheckSubmissionActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null && requestId != null && currentRequest != null) {
            viewModel.refresh();
        }
    }
}
