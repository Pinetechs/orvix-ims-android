package com.pinetechs.orvix.ims.android.task.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.auth.presentation.LoginActivity;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.location.presentation.LocationSelectionActivity;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskResponse;

public class TaskListActivity extends AppCompatActivity {

    private TaskListViewModel viewModel;
    private TaskListAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        viewModel = new ViewModelProvider(this).get(TaskListViewModel.class);

        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        logoutButton = findViewById(R.id.logoutButton);

        RecyclerView recyclerView = findViewById(R.id.tasksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskListAdapter(this::openLocationSelection);
        recyclerView.setAdapter(adapter);

        logoutButton.setOnClickListener(v -> logout());

        observeTasks();
        viewModel.loadTasks();
    }

    private void observeTasks() {
        viewModel.getTasksState().observe(this, state -> {
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
                emptyTextView.setText("Failed to load tasks");
            }
        });
    }

    private void openLocationSelection(AppInventoryTaskResponse task) {
        Intent intent = new Intent(this, LocationSelectionActivity.class);
        intent.putExtra(LocationSelectionActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(LocationSelectionActivity.EXTRA_TASK_NUMBER, task.getTaskNumber());
        intent.putExtra(LocationSelectionActivity.EXTRA_INVENTORY_DOMAIN, task.getInventoryDomain());
        startActivity(intent);
    }

    private void logout() {
        new SessionManager(this).clearLoginSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
