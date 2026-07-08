package com.pinetechs.orvix.ims.android.scan.presentation;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

public class ScanActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_NUMBER = "task_number";
    public static final String EXTRA_INVENTORY_DOMAIN = "inventory_domain";
    public static final String EXTRA_LOCATION_CODE = "location_code";
    public static final String EXTRA_LOCATION_NAME = "location_name";

    private ScanViewModel viewModel;
    private TextView titleTextView;
    private TextView locationTextView;
    private TextView resultTextView;
    private TextInputEditText barcodeEditText;
    private Button scanButton;
    private ProgressBar progressBar;

    private Long taskId;
    private String taskNumber;
    private String inventoryDomain;
    private String locationCode;
    private String locationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        taskId = getIntent().hasExtra(EXTRA_TASK_ID) ? getIntent().getLongExtra(EXTRA_TASK_ID, -1L) : null;
        if (taskId != null && taskId == -1L) {
            taskId = null;
        }
        taskNumber = getIntent().getStringExtra(EXTRA_TASK_NUMBER);
        inventoryDomain = getIntent().getStringExtra(EXTRA_INVENTORY_DOMAIN);
        locationCode = getIntent().getStringExtra(EXTRA_LOCATION_CODE);
        locationName = getIntent().getStringExtra(EXTRA_LOCATION_NAME);

        viewModel = new ViewModelProvider(this).get(ScanViewModel.class);

        titleTextView = findViewById(R.id.titleTextView);
        locationTextView = findViewById(R.id.locationTextView);
        resultTextView = findViewById(R.id.resultTextView);
        barcodeEditText = findViewById(R.id.barcodeEditText);
        scanButton = findViewById(R.id.scanButton);
        progressBar = findViewById(R.id.progressBar);

        titleTextView.setText((taskNumber != null ? taskNumber : "Task") + " - Scan");
        locationTextView.setText("Location: " + (locationCode != null ? locationCode : "-")
                + " - "
                + (locationName != null ? locationName : ""));

        scanButton.setOnClickListener(v -> submitScan());

        barcodeEditText.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterPressed = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            boolean donePressed = actionId == EditorInfo.IME_ACTION_DONE;

            if (enterPressed || donePressed) {
                submitScan();
                return true;
            }
            return false;
        });

        observeScanState();
        barcodeEditText.requestFocus();
    }

    private void submitScan() {
        String barcode = barcodeEditText.getText() != null ? barcodeEditText.getText().toString() : "";
        viewModel.scan(taskId, barcode, locationCode, resolveScanType());
    }

    private String resolveScanType() {
        if ("VEHICLE".equalsIgnoreCase(inventoryDomain)) {
            return "VIN";
        }
        return "BARCODE";
    }

    private void observeScanState() {
        viewModel.getScanState().observe(this, state -> {
            if (state == null) {
                return;
            }

            if (state.getStatus() == Resource.Status.LOADING) {
                setLoading(true);
            } else if (state.getStatus() == Resource.Status.SUCCESS) {
                setLoading(false);
                showScanResult(state.getData());
                barcodeEditText.setText("");
                barcodeEditText.requestFocus();
            } else if (state.getStatus() == Resource.Status.ERROR) {
                setLoading(false);
                resultTextView.setText(state.getMessage());
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
                barcodeEditText.requestFocus();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        scanButton.setEnabled(!loading);
    }

    private void showScanResult(ScanResponse response) {
        if (response == null) {
            resultTextView.setText("Scan completed");
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(response.getStatus() != null ? response.getStatus() : "SUCCESS");

        if (response.getMessage() != null && !response.getMessage().trim().isEmpty()) {
            builder.append("\n").append(response.getMessage());
        }

        if (response.getItem() != null) {
            String displayCode = response.getItem().getVin() != null
                    ? response.getItem().getVin()
                    : response.getItem().getBarcode();

            if (displayCode != null) {
                builder.append("\nItem: ").append(displayCode);
            }

            if (response.getItem().getExpectedLocationCode() != null) {
                builder.append("\nExpected: ").append(response.getItem().getExpectedLocationCode());
            }

            if (response.getItem().getScannedLocationCode() != null) {
                builder.append("\nScanned: ").append(response.getItem().getScannedLocationCode());
            }
        }

        resultTextView.setText(builder.toString());
    }
}
