package com.pinetechs.orvix.ims.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pinetechs.orvix.ims.android.auth.presentation.LoginActivity;
import com.pinetechs.orvix.ims.android.bootstrap.presentation.SetupActivity;
import com.pinetechs.orvix.ims.android.bootstrap.presentation.UpdateRequiredActivity;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.VersionUtils;
import com.pinetechs.orvix.ims.android.task.presentation.TaskListActivity;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.hasClientConfig()) {
            openAndFinish(SetupActivity.class);
            return;
        }

        if (VersionUtils.isForceUpdateRequired(this, sessionManager)) {
            openAndFinish(UpdateRequiredActivity.class);
            return;
        }

        if (VersionUtils.isOptionalUpdateAvailable(this, sessionManager)) {
            showOptionalUpdateDialog();
            return;
        }

        openDefaultScreen();
    }

    private void showOptionalUpdateDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update available")
                .setMessage("A newer Orvix IMS version is available. You can update now or continue with the current version.")
                .setPositiveButton("Update", (dialog, which) -> openApkDownloadUrl())
                .setNegativeButton("Later", (dialog, which) -> openDefaultScreen())
                .setCancelable(false)
                .show();
    }

    private void openApkDownloadUrl() {
        String apkUrl = sessionManager.getAndroidApkUrl();
        if (apkUrl != null && !apkUrl.trim().isEmpty()) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)));
        }
        openDefaultScreen();
    }

    private void openDefaultScreen() {
        if (sessionManager.isLoggedIn()) {
            openAndFinish(TaskListActivity.class);
        } else {
            openAndFinish(LoginActivity.class);
        }
    }

    private void openAndFinish(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        finish();
    }
}
