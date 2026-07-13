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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.auth.presentation.LoginActivity;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.workarea.presentation.WorkAreaActivity;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskResponse;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskSliceResponse;

public class TaskListActivity extends AppCompatActivity {

    private TaskListViewModel viewModel;
    private TaskListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private TextView welcomeTextView;
    private TextView assignedTasksCount, readyTasksCount, inProgressTasksCount, completedTasksCount;
    private Button logoutButton;
    private boolean showingCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        viewModel = new ViewModelProvider(this).get(TaskListViewModel.class);
        SessionManager sessionManager = new SessionManager(this);

        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        logoutButton = findViewById(R.id.logoutButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        assignedTasksCount = findViewById(R.id.assignedTasksCount);
        readyTasksCount = findViewById(R.id.readyTasksCount);
        inProgressTasksCount = findViewById(R.id.inProgressTasksCount);
        completedTasksCount = findViewById(R.id.completedTasksCount);

        setupClickListeners();

        if (welcomeTextView != null) {
            String fullName = sessionManager.getFullName();
            welcomeTextView.setText("Welcome, " + (fullName != null ? fullName : "User"));
        }

        RecyclerView recyclerView = findViewById(R.id.tasksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskListAdapter(this::openLocationSelection);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.loadTasks(showingCompleted));

        observeTasks();
        viewModel.loadTasks(showingCompleted);
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> logout());
        
        // Toggle when clicking on History box
        View historyBox = findViewById(R.id.historyBox);
        if (historyBox != null) {
            historyBox.setOnClickListener(v -> {
                showingCompleted = !showingCompleted;
                
                // Visual feedback for the selected mode
                historyBox.setAlpha(showingCompleted ? 1.0f : 0.6f);
                
                viewModel.loadTasks(showingCompleted);
            });
            // Initial state: slightly faded when not active
            historyBox.setAlpha(showingCompleted ? 1.0f : 0.6f);
        }
    }

    private void observeTasks() {
        viewModel.getTasksState().observe(this, state -> {
            if (state == null) {
                return;
            }

            if (state.getStatus() == Resource.Status.LOADING) {
                if (!swipeRefreshLayout.isRefreshing()) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                emptyTextView.setVisibility(View.GONE);
            } else if (state.getStatus() == Resource.Status.SUCCESS) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                
                AppInventoryTaskSliceResponse data = state.getData();
                if (data != null) {
                    updateHeaderStats(data);
                    if (data.getTasks() != null) {
                        adapter.submitList(data.getTasks().getContent());
                        boolean empty = data.getTasks().getContent() == null || data.getTasks().getContent().isEmpty();
                        emptyTextView.setVisibility(empty ? View.VISIBLE : View.GONE);
                        if (empty) {
                            emptyTextView.setText(showingCompleted ? "No completed tasks yet." : "No active tasks assigned to you.");
                        }
                    }
                }
            } else if (state.getStatus() == Resource.Status.ERROR) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Failed to load tasks");
            }
        });
    }

    private void updateHeaderStats(AppInventoryTaskSliceResponse data) {
        if (assignedTasksCount != null) assignedTasksCount.setText(String.valueOf(data.getAssignedTasks()));
        if (readyTasksCount != null) readyTasksCount.setText(String.valueOf(data.getReadyToStartTasks()));
        if (inProgressTasksCount != null) inProgressTasksCount.setText(String.valueOf(data.getInProgressTasks()));
        if (completedTasksCount != null) completedTasksCount.setText(String.valueOf(data.getCompletedTasks()));
    }

    private void openLocationSelection(AppInventoryTaskResponse task) {
        Intent intent = new Intent(this, WorkAreaActivity.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_number", task.getTaskNumber());
        intent.putExtra("task_name", task.getTaskName());
        intent.putExtra("company_name", task.getCompanyName());
        intent.putExtra("inventory_domain", task.getInventoryDomain());
        startActivity(intent);
    }

    private void logout() {
        new SessionManager(this).clearLoginSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
