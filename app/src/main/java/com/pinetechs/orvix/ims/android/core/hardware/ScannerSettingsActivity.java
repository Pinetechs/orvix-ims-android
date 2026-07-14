package com.pinetechs.orvix.ims.android.core.hardware;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;

public class ScannerSettingsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private SwitchMaterial beepSwitch;
    private TextInputEditText actionEditText, tagEditText, typeTagEditText;
    private Button saveButton, resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_settings);

        sessionManager = new SessionManager(this);

        initViews();
        loadCurrentSettings();
    }

    private void initViews() {
        beepSwitch = findViewById(R.id.beepSwitch);
        actionEditText = findViewById(R.id.actionEditText);
        tagEditText = findViewById(R.id.tagEditText);
        typeTagEditText = findViewById(R.id.typeTagEditText);
        saveButton = findViewById(R.id.saveButton);
        resetButton = findViewById(R.id.resetButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> saveSettings());
        resetButton.setOnClickListener(v -> resetToDefaults());
    }

    private void loadCurrentSettings() {
        beepSwitch.setChecked(sessionManager.isScannerBeepEnabled());
        actionEditText.setText(sessionManager.getUrovoIntentAction());
        tagEditText.setText(sessionManager.getUrovoDataTag());
        typeTagEditText.setText(sessionManager.getUrovoTypeTag());
    }

    private void saveSettings() {
        String action = actionEditText.getText() != null ? actionEditText.getText().toString().trim() : "";
        String tag = tagEditText.getText() != null ? tagEditText.getText().toString().trim() : "";
        String typeTag = typeTagEditText.getText() != null ? typeTagEditText.getText().toString().trim() : "";

        if (action.isEmpty() || tag.isEmpty() || typeTag.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.setScannerBeepEnabled(beepSwitch.isChecked());
        sessionManager.setUrovoIntentAction(action);
        sessionManager.setUrovoDataTag(tag);
        sessionManager.setUrovoTypeTag(typeTag);

        Toast.makeText(this, "Scanner settings saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void resetToDefaults() {
        sessionManager.resetScannerSettings();
        loadCurrentSettings();
        Toast.makeText(this, "Reset to default settings", Toast.LENGTH_SHORT).show();
    }
}
