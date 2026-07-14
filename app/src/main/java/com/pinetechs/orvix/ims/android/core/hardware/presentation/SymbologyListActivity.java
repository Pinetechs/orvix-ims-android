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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerOptionDefinition;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerOptionKey;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerOptionType;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfileDefaults;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfileSettings;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologyCatalog;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologyDefinition;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerSymbologySettings;
import com.pinetechs.orvix.ims.android.core.util.Constants;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SymbologyListActivity extends AppCompatActivity {

    private final Gson gson = new Gson();
    private ScannerProfile profile;
    private ScannerProfileSettings settings;
    private LinearLayout symbologyContainer;
    private ChipGroup filterChipGroup;
    private String currentFilter = "ALL";
    private final Map<BarcodeSymbology, SymbologyRow> symbologyRows = new EnumMap<>(BarcodeSymbology.class);

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

        profile = ScannerProfile.valueOf(profileStr);
        settings = gson.fromJson(settingsJson, ScannerProfileSettings.class);

        initViews();
        renderSymbologyRows();
    }

    private void initViews() {
        symbologyContainer = findViewById(R.id.symbologyContainer);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        TextView profileTv = findViewById(R.id.profileTextView);
        profileTv.setText("Profile: " + profile.getDisplayName());

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.doneButton).setOnClickListener(v -> saveAndFinish());

        setupFilterChips();
    }

    private void setupFilterChips() {
        String[] filters = {"ALL", "1D", "2D", "Retail", "GS1", "Postal"};
        for (String filter : filters) {
            Chip chip = new Chip(this);
            chip.setText(filter);
            chip.setCheckable(true);
            chip.setChecked(filter.equals(currentFilter));
            chip.setOnClickListener(v -> {
                currentFilter = filter;
                renderSymbologyRows();
            });
            filterChipGroup.addView(chip);
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
            symbologyContainer.addView(createSectionLabel(entry.getKey() + " SYMBOLOGIES"));
            for (ScannerSymbologyDefinition definition : entry.getValue()) {
                BarcodeSymbology symbology = definition.getSymbology();
                ScannerSymbologySettings typeSettings = settings.getOrCreateSymbologySettings(symbology);
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

    private SymbologyRow createSymbologyRow(ScannerSymbologyDefinition definition, ScannerSymbologySettings typeSettings) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(8), dp(8), dp(8));
        root.setBackgroundResource(R.drawable.bg_input_soft);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootParams.bottomMargin = dp(8);
        root.setLayoutParams(rootParams);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(android.view.Gravity.CENTER_VERTICAL);

        SwitchMaterial enabledSwitch = new SwitchMaterial(this);
        enabledSwitch.setText(definition.getSymbology().getDisplayName());
        enabledSwitch.setTextColor(getResources().getColor(R.color.orvix_black));
        enabledSwitch.setChecked(typeSettings.isEnabled());
        enabledSwitch.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(enabledSwitch);

        Button configureButton = new Button(this);
        configureButton.setText("Configure");
        configureButton.setTextSize(12);
        configureButton.setAllCaps(false);
        configureButton.setMinWidth(0);
        configureButton.setPadding(dp(10), 0, dp(10), 0);
        configureButton.setVisibility(definition.hasOptions() ? View.VISIBLE : View.GONE);
        top.addView(configureButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)));
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
        configureButton.setOnClickListener(v -> showSymbologyOptionsDialog(definition, typeSettings, row));
        return row;
    }

    private void showSymbologyOptionsDialog(ScannerSymbologyDefinition definition, ScannerSymbologySettings typeSettings, SymbologyRow row) {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(8), dp(24), dp(8));
        scrollView.addView(content);

        Map<ScannerOptionKey, OptionEditor> editors = new EnumMap<>(ScannerOptionKey.class);
        for (ScannerOptionDefinition option : definition.getOptions()) {
            if (option.getKey() == ScannerOptionKey.MIN_LENGTH || option.getKey() == ScannerOptionKey.MAX_LENGTH) continue;
            OptionEditor editor = createOptionEditor(option, typeSettings);
            editors.put(option.getKey(), editor);
            content.addView(editor.root);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(definition.getSymbology().getDisplayName() + " settings")
                .setView(scrollView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply", (dialog, which) -> {
                    if (saveOptionEditors(definition, typeSettings, editors)) {
                        updateRowSummary(row, definition, typeSettings);
                    }
                }).show();
    }

    private OptionEditor createOptionEditor(ScannerOptionDefinition option, ScannerSymbologySettings typeSettings) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(12);
        root.setLayoutParams(params);

        String currentValue = typeSettings.getOption(option.getKey(), option.getDefaultValue());
        View inputView;

        if (option.getType() == ScannerOptionType.BOOLEAN) {
            SwitchMaterial sw = new SwitchMaterial(this);
            sw.setText(option.getDisplayName());
            sw.setChecked(Boolean.parseBoolean(currentValue));
            sw.setTextColor(getResources().getColor(R.color.orvix_black));
            root.addView(sw);
            inputView = sw;
        } else if (option.getType() == ScannerOptionType.INTEGER) {
            TextInputLayout il = new TextInputLayout(this);
            il.setHint(option.getDisplayName());
            TextInputEditText et = new TextInputEditText(il.getContext());
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.setText(currentValue);
            il.addView(et);
            root.addView(il);
            inputView = et;
        } else {
            TextView t = new TextView(this);
            t.setText(option.getDisplayName());
            t.setTextSize(13);
            root.addView(t);
            Spinner s = new Spinner(this);
            List<String> labels = new ArrayList<>(option.getChoices().values());
            ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s.setAdapter(ad);
            s.setSelection(new ArrayList<>(option.getChoices().keySet()).indexOf(currentValue));
            s.setTag(new ArrayList<>(option.getChoices().keySet()));
            root.addView(s, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));
            inputView = s;
        }
        return new OptionEditor(option, root, inputView);
    }

    private boolean saveOptionEditors(ScannerSymbologyDefinition def, ScannerSymbologySettings typeSettings, Map<ScannerOptionKey, OptionEditor> editors) {
        for (OptionEditor ed : editors.values()) {
            String val;
            if (ed.definition.getType() == ScannerOptionType.BOOLEAN) {
                val = String.valueOf(((SwitchMaterial) ed.input).isChecked());
            } else if (ed.definition.getType() == ScannerOptionType.INTEGER) {
                val = ((TextInputEditText) ed.input).getText().toString();
            } else {
                val = ((List<String>) ed.input.getTag()).get(((Spinner) ed.input).getSelectedItemPosition());
            }
            typeSettings.setOption(ed.definition.getKey(), val);
        }
        return true;
    }

    private void updateRowSummary(SymbologyRow row, ScannerSymbologyDefinition definition, ScannerSymbologySettings typeSettings) {
        StringBuilder sb = new StringBuilder(definition.getSymbology().getCategory());
        if (ScannerProfileDefaults.isDefaultEnabled(profile, definition.getSymbology())) {
            sb.append(" • Profile default");
        }
        if (definition.findOption(ScannerOptionKey.MIN_LENGTH) != null) {
            sb.append(" • Global length ").append(settings.getMinScanLength()).append("–").append(settings.getMaxScanLength());
        }
        if (!typeSettings.isEnabled()) sb.append(" • Disabled");
        row.summary.setText(sb.toString());
    }

    private void saveAndFinish() {
        Intent data = new Intent();
        data.putExtra(Constants.EXTRA_SCANNER_SETTINGS_JSON, gson.toJson(settings));
        setResult(RESULT_OK, data);
        finish();
    }

    private int dp(int px) {
        return (int) (px * getResources().getDisplayMetrics().density);
    }

    private static class SymbologyRow {
        final View root;
        final SwitchMaterial enabledSwitch;
        final Button configureButton;
        final TextView summary;
        SymbologyRow(View root, SwitchMaterial es, Button cb, TextView sm) {
            this.root = root; this.enabledSwitch = es; this.configureButton = cb; this.summary = sm;
        }
    }

    private static class OptionEditor {
        final ScannerOptionDefinition definition;
        final View root;
        final View input;
        OptionEditor(ScannerOptionDefinition def, View r, View i) {
            this.definition = def; this.root = r; this.input = i;
        }
    }
}
