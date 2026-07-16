package com.pinetechs.orvix.ims.android.core.hardware.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.pinetechs.orvix.ims.android.core.presentation.BaseActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerFactory;
import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerBeepMode;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfileSettings;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologySettings;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Constants;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ScannerSettingsActivity extends BaseActivity {

    private static final ScannerProfile[] EDITABLE_PROFILES = {
            ScannerProfile.VEHICLE,
            ScannerProfile.SPARE_PART,
            ScannerProfile.ASSET,
            ScannerProfile.GENERAL
    };

    private final Gson gson = new Gson();
    private SessionManager sessionManager;
    private Spinner beepModeSpinner;
    private TextView beepModeDescriptionTextView;
    private SwitchMaterial vibrationSwitch;
    private TextInputEditText actionEditText, tagEditText, typeTagEditText;
    private TextInputEditText profileMinLengthEditText, profileMaxLengthEditText;

    private Spinner profileSpinner;
    private TextView profileDescriptionTextView, enabledSymbologiesSummary;
    private SwitchMaterial showCapturedImageSwitch;
    private MaterialButton manageSymbologiesButton;

    private final Map<ScannerProfile, ScannerProfileSettings> profileSettings = new EnumMap<>(ScannerProfile.class);
    private ScannerProfile selectedProfile = ScannerProfile.VEHICLE;
    private boolean loadingProfile = false;

    private final ActivityResultLauncher<Intent> symbologyLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String json = result.getData().getStringExtra(Constants.EXTRA_SCANNER_SETTINGS_JSON);
                    if (json != null) {
                        ScannerProfileSettings updated = gson.fromJson(json, ScannerProfileSettings.class);
                        profileSettings.put(selectedProfile, updated);
                        updateSymbologySummary();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_settings);

        sessionManager = new SessionManager(this);

        initViews();
        setupBeepModeSelector();
        setupProfileSelector();
        loadCurrentSettings();
    }

    private void initViews() {
        beepModeSpinner = findViewById(R.id.beepModeSpinner);
        beepModeDescriptionTextView = findViewById(R.id.beepModeDescriptionTextView);
        vibrationSwitch = findViewById(R.id.vibrationSwitch);
        actionEditText = findViewById(R.id.actionEditText);
        tagEditText = findViewById(R.id.tagEditText);
        typeTagEditText = findViewById(R.id.typeTagEditText);
        profileMinLengthEditText = findViewById(R.id.profileMinLengthEditText);
        profileMaxLengthEditText = findViewById(R.id.profileMaxLengthEditText);

        boolean urovoDevice = "UROVO".equals(ScannerFactory.getCurrentScannerVendor());
        findViewById(R.id.urovoAdvancedTitle).setVisibility(urovoDevice ? View.VISIBLE : View.GONE);
        findViewById(R.id.urovoAdvancedCard).setVisibility(urovoDevice ? View.VISIBLE : View.GONE);

        profileSpinner = findViewById(R.id.profileSpinner);
        profileDescriptionTextView = findViewById(R.id.profileDescriptionTextView);
        enabledSymbologiesSummary = findViewById(R.id.enabledSymbologiesSummary);
        showCapturedImageSwitch = findViewById(R.id.showCapturedImageSwitch);
        manageSymbologiesButton = findViewById(R.id.manageSymbologiesButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.saveButton).setOnClickListener(v -> saveSettings());
        findViewById(R.id.resetButton).setOnClickListener(v -> resetToDefaults());

        manageSymbologiesButton.setOnClickListener(v -> launchSymbologyManager());
    }

    private void setupBeepModeSelector() {
        ScannerBeepMode[] modes = ScannerBeepMode.values();
        String[] names = new String[modes.length];
        for (int i = 0; i < modes.length; i++) names[i] = modes[i].getDisplayName(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        beepModeSpinner.setAdapter(adapter);
        beepModeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                beepModeDescriptionTextView.setText(modes[pos].getDescription(ScannerSettingsActivity.this));
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
    }

    private void setupProfileSelector() {
        String[] names = new String[EDITABLE_PROFILES.length];
        for (int i = 0; i < EDITABLE_PROFILES.length; i++) names[i] = EDITABLE_PROFILES[i].getDisplayName(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileSpinner.setAdapter(adapter);
        profileSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                if (!loadingProfile) captureSelectedProfile();
                selectedProfile = EDITABLE_PROFILES[pos];
                showProfile(selectedProfile);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
    }

    private void loadCurrentSettings() {
        loadingProfile = true;
        ScannerBeepMode currentBeep = sessionManager.getScannerBeepMode();
        for (int i = 0; i < ScannerBeepMode.values().length; i++) {
            if (ScannerBeepMode.values()[i] == currentBeep) beepModeSpinner.setSelection(i);
        }
        vibrationSwitch.setChecked(sessionManager.isScannerVibrationEnabled());
        actionEditText.setText(sessionManager.getUrovoIntentAction());
        tagEditText.setText(sessionManager.getUrovoDataTag());
        typeTagEditText.setText(sessionManager.getUrovoTypeTag());

        for (ScannerProfile profile : EDITABLE_PROFILES) {
            profileSettings.put(profile, sessionManager.getScannerProfileSettings(profile));
        }

        showProfile(selectedProfile);
        loadingProfile = false;
    }

    private void showProfile(ScannerProfile profile) {
        loadingProfile = true;
        ScannerProfileSettings s = profileSettings.get(profile);
        if (s != null) {
            profileDescriptionTextView.setText(profile.getDescription(this));
            showCapturedImageSwitch.setChecked(s.isShowCapturedImage());
            profileMinLengthEditText.setText(String.valueOf(s.getMinScanLength()));
            profileMaxLengthEditText.setText(String.valueOf(s.getMaxScanLength()));
            updateSymbologySummary();
        }
        loadingProfile = false;
    }

    private void updateSymbologySummary() {
        ScannerProfileSettings s = profileSettings.get(selectedProfile);
        if (s == null) return;

        String summary = s.getSymbologies().values().stream()
                .filter(ScannerSymbologySettings::isEnabled)
                .map(ss -> {
                    BarcodeSymbology b = BarcodeSymbology.fromStorageName(
                        s.getSymbologies().entrySet().stream()
                            .filter(e -> e.getValue() == ss)
                            .map(Map.Entry::getKey)
                            .findFirst().orElse("")
                    );
                    return b != null ? b.getDisplayName() : "";
                })
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(", "));

        enabledSymbologiesSummary.setText(summary.isEmpty() ? getString(R.string.none_enabled) : summary);
    }

    private void launchSymbologyManager() {
        captureSelectedProfile();
        Intent intent = new Intent(this, SymbologyListActivity.class);
        intent.putExtra(Constants.EXTRA_SCANNER_PROFILE, selectedProfile.name());
        intent.putExtra(Constants.EXTRA_SCANNER_SETTINGS_JSON, gson.toJson(profileSettings.get(selectedProfile)));
        symbologyLauncher.launch(intent);
    }

    private void captureSelectedProfile() {
        ScannerProfileSettings s = profileSettings.get(selectedProfile);
        if (s == null) return;
        s.setShowCapturedImage(showCapturedImageSwitch.isChecked());
        try {
            s.setMinScanLength(Integer.parseInt(textOf(profileMinLengthEditText)));
            s.setMaxScanLength(Integer.parseInt(textOf(profileMaxLengthEditText)));
        } catch (Exception ignored) {}
    }

    private void saveSettings() {
        captureSelectedProfile();
        sessionManager.setScannerBeepMode(ScannerBeepMode.values()[beepModeSpinner.getSelectedItemPosition()]);
        sessionManager.setScannerVibrationEnabled(vibrationSwitch.isChecked());
        sessionManager.setUrovoIntentAction(textOf(actionEditText));
        sessionManager.setUrovoDataTag(textOf(tagEditText));
        sessionManager.setUrovoTypeTag(textOf(typeTagEditText));

        for (Map.Entry<ScannerProfile, ScannerProfileSettings> entry : profileSettings.entrySet()) {
            sessionManager.setScannerProfileSettings(entry.getKey(), entry.getValue());
        }

        Toast.makeText(this, R.string.msg_settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void resetToDefaults() {
        sessionManager.resetScannerSettings();
        loadCurrentSettings();
        Toast.makeText(this, R.string.msg_reset_defaults, Toast.LENGTH_SHORT).show();
    }
}
