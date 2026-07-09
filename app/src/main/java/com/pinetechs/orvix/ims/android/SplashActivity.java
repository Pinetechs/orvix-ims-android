package com.pinetechs.orvix.ims.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pinetechs.orvix.ims.android.auth.presentation.LoginActivity;
import com.pinetechs.orvix.ims.android.bootstrap.data.BootstrapRepository;
import com.pinetechs.orvix.ims.android.bootstrap.data.dto.BootstrapResolveResponse;
import com.pinetechs.orvix.ims.android.bootstrap.presentation.SetupActivity;
import com.pinetechs.orvix.ims.android.bootstrap.presentation.UpdateRequiredActivity;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.LocaleHelper;
import com.pinetechs.orvix.ims.android.core.util.VersionUtils;
import com.pinetechs.orvix.ims.android.task.presentation.TaskListActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private SessionManager sessionManager;
    private BootstrapRepository repository;
    private long startTime;
    private static final int MIN_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        repository = new BootstrapRepository(this);
        startTime = System.currentTimeMillis();

        startBootstrapCheck();
    }

    private void startBootstrapCheck() {
        String clientCode = sessionManager.getClientCode();

        if (clientCode == null || clientCode.trim().isEmpty()) {
            // No client configured yet
            waitAndOpen(SetupActivity.class);
            return;
        }

        // Try to fetch latest config
        repository.loadByClientCode(clientCode, new BootstrapRepository.RepositoryCallback<BootstrapResolveResponse>() {
            @Override
            public void onSuccess(BootstrapResolveResponse data) {
                sessionManager.saveClientConfig(data);
                checkNavigationAndProceed();
            }

            @Override
            public void onError(String message) {
                // If we already have config, we can proceed even if API fails (offline/resilience)
                if (sessionManager.hasClientConfig()) {
                    checkNavigationAndProceed();
                } else {
                    // Critical failure: no config and can't fetch it
                    Toast.makeText(SplashActivity.this, "Connection failed: " + message, Toast.LENGTH_LONG).show();
                    waitAndOpen(SetupActivity.class);
                }
            }
        });
    }

    private void checkNavigationAndProceed() {
        // 1. Check for Force Update
        if (VersionUtils.isForceUpdateRequired(this, sessionManager)) {
            waitAndOpen(UpdateRequiredActivity.class);
            return;
        }

        // 2. Decide between TaskList or Login
        if (sessionManager.isLoggedIn()) {
            waitAndOpen(TaskListActivity.class);
        } else {
            waitAndOpen(LoginActivity.class);
        }
    }

    private void waitAndOpen(Class<?> target) {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = Math.max(0, MIN_DELAY - elapsed);

        new Handler(Looper.getMainLooper()).postDelayed(() -> openAndFinish(target), remaining);
    }

    private void openAndFinish(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        finish();
    }
}
