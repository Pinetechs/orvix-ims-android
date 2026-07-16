package com.pinetechs.orvix.ims.android.core.hardware.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pinetechs.orvix.ims.android.core.presentation.BaseActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerFactory;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerInterface;
import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerOptionDefinition;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerOptionKey;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerOptionType;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfileSettings;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologyCatalog;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologyDefinition;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologySettings;
import com.pinetechs.orvix.ims.android.core.util.Constants;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SymbologyListActivity extends BaseActivity {

    private final Gson gson = new Gson();
    private ScannerProfile profile;
    private ScannerProfileSettings settings;
    private LinearLayout symbologyContainer;
    private ChipGroup filterChipGroup;
    private MaterialButton detectSymbologyButton;
    private TextView detectionStatusTextView;
    private String currentFilter = "ALL";
    private final Map<BarcodeSymbology, SymbologyRow> symbologyRows =
            new EnumMap<>(BarcodeSymbology.class);

    private ScannerInterface detectionScanner;
    private boolean detectionActive;
    private boolean detectionResultVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symbology_list);

        String profileStr = getIntent().getStringExtra(Constants.EXTRA_SCANNER_PROFILE);
        String settingsJson = getIntent().getStringExtra(Constants.EXTRA_SCANNER_SETTINGS_JSON);

        if (profileStr == null || settingsJson == null) {
            finish();
            return;
        }

        try {
            profile = ScannerProfile.valueOf(profileStr);
            settings = gson.fromJson(settingsJson, ScannerProfileSettings.class);
        } catch (RuntimeException exception) {
            finish();
            return;
        }

        if (settings == null) {
            finish();
            return;
        }

        initViews();
        renderSymbologyRows();
    }

    private void initViews() {
        symbologyContainer = findViewById(R.id.symbologyContainer);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        detectSymbologyButton = findViewById(R.id.detectSymbologyButton);
        detectionStatusTextView = findViewById(R.id.detectionStatusTextView);

        TextView profileTv = findViewById(R.id.profileTextView);
        profileTv.setText(profile.getDisplayName(this));

        findViewById(R.id.backButton).setOnClickListener(v -> {
            stopDetectionSession();
            finish();
        });
        findViewById(R.id.doneButton).setOnClickListener(v -> saveAndFinish());
        detectSymbologyButton.setOnClickListener(v -> {
            if (detectionActive) stopDetectionSession();
            else startDetectionSession();
        });

        setupFilterChips();
    }

    private void setupFilterChips() {
        String[] filters = {"ALL", "1D", "2D", "Retail", "GS1", "Postal"};
        for (String filter : filters) {
            Chip chip = new Chip(this);
            chip.setText(getCategoryDisplayName(filter));
            chip.setCheckable(true);
            chip.setChecked(filter.equals(currentFilter));
            chip.setOnClickListener(v -> {
                currentFilter = filter;
                renderSymbologyRows();
            });
            filterChipGroup.addView(chip);
        }
    }

    private void startDetectionSession() {
        stopDetectionSession();
        detectionResultVisible = false;

        ScannerInterface scanner = ScannerFactory.getScanner(this, profile);
        scanner.setOnScanListener((data, type) ->
                detectSymbologyButton.post(() -> handleDetectedBarcode(data, type))
        );

        if (!scanner.init()) {
            scanner.close();
            showDetectionUnavailable(getString(R.string.err_connection_failed));
            return;
        }

        scanner.register(this);
        if (!scanner.enterSymbologyDetectionMode()) {
            scanner.unregister(this);
            scanner.close();
            showDetectionUnavailable("Hardware detection not supported");
            return;
        }

        detectionScanner = scanner;
        detectionActive = true;

        Set<BarcodeSymbology> supported = scanner.getSupportedSymbologies();
        detectionStatusTextView.setText(getString(R.string.msg_ready_for_next));
        detectSymbologyButton.setText(R.string.stop);
    }

    private void showDetectionUnavailable(String message) {
        detectionStatusTextView.setText(message);
        detectSymbologyButton.setText("Detect");
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void handleDetectedBarcode(String value, String typeName) {
        if (!detectionActive || detectionResultVisible) return;

        detectionResultVisible = true;
        BarcodeSymbology detectedType = BarcodeSymbology.fromStorageName(typeName);
        stopDetectionSession();

        if (detectedType == null) {
            showUnknownDetectionResult(value, typeName);
            return;
        }

        ScannerSymbologySettings detectedSettings =
                settings.getOrCreateSymbologySettings(detectedType);
        boolean alreadyEnabled = detectedSettings.isEnabled();

        StringBuilder message = new StringBuilder();
        message.append("Value\n").append(value)
                .append("\n\nSymbology\n").append(detectedType.getDisplayName())
                .append("\n\nLength\n").append(value != null ? value.length() : 0)
                .append(" characters");

        if (alreadyEnabled) {
            message.append("\n\nThis symbology is already enabled in the ")
                    .append(profile.getDisplayName()).append(" profile.");
        } else {
            message.append("\n\nAdd this symbology to the ")
                    .append(profile.getDisplayName())
                    .append(" profile using its safe default options?");
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(detectedType.getDisplayName())
                .setMessage(message.toString())
                .setNegativeButton(R.string.close_label, (dialog, which) -> detectionResultVisible = false)
                .setNeutralButton(R.string.scan_another, (dialog, which) -> {
                    detectionResultVisible = false;
                    startDetectionSession();
                });

        if (!alreadyEnabled) {
            builder.setPositiveButton(R.string.add_to_profile, (dialog, which) -> {
                detectedSettings.setEnabled(true);
                detectionResultVisible = false;
                renderSymbologyRows();
                detectionStatusTextView.setText(detectedType.getDisplayName());
                Toast.makeText(
                        this,
                        detectedType.getDisplayName(),
                        Toast.LENGTH_SHORT
                ).show();
            });
        } else {
            builder.setPositiveButton(R.string.continue_label, (dialog, which) -> detectionResultVisible = false);
        }

        builder.setOnCancelListener(dialog -> detectionResultVisible = false);
        builder.show();
    }

    private void showUnknownDetectionResult(String value, String rawType) {
        String message = "The scanner decoded the value, but its type could not be mapped "
                + "to the Orvix symbology catalog.\n\nValue\n"
                + value + "\n\nProvider type\n" + rawType;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.unknown_barcode_type)
                .setMessage(message)
                .setNegativeButton(R.string.close_label, (dialog, which) -> detectionResultVisible = false)
                .setPositiveButton(R.string.scan_another, (dialog, which) -> {
                    detectionResultVisible = false;
                    startDetectionSession();
                })
                .setOnCancelListener(dialog -> detectionResultVisible = false)
                .show();
    }

    private void stopDetectionSession() {
        ScannerInterface scanner = detectionScanner;
        detectionScanner = null;
        detectionActive = false;
        detectSymbologyButton.setText("Detect");

        if (scanner != null) {
            try {
                scanner.exitSymbologyDetectionMode();
            } finally {
                scanner.unregister(this);
                scanner.close();
            }
        }
    }

    private void renderSymbologyRows() {
        symbologyContainer.removeAllViews();
        symbologyRows.clear();

        Map<String, List<ScannerSymbologyDefinition>> groupedMap = new LinkedHashMap<>();
        for (ScannerSymbologyDefinition definition : ScannerSymbologyCatalog.getDefinitions()) {
            String category = definition.getSymbology().getCategory();
            if (!currentFilter.equals("ALL") && !currentFilter.equals(category)) continue;

            if (!groupedMap.containsKey(category)) {
                groupedMap.put(category, new ArrayList<>());
            }
            groupedMap.get(category).add(definition);
        }

        for (Map.Entry<String, List<ScannerSymbologyDefinition>> entry : groupedMap.entrySet()) {
            String category = entry.getKey();
            String displayCategory = getCategoryDisplayName(category);
            symbologyContainer.addView(createSectionLabel(getString(R.string.symbology_header_plain, displayCategory)));
            for (ScannerSymbologyDefinition definition : entry.getValue()) {
                BarcodeSymbology symbology = definition.getSymbology();
                ScannerSymbologySettings typeSettings =
                        settings.getOrCreateSymbologySettings(symbology);
                SymbologyRow row = createSymbologyRow(definition, typeSettings);
                symbologyRows.put(symbology, row);
                symbologyContainer.addView(row.root);
            }
        }
    }

    private TextView createSectionLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(11);
        label.setTextColor(getResources().getColor(R.color.orvix_text_sub));
        label.setAllCaps(true);
        label.setPadding(0, dp(16), 0, dp(6));
        return label;
    }

    private SymbologyRow createSymbologyRow(
            ScannerSymbologyDefinition definition,
            ScannerSymbologySettings typeSettings
    ) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(8), dp(8), dp(8));
        root.setBackgroundResource(R.drawable.bg_input_soft);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rootParams.bottomMargin = dp(8);
        root.setLayoutParams(rootParams);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(android.view.Gravity.CENTER_VERTICAL);

        SwitchMaterial enabledSwitch = new SwitchMaterial(this);
        enabledSwitch.setText(definition.getSymbology().getDisplayName());
        enabledSwitch.setTextColor(getResources().getColor(R.color.orvix_black));
        enabledSwitch.setChecked(typeSettings.isEnabled());
        enabledSwitch.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));
        top.addView(enabledSwitch);

        Button configureButton = new Button(this);
        configureButton.setText("Configure");
        configureButton.setTextSize(12);
        configureButton.setAllCaps(false);
        configureButton.setMinWidth(0);
        configureButton.setPadding(dp(10), 0, dp(10), 0);
        configureButton.setVisibility(definition.hasOptions() ? View.VISIBLE : View.GONE);
        top.addView(
                configureButton,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(42))
        );
        root.addView(top);

        TextView summary = new TextView(this);
        summary.setTextSize(11);
        summary.setTextColor(getResources().getColor(R.color.orvix_text_sub));
        summary.setPadding(dp(4), 0, dp(4), dp(4));
        root.addView(summary);

        SymbologyRow row = new SymbologyRow(root, enabledSwitch, configureButton, summary);
        updateRowSummary(row, definition, typeSettings);

        enabledSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
            typeSettings.setEnabled(checked);
            updateRowSummary(row, definition, typeSettings);
        });
        configureButton.setOnClickListener(
                v -> showSymbologyOptionsDialog(definition, typeSettings, row)
        );
        return row;
    }

    private void showSymbologyOptionsDialog(
            ScannerSymbologyDefinition definition,
            ScannerSymbologySettings typeSettings,
            SymbologyRow row
    ) {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(8), dp(24), dp(8));
        scrollView.addView(content);

        Map<ScannerOptionKey, OptionEditor> editors = new EnumMap<>(ScannerOptionKey.class);
        for (ScannerOptionDefinition option : definition.getOptions()) {
            if (option.getKey() == ScannerOptionKey.MIN_LENGTH
                    || option.getKey() == ScannerOptionKey.MAX_LENGTH) {
                continue;
            }
            OptionEditor editor = createOptionEditor(option, typeSettings);
            editors.put(option.getKey(), editor);
            content.addView(editor.root);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(definition.getSymbology().getDisplayName() + " settings")
                .setView(scrollView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply", (dialog, which) -> {
                    if (saveOptionEditors(typeSettings, editors)) {
                        updateRowSummary(row, definition, typeSettings);
                    }
                }).show();
    }

    private OptionEditor createOptionEditor(
            ScannerOptionDefinition option,
            ScannerSymbologySettings typeSettings
    ) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(12);
        root.setLayoutParams(params);

        String currentValue = typeSettings.getOption(option.getKey(), option.getDefaultValue());
        View inputView;
        
        String displayName = getOptionDisplayName(option);

        if (option.getType() == ScannerOptionType.BOOLEAN) {
            SwitchMaterial sw = new SwitchMaterial(this);
            sw.setText(displayName);
            sw.setChecked(Boolean.parseBoolean(currentValue));
            sw.setTextColor(getResources().getColor(R.color.orvix_black));
            root.addView(sw);
            inputView = sw;
        } else if (option.getType() == ScannerOptionType.INTEGER) {
            TextInputLayout inputLayout = new TextInputLayout(this);
            inputLayout.setHint(displayName);
            TextInputEditText editText = new TextInputEditText(inputLayout.getContext());
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setText(currentValue);
            inputLayout.addView(editText);
            root.addView(inputLayout);
            inputView = editText;
        } else {
            TextView title = new TextView(this);
            title.setText(displayName);
            title.setTextSize(13);
            root.addView(title);

            Spinner spinner = new Spinner(this);
            List<String> labels = new ArrayList<>(option.getChoices().values());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    labels
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            List<String> keys = new ArrayList<>(option.getChoices().keySet());
            int selectedIndex = keys.indexOf(currentValue);
            spinner.setSelection(Math.max(0, selectedIndex));
            spinner.setTag(keys);
            root.addView(
                    spinner,
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50))
            );
            inputView = spinner;
        }
        return new OptionEditor(option, root, inputView);
    }

    private String getCategoryDisplayName(String category) {
        String key = "category_" + category.toLowerCase(Locale.ROOT);
        int resId = getResources().getIdentifier(key, "string", getPackageName());
        return resId != 0 ? getString(resId) : category;
    }

    private String getOptionDisplayName(ScannerOptionDefinition option) {
        String key = "opt_" + option.getKey().name().toLowerCase(Locale.ROOT);
        int resId = getResources().getIdentifier(key, "string", getPackageName());
        return resId != 0 ? getString(resId) : option.getDisplayName();
    }

    @SuppressWarnings("unchecked")
    private boolean saveOptionEditors(
            ScannerSymbologySettings typeSettings,
            Map<ScannerOptionKey, OptionEditor> editors
    ) {
        for (OptionEditor editor : editors.values()) {
            String value;
            if (editor.definition.getType() == ScannerOptionType.BOOLEAN) {
                value = String.valueOf(((SwitchMaterial) editor.input).isChecked());
            } else if (editor.definition.getType() == ScannerOptionType.INTEGER) {
                CharSequence text = ((TextInputEditText) editor.input).getText();
                value = text != null ? text.toString().trim() : "";
                if (value.isEmpty()) {
                    Toast.makeText(
                            this,
                            editor.definition.getDisplayName() + " is required",
                            Toast.LENGTH_SHORT
                    ).show();
                    return false;
                }
            } else {
                List<String> keys = (List<String>) editor.input.getTag();
                value = keys.get(((Spinner) editor.input).getSelectedItemPosition());
            }
            typeSettings.setOption(editor.definition.getKey(), value);
        }
        return true;
    }

    private void updateRowSummary(
            SymbologyRow row,
            ScannerSymbologyDefinition definition,
            ScannerSymbologySettings typeSettings
    ) {
        StringBuilder summary = new StringBuilder(getCategoryDisplayName(definition.getSymbology().getCategory()));
        if (ScannerSymbologyCatalog.isCoreDefault(definition.getSymbology())) {
            summary.append(" • ").append(getString(R.string.standard_default));
        }
        if (definition.findOption(ScannerOptionKey.MIN_LENGTH) != null) {
            summary.append(" • ").append(getString(R.string.opt_min_length)).append(" ")
                    .append(settings.getMinScanLength()).append("–").append(settings.getMaxScanLength());
        }
        if (!typeSettings.isEnabled()) summary.append(" • ").append(getString(R.string.disabled_label));
        row.summary.setText(summary.toString());
    }

    private void saveAndFinish() {
        stopDetectionSession();
        Intent data = new Intent();
        data.putExtra(Constants.EXTRA_SCANNER_SETTINGS_JSON, gson.toJson(settings));
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (detectionActive) stopDetectionSession();
    }

    @Override
    protected void onDestroy() {
        stopDetectionSession();
        super.onDestroy();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private static class SymbologyRow {
        final View root;
        final SwitchMaterial enabledSwitch;
        final Button configureButton;
        final TextView summary;

        SymbologyRow(
                View root,
                SwitchMaterial enabledSwitch,
                Button configureButton,
                TextView summary
        ) {
            this.root = root;
            this.enabledSwitch = enabledSwitch;
            this.configureButton = configureButton;
            this.summary = summary;
        }
    }

    private static class OptionEditor {
        final ScannerOptionDefinition definition;
        final View root;
        final View input;

        OptionEditor(ScannerOptionDefinition definition, View root, View input) {
            this.definition = definition;
            this.root = root;
            this.input = input;
        }
    }
}
