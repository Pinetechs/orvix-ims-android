package com.pinetechs.orvix.ims.android.recheck.presentation.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.presentation.BaseActivity;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckPageResponse;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckRequestResponse;
import com.pinetechs.orvix.ims.android.recheck.presentation.detail.RecheckDetailActivity;

public class RecheckListActivity extends BaseActivity {

    private RecheckListViewModel viewModel;
    private RecheckRequestAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private TextView requestCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recheck_list);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        requestCountTextView = findViewById(R.id.requestCountTextView);

        RecyclerView recyclerView = findViewById(R.id.rechecksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecheckRequestAdapter(this::openRequest);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(RecheckListViewModel.class);
        swipeRefreshLayout.setOnRefreshListener(viewModel::load);
        observeState();
    }

    private void observeState() {
        viewModel.getState().observe(this, state -> {
            if (state == null) return;

            if (state.getStatus() == Resource.Status.LOADING) {
                if (!swipeRefreshLayout.isRefreshing()) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                emptyTextView.setVisibility(View.GONE);
                return;
            }

            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);

            if (state.getStatus() == Resource.Status.ERROR) {
                emptyTextView.setText(R.string.recheck_load_error);
                emptyTextView.setVisibility(View.VISIBLE);
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (state.getStatus() == Resource.Status.SUCCESS) {
                render(state.getData());
            }
        });
    }

    private void render(RecheckPageResponse page) {
        adapter.submitList(page == null ? null : page.getContent());
        int count = page == null ? 0 : page.getContent().size();
        requestCountTextView.setText(getResources().getQuantityString(
                R.plurals.recheck_active_request_count,
                count,
                count
        ));
        boolean empty = count == 0;
        emptyTextView.setText(R.string.recheck_no_active_requests);
        emptyTextView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void openRequest(RecheckRequestResponse request) {
        if (request == null || request.getId() == null) return;
        Intent intent = new Intent(this, RecheckDetailActivity.class);
        intent.putExtra(RecheckDetailActivity.EXTRA_REQUEST_ID, request.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.load();
        }
    }
}
