package com.pinetechs.orvix.ims.android.scan.presentation;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

    private TextInputLayout scanInputLayout;
    private TextView scanTaskSummaryTextView;
    private TextView scanLocationSummaryTextView;
    private TextView scanDomainChipTextView;
    private TextView scanInputTitleTextView;
    private TextView resultStatusTextView;
    private TextView resultCodeTextView;
    private TextView resultMessageTextView;
    private TextView expectedLocationTextView;
    private TextView scannedLocationTextView;
    private LinearLayout resultDetailsContainer;

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

        scanInputLayout = findViewById(R.id.scanInputLayout);
        scanTaskSummaryTextView = findViewById(R.id.scanTaskSummaryTextView);
        scanLocationSummaryTextView = findViewById(R.id.scanLocationSummaryTextView);
        scanDomainChipTextView = findViewById(R.id.scanDomainChipTextView);
        scanInputTitleTextView = findViewById(R.id.scanInputTitleTextView);
        resultStatusTextView = findViewById(R.id.resultStatusTextView);
        resultCodeTextView = findViewById(R.id.resultCodeTextView);
        resultMessageTextView = findViewById(R.id.resultMessageTextView);
        expectedLocationTextView = findViewById(R.id.expectedLocationTextView);
        scannedLocationTextView = findViewById(R.id.scannedLocationTextView);
        resultDetailsContainer = findViewById(R.id.resultDetailsContainer);

        bindHeader();
        configureScanMode();
        showReadyResult();

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

    private void bindHeader() {
        String displayTask = taskNumber != null ? taskNumber : "Task";
        String displayLocationCode = locationCode != null ? locationCode : "-";
        String displayLocationName = locationName != null ? locationName : "";
        String domain = inventoryDomain != null ? inventoryDomain : "-";

        titleTextView.setText("Scan Inventory");
        locationTextView.setText(displayTask + "  •  " + displayLocationCode + " - " + displayLocationName);

        if (scanTaskSummaryTextView != null) {
            scanTaskSummaryTextView.setText(displayTask);
        }
        if (scanLocationSummaryTextView != null) {
            scanLocationSummaryTextView.setText(displayLocationCode + " - " + displayLocationName);
        }
        if (scanDomainChipTextView != null) {
            scanDomainChipTextView.setText(domain);
        }
    }

    private void configureScanMode() {
        String scanType = resolveScanType();
        String label = "VIN".equals(scanType) ? "VIN" : "Barcode";

        if (scanInputTitleTextView != null) {
            scanInputTitleTextView.setText("Enter / Scan " + label);
        }
        if (scanInputLayout != null) {
            scanInputLayout.setHint("Scan or enter " + label);
        }
        scanButton.setText("Scan " + label);
    }

    private void submitScan() {
        String barcode = barcodeEditText.getText() != null ? barcodeEditText.getText().toString().trim() : "";
        if (barcode.isEmpty()) {
            Toast.makeText(this, "Scan value is required", Toast.LENGTH_SHORT).show();
            barcodeEditText.requestFocus();
            return;
        }
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
                showErrorResult(state.getMessage());
                Toast.makeText(this, state.getMessage(), Toast.LENGTH_LONG).show();
                barcodeEditText.requestFocus();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        scanButton.setEnabled(!loading);
    }

    private void showReadyResult() {
        if (resultStatusTextView != null) {
            resultStatusTextView.setText("READY");
            resultStatusTextView.setBackgroundResource(R.drawable.bg_chip_blue);
            resultStatusTextView.setTextColor(getResources().getColor(R.color.orvix_primary));
        }
        if (resultCodeTextView != null) {
            resultCodeTextView.setText("No scan yet");
        }
        if (resultMessageTextView != null) {
            resultMessageTextView.setText("Waiting for scanner input.");
        }
        if (expectedLocationTextView != null) {
            expectedLocationTextView.setText("Expected\n-");
        }
        if (scannedLocationTextView != null) {
            scannedLocationTextView.setText("Scanned\n-");
        }
    }

    private void showErrorResult(String message) {
        String errorMessage = message != null && !message.trim().isEmpty() ? message : "Scan failed";
        if (resultStatusTextView != null) {
            resultStatusTextView.setText("ERROR");
            resultStatusTextView.setBackgroundResource(R.drawable.bg_chip_danger);
            resultStatusTextView.setTextColor(getResources().getColor(R.color.danger));
        }
        if (resultCodeTextView != null) {
            resultCodeTextView.setText("Scan failed");
        }
        if (resultMessageTextView != null) {
            resultMessageTextView.setText(errorMessage);
        }
        if (resultTextView != null) {
            resultTextView.setText(errorMessage);
        }
    }

    private void showScanResult(ScanResponse response) {
        if (response == null) {
            setResultHeader("SUCCESS");
            if (resultCodeTextView != null) {
                resultCodeTextView.setText("Scan completed");
            }
            if (resultMessageTextView != null) {
                resultMessageTextView.setText("The scan was submitted successfully.");
            }
            return;
        }

        String status = response.getStatus() != null ? response.getStatus() : "SUCCESS";
        String message = response.getMessage() != null && !response.getMessage().trim().isEmpty()
                ? response.getMessage()
                : "Scan completed successfully.";

        setResultHeader(status);

        String displayCode = "-";
        String expected = "-";
        String scanned = locationCode != null ? locationCode : "-";

        if (response.getItem() != null) {
            displayCode = response.getItem().getVin() != null
                    ? response.getItem().getVin()
                    : response.getItem().getBarcode();

            if (displayCode == null || displayCode.trim().isEmpty()) {
                displayCode = response.getItem().getItemCode() != null ? response.getItem().getItemCode() : "-";
            }

            if (response.getItem().getExpectedLocationCode() != null) {
                expected = response.getItem().getExpectedLocationCode();
            }

            if (response.getItem().getScannedLocationCode() != null) {
                scanned = response.getItem().getScannedLocationCode();
            }

            if (response.getItem().getName() != null && !response.getItem().getName().trim().isEmpty()) {
                message = response.getItem().getName() + "\n" + message;
            }
        }

        if (resultCodeTextView != null) {
            resultCodeTextView.setText(displayCode);
        }
        if (resultMessageTextView != null) {
            resultMessageTextView.setText(message);
        }
        if (expectedLocationTextView != null) {
            expectedLocationTextView.setText("Expected\n" + expected);
        }
        if (scannedLocationTextView != null) {
            scannedLocationTextView.setText("Scanned\n" + scanned);
        }

        if (resultTextView != null) {
            resultTextView.setText(buildLegacyResultText(response));
        }
    }

    private void setResultHeader(String status) {
        if (resultStatusTextView == null) {
            return;
        }
        String normalized = status != null ? status.toUpperCase() : "SUCCESS";
        resultStatusTextView.setText(normalized.replace('_', ' '));
        if (normalized.contains("MISMATCH") || normalized.contains("ERROR") || normalized.contains("FAIL")) {
            resultStatusTextView.setBackgroundResource(R.drawable.bg_chip_danger);
            resultStatusTextView.setTextColor(getResources().getColor(R.color.danger));
        } else if (normalized.contains("DUPLICATE") || normalized.contains("WARN")) {
            resultStatusTextView.setBackgroundResource(R.drawable.bg_chip_warning);
            resultStatusTextView.setTextColor(getResources().getColor(R.color.warning));
        } else {
            resultStatusTextView.setBackgroundResource(R.drawable.bg_chip_success);
            resultStatusTextView.setTextColor(getResources().getColor(R.color.success));
        }
    }

    private String buildLegacyResultText(ScanResponse response) {
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

        return builder.toString();
    }
}
