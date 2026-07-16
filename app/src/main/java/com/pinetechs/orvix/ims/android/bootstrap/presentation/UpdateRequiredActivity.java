package com.pinetechs.orvix.ims.android.bootstrap.presentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.presentation.BaseActivity;
import com.pinetechs.orvix.ims.android.core.util.VersionUtils;

public class UpdateRequiredActivity extends BaseActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_required);

        sessionManager = new SessionManager(this);

        TextView titleTextView = findViewById(R.id.updateTitleTextView);
        TextView messageTextView = findViewById(R.id.updateMessageTextView);
        TextView releaseNotesTextView = findViewById(R.id.releaseNotesTextView);
        Button downloadButton = findViewById(R.id.downloadUpdateButton);
        Button resetButton = findViewById(R.id.resetSetupButton);

        String clientName = sessionManager.getClientName();
        int currentVersion = VersionUtils.getCurrentVersionCode(this);

        titleTextView.setText(R.string.update_required_title);
        messageTextView.setText(getString(R.string.update_required_msg)
                + (clientName != null ? " (" + clientName + ")" : "")
                + ".\n" + getString(R.string.current_version, currentVersion)
                + "\n" + getString(R.string.min_required_version, sessionManager.getMinSupportedAndroidVersionCode()));

        String releaseNotes = sessionManager.getReleaseNotes();
        if (releaseNotes == null || releaseNotes.trim().isEmpty()) {
            releaseNotesTextView.setText(R.string.msg_download_apk);
        } else {
            releaseNotesTextView.setText(releaseNotes);
        }

        downloadButton.setOnClickListener(v -> openApkDownloadUrl());
        resetButton.setOnClickListener(v -> resetSetup());
    }

    private void openApkDownloadUrl() {
        String apkUrl = sessionManager.getAndroidApkUrl();
        if (apkUrl == null || apkUrl.trim().isEmpty()) {
            Toast.makeText(this, "APK download URL is not configured", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
        startActivity(intent);
    }

    private void resetSetup() {
        sessionManager.clearClientConfigAndSession();
        Intent intent = new Intent(this, SetupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
