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
import com.pinetechs.orvix.ims.android.core.hardware.presentation.ScannerSettingsActivity;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.workarea.presentation.WorkAreaActivity;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskResponse;
import com.pinetechs.orvix.ims.android.core.presentation.BaseActivity;
import com.pinetechs.orvix.ims.android.task.data.dto.AppInventoryTaskSliceResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.pinetechs.orvix.ims.android.core.util.VersionUtils;
import com.pinetechs.orvix.ims.android.core.util.DeviceUtils;
import androidx.appcompat.app.AlertDialog;

import com.pinetechs.orvix.ims.android.bootstrap.presentation.AboutActivity;

public class TaskListActivity extends BaseActivity {

    private TaskListViewModel viewModel;
    private TaskListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private TextView welcomeTextView;
    private TextView assignedTasksCount, readyTasksCount, inProgressTasksCount, completedTasksCount;
    private View profileAvatarContainer;
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
        profileAvatarContainer = findViewById(R.id.profileAvatarContainer);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        assignedTasksCount = findViewById(R.id.assignedTasksCount);
        readyTasksCount = findViewById(R.id.readyTasksCount);
        inProgressTasksCount = findViewById(R.id.inProgressTasksCount);
        completedTasksCount = findViewById(R.id.completedTasksCount);

        setupClickListeners();

        if (welcomeTextView != null) {
            String fullName = sessionManager.getFullName();
            welcomeTextView.setText(getString(R.string.welcome_label, fullName != null ? fullName : "User"));
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
        profileAvatarContainer.setOnClickListener(v -> showProfileBottomSheet());

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

    private void showProfileBottomSheet() {
        SessionManager sessionManager = new SessionManager(this);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_user_profile, null);

        TextView initialsTv = view.findViewById(R.id.userInitialsTextView);
        TextView nameTv = view.findViewById(R.id.userNameTextView);
        TextView roleTv = view.findViewById(R.id.userRoleTextView);

        String fullName = sessionManager.getFullName();
        nameTv.setText(fullName != null ? fullName : "User");
        roleTv.setText(sessionManager.getClientName());

        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.split(" ");
            String initials = parts[0].substring(0, 1).toUpperCase();
            if (parts.length > 1) initials += parts[parts.length - 1].substring(0, 1).toUpperCase();
            initialsTv.setText(initials);
        } else {
            initialsTv.setText("U");
        }

        view.findViewById(R.id.settingsActionItem).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, ScannerSettingsActivity.class));
        });

        view.findViewById(R.id.languageActionItem).setOnClickListener(v -> {
            dialog.dismiss();
            toggleLanguage();
        });

        view.findViewById(R.id.aboutActionItem).setOnClickListener(v -> {
            dialog.dismiss();
            showAboutDialog();
        });

        view.findViewById(R.id.logoutActionItem).setOnClickListener(v -> {
            dialog.dismiss();
            logout();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void toggleLanguage() {
        SessionManager sessionManager = new SessionManager(this);
        String currentLang = sessionManager.getLanguage();
        String newLang = "ar".equals(currentLang) ? "en" : "ar";
        
        sessionManager.setLanguage(newLang);
        
        // Restart the activity to apply changes
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void showAboutDialog() {
        startActivity(new Intent(this, AboutActivity.class));
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
                            emptyTextView.setText(showingCompleted ? R.string.no_completed_tasks : R.string.no_active_tasks);
                        }
                    }
                }
            } else if (state.getStatus() == Resource.Status.ERROR) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText(R.string.err_failed_to_load_tasks);
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
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
        String status = task.getStatus() == null ? "" : task.getStatus().toUpperCase();
        if (!"READY_TO_START".equals(status) && !"IN_PROGRESS".equals(status)) {
            Toast.makeText(this, R.string.err_task_not_scannable, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, WorkAreaActivity.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_number", task.getTaskNumber());
        intent.putExtra("task_name", task.getTaskName());
        intent.putExtra("company_name", task.getCompanyName());
        intent.putExtra("inventory_domain", task.getInventoryDomain());
        intent.putExtra("scan_image_required", task.isScanImageRequired());
        intent.putExtra("spare_progress_mode", task.getSparePartLocationProgressMode());
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
