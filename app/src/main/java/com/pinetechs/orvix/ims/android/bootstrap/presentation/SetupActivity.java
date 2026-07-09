package com.pinetechs.orvix.ims.android.bootstrap.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.auth.presentation.LoginActivity;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.core.util.VersionUtils;

public class SetupActivity extends AppCompatActivity {

    private SetupViewModel viewModel;
    private TextInputEditText clientCodeEditText;
    private Button connectByCodeButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        viewModel = new ViewModelProvider(this).get(SetupViewModel.class);

        clientCodeEditText = findViewById(R.id.clientCodeEditText);
        connectByCodeButton = findViewById(R.id.connectByCodeButton);
        progressBar = findViewById(R.id.progressBar);

        connectByCodeButton.setOnClickListener(v -> {
            String clientCode = clientCodeEditText.getText() != null ? clientCodeEditText.getText().toString() : "";
            viewModel.connectByClientCode(clientCode);
        });

        observeSetupState();
    }

    private void observeSetupState() {
        viewModel.getSetupState().observe(this, state -> {
            if (state == null) {
                return;
            }

            if (state.getStatus() == Resource.Status.LOADING) {
                setLoading(true);
            } else if (state.getStatus() == Resource.Status.SUCCESS) {
                setLoading(false);
                openNextScreen();
            } else if (state.getStatus() == Resource.Status.ERROR) {
                setLoading(false);
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        connectByCodeButton.setEnabled(!loading);
    }

    private void openNextScreen() {
        SessionManager sessionManager = new SessionManager(this);

        Intent intent;
        if (VersionUtils.isForceUpdateRequired(this, sessionManager)) {
            intent = new Intent(this, UpdateRequiredActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
