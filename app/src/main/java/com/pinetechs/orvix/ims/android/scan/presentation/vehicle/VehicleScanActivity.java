package com.pinetechs.orvix.ims.android.scan.presentation.vehicle;

import android.device.scanner.configuration.Constants;
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
import com.pinetechs.orvix.ims.android.core.hardware.ScannerFactory;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerInterface;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;

public class VehicleScanActivity extends AppCompatActivity {

    private VehicleScanViewModel viewModel;
    private ScannerInterface scannerManager;
    private TextInputEditText vinEditText;
    private Button submitButton;
    private ProgressBar progressBar;
    
    private TextView resultStatusChip, resultCodeTv, resultMessageTv;
    private TextView expectedLocationTv, currentLocationTv;

    private Long taskId;
    private String taskNumber, locationCode, locationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_scan);

        initData();
        initViews();
        setupHeader();
        initScanner();

        viewModel = new ViewModelProvider(this).get(VehicleScanViewModel.class);
        observeScanState();
    }

    private void initScanner() {
        scannerManager = ScannerFactory.getScanner(this);
        scannerManager.setOnScanListener(data -> {
            runOnUiThread(() -> {
                vinEditText.setText(data);
                performScan();
            });
        });
        if (!scannerManager.init()) {
            // Silently fail or show message based on preference
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scannerManager != null) {
            scannerManager.register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scannerManager != null) {
            scannerManager.unregister(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scannerManager != null) {
            scannerManager.close();
        }
    }

    private void initData() {
        taskId = getIntent().getLongExtra("task_id", -1L);
        if (taskId == -1L) taskId = null;
        taskNumber = getIntent().getStringExtra("task_number");
        locationCode = getIntent().getStringExtra("location_code");
        locationName = getIntent().getStringExtra("location_name");
    }

    private void initViews() {
        vinEditText = findViewById(R.id.vinEditText);
        submitButton = findViewById(R.id.submitScanButton);
        progressBar = findViewById(R.id.progressBar);
        
        resultStatusChip = findViewById(R.id.resultStatusChip);
        resultCodeTv = findViewById(R.id.resultCodeTextView);
        resultMessageTv = findViewById(R.id.resultMessageTextView);
        expectedLocationTv = findViewById(R.id.expectedLocationTv);
        currentLocationTv = findViewById(R.id.currentLocationTv);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        submitButton.setOnClickListener(v -> performScan());

        vinEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performScan();
                return true;
            }
            return false;
        });
    }

    private void setupHeader() {
        TextView locationTv = findViewById(R.id.locationNameTextView);
        TextView taskRefTv = findViewById(R.id.taskRefTextView);
        
        locationTv.setText(locationName != null ? locationName : (locationCode != null ? locationCode : "Unknown Location"));
        taskRefTv.setText("Task: " + (taskNumber != null ? taskNumber : "---"));
    }

    private void performScan() {
        String vin = vinEditText.getText() != null ? vinEditText.getText().toString().trim() : "";
        if (vin.isEmpty()) {
            Toast.makeText(this, "Please enter or scan a VIN", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.scanVehicle(taskId, vin, locationCode);
    }

    private void observeScanState() {
        viewModel.getScanState().observe(this, state -> {
            if (state == null) return;

            if (state.getStatus() == Resource.Status.LOADING) {
                setLoading(true);
            } else if (state.getStatus() == Resource.Status.SUCCESS) {
                setLoading(false);
                displayResult(state.getData());
                vinEditText.setText("");
                vinEditText.requestFocus();
            } else if (state.getStatus() == Resource.Status.ERROR) {
                setLoading(false);
                displayError(state.getMessage());
                vinEditText.requestFocus();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!loading);
        vinEditText.setEnabled(!loading);
    }

    private void displayResult(ScanResponse response) {
        if (response == null) return;

        String status = response.getStatus() != null ? response.getStatus() : "SUCCESS";
        resultStatusChip.setText(status.replace("_", " "));
        applyStatusStyle(resultStatusChip, status);

        if (response.getItem() != null) {
            resultCodeTv.setText(response.getItem().getVin());
            resultMessageTv.setText(response.getItem().getName() != null ? response.getItem().getName() : response.getMessage());
            expectedLocationTv.setText(response.getItem().getExpectedLocationCode());
            currentLocationTv.setText(response.getItem().getScannedLocationCode());
        } else {
            resultCodeTv.setText("Scan Successful");
            resultMessageTv.setText(response.getMessage());
        }
    }

    private void displayError(String message) {
        resultStatusChip.setText("ERROR");
        resultStatusChip.setBackgroundResource(R.drawable.bg_chip_danger);
        resultStatusChip.setTextColor(getResources().getColor(R.color.danger));
        resultCodeTv.setText("Failed");
        resultMessageTv.setText(message);
    }

    private void applyStatusStyle(TextView chip, String status) {
        String normalized = status.toUpperCase();
        if (normalized.contains("MISMATCH") || normalized.contains("FAIL")) {
            chip.setBackgroundResource(R.drawable.bg_chip_danger);
            chip.setTextColor(getResources().getColor(R.color.danger));
        } else if (normalized.contains("WARN") || normalized.contains("DUPLICATE")) {
            chip.setBackgroundResource(R.drawable.bg_chip_warning);
            chip.setTextColor(getResources().getColor(R.color.warning));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_success);
            chip.setTextColor(getResources().getColor(R.color.success));
        }
    }
}
