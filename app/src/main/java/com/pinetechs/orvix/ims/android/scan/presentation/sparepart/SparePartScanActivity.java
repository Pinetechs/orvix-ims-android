package com.pinetechs.orvix.ims.android.scan.presentation.sparepart;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerFactory;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerInterface;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;
import com.pinetechs.orvix.ims.android.scan.presentation.common.ScanImageCoordinator;
import com.pinetechs.orvix.ims.android.workarea.data.dto.HierarchyOptionResponse;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SparePartScanActivity extends AppCompatActivity {

    private static final String TAG = "SparePartScanActivity";

    private SparePartScanViewModel viewModel;
    private ScannerInterface scannerManager;
    private ScanImageCoordinator imageCoordinator;

    private TextInputEditText barcodeEditText;
    private Button submitButton;
    private Button correctButton;
    private Button retryButton;
    private Spinner locationSpinner;
    private TextInputEditText quantityEditText;
    private ProgressBar progressBar;
    private View capturedImageCard;
    private ImageView capturedBarcodeImageView;
    private TextView resultStatusChip;
    private TextView resultCodeTv;
    private TextView resultMessageTv;
    private TextView symbologyBadge;

    private Long taskId;
    private Long branchId;
    private HierarchyOptionResponse selectedLocation;
    private String taskNumber;
    private String branchCode;
    private String branchName;
    private boolean scanImageRequired;
    private boolean showCapturedImage;
    private boolean apiLoading;
    private boolean hierarchyLoading;
    private byte[] lastImage;
    private String lastSymbology = "UNKNOWN";
    private ScanResponse lastResponse;
    private Long lastSubmittedLocationId;
    private BigDecimal lastSubmittedQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sparepart_scan);

        initData();
        initViews();
        setupHeader();

        viewModel = new ViewModelProvider(this).get(SparePartScanViewModel.class);
        observeScanState();
        observeLocations();
        if (taskId != null && branchId != null) viewModel.loadLocations(taskId, branchId);
        initScanner();
    }

    private void initData() {
        taskId = getIntent().getLongExtra("task_id", -1L);
        if (taskId == -1L) taskId = null;
        taskNumber = getIntent().getStringExtra("task_number");
        long rawBranchId = getIntent().getLongExtra("work_area_id", -1L);
        branchId = rawBranchId < 0 ? null : rawBranchId;
        branchCode = getIntent().getStringExtra("location_code");
        branchName = getIntent().getStringExtra("location_name");
        scanImageRequired = getIntent().getBooleanExtra("scan_image_required", false);
    }

    private void initViews() {
        barcodeEditText = findViewById(R.id.barcodeEditText);
        submitButton = findViewById(R.id.submitScanButton);
        correctButton = findViewById(R.id.correctScanButton);
        retryButton = findViewById(R.id.retryScanButton);
        locationSpinner = findViewById(R.id.locationSpinner);
        quantityEditText = findViewById(R.id.quantityEditText);
        progressBar = findViewById(R.id.progressBar);
        capturedImageCard = findViewById(R.id.capturedImageCard);
        capturedBarcodeImageView = findViewById(R.id.capturedBarcodeImageView);
        resultStatusChip = findViewById(R.id.resultStatusChip);
        resultCodeTv = findViewById(R.id.resultCodeTextView);
        resultMessageTv = findViewById(R.id.resultMessageTextView);
        symbologyBadge = findViewById(R.id.symbologyBadge);
        quantityEditText.setText("0");

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        submitButton.setOnClickListener(v -> performManualScan());
        correctButton.setOnClickListener(v -> requestCorrectionReason());
        retryButton.setOnClickListener(v -> viewModel.retryLastScan());
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                selectedLocation = item instanceof HierarchyOptionResponse ? (HierarchyOptionResponse) item : null;
                updateBusyState();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { selectedLocation = null; }
        });
        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateBusyState(); }
            @Override public void afterTextChanged(Editable s) { }
        });

        barcodeEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performManualScan();
                return true;
            }
            return false;
        });
    }

    private void setupHeader() {
        TextView branchTv = findViewById(R.id.branchNameTextView);
        TextView taskRefTv = findViewById(R.id.taskRefTextView);

        branchTv.setText(branchName != null
                ? branchName
                : (branchCode != null ? branchCode : "Unknown Branch"));
        taskRefTv.setText("Task: " + (taskNumber != null ? taskNumber : "---"));
    }

    private void initScanner() {
        refreshImageCaptureSettings();

        scannerManager = ScannerFactory.getScanner(this, ScannerProfile.SPARE_PART);
        imageCoordinator = new ScanImageCoordinator(
                scannerManager,
                scanImageRequired,
                showCapturedImage,
                new ScanImageCoordinator.Callback() {
                    @Override
                    public void onCapturedImage(byte[] jpegImage) {
                        runOnUiThread(() -> displayCapturedImage(jpegImage));
                    }

                    @Override
                    public void onScanReady(String barcode, String barcodeType, byte[] uploadImage) {
                        runOnUiThread(() -> submitScan(barcode, barcodeType, uploadImage));
                    }

                    @Override
                    public void onCaptureFailed(String message) {
                        runOnUiThread(() -> {
                            updateBusyState();
                            displayError(message);
                            Toast.makeText(SparePartScanActivity.this, message, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );

        scannerManager.setOnScanListener((data, type) -> runOnUiThread(() -> {
            if (isBusy()) return;
            clearCapturedImage();
            barcodeEditText.setText(data);

            if (symbologyBadge != null) {
                symbologyBadge.setText(type);
                symbologyBadge.setVisibility(View.VISIBLE);
            }

            imageCoordinator.handleHardwareScan(data, type);
            updateBusyState();
        }));

    }

    private void refreshImageCaptureSettings() {
        showCapturedImage = new SessionManager(this)
                .getScannerProfileSettings(ScannerProfile.SPARE_PART)
                .isShowCapturedImage();

        if (imageCoordinator != null) {
            imageCoordinator.updateOptions(scanImageRequired, showCapturedImage);
        }

        if (!showCapturedImage && capturedImageCard != null) {
            capturedBarcodeImageView.setImageDrawable(null);
            capturedImageCard.setVisibility(View.GONE);
        }

        Log.d(TAG, "Capture image settings: required=" + scanImageRequired
                + ", preview=" + showCapturedImage);
    }

    private void performManualScan() {
        if (scanImageRequired) {
            Toast.makeText(
                    this,
                    "This task requires a scanner image. Use the physical scan button.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String barcode = textOf(barcodeEditText);
        if (barcode.isEmpty()) {
            Toast.makeText(this, "Please enter or scan a barcode", Toast.LENGTH_SHORT).show();
            return;
        }
        submitScan(barcode, "MANUAL", null);
    }

    private void submitScan(String barcode, String symbology, byte[] scanImage) {
        if (apiLoading) return;
        String normalizedBarcode = barcode != null ? barcode.trim() : "";
        if (normalizedBarcode.isEmpty()) {
            if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
            Toast.makeText(this, "Please enter or scan a barcode", Toast.LENGTH_SHORT).show();
            return;
        }
        BigDecimal quantity = readQuantity();
        if (branchId == null || selectedLocation == null || quantity == null) {
            if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
            return;
        }
        lastImage = scanImage;
        lastSymbology = symbology == null ? "UNKNOWN" : symbology;
        lastSubmittedLocationId = selectedLocation.getId();
        lastSubmittedQuantity = quantity;
        viewModel.scanSparePart(taskId, normalizedBarcode, branchId, selectedLocation.getId(),
                quantity, lastSymbology, scanImage);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void observeScanState() {
        viewModel.getScanState().observe(this, state -> {
            if (state == null) return;

            if (state.getStatus() == Resource.Status.LOADING) {
                apiLoading = true;
                retryButton.setVisibility(View.GONE);
                updateBusyState();
            } else if (state.getStatus() == Resource.Status.SUCCESS) {
                apiLoading = false;
                if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
                updateBusyState();
                displayResult(state.getData());
                barcodeEditText.setText("");
                barcodeEditText.requestFocus();
            } else if (state.getStatus() == Resource.Status.ERROR) {
                apiLoading = false;
                if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
                updateBusyState();
                displayError(state.getMessage());
                retryButton.setVisibility(state.getMessage() != null && !state.getMessage().startsWith("[")
                        ? View.VISIBLE : View.GONE);
                barcodeEditText.requestFocus();
            }
        });
    }

    private boolean isBusy() {

        System.err.println("*****************************");

        System.err.println("apiLoading ="+apiLoading);
        System.err.println("hierarchyLoading ="+hierarchyLoading);
        System.err.println("selectedLocation ="+selectedLocation == null);
        System.err.println("quantityEditText ="+textOf(quantityEditText).isEmpty());

        System.err.println("imageCoordinator ="+(imageCoordinator != null && imageCoordinator.isWaitingForImage()));

        System.err.println("*****************************");


        return apiLoading || hierarchyLoading || selectedLocation == null || textOf(quantityEditText).isEmpty()
                || (imageCoordinator != null && imageCoordinator.isWaitingForImage());
    }

    private void updateBusyState() {
        boolean busy = isBusy();
        System.err.println("isBussy" +busy );
        progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!busy);
        barcodeEditText.setEnabled(!busy);
        quantityEditText.setEnabled(!apiLoading && !hierarchyLoading);
        locationSpinner.setEnabled(!apiLoading && !hierarchyLoading && locationSpinner.getCount() > 0);
    }

    private void displayCapturedImage(byte[] imageBytes) {
        if (!showCapturedImage || imageBytes == null || imageBytes.length == 0) return;
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        if (bitmap == null) return;
        capturedBarcodeImageView.setImageBitmap(bitmap);
        capturedImageCard.setVisibility(View.VISIBLE);
    }

    private void clearCapturedImage() {
        capturedBarcodeImageView.setImageDrawable(null);
        capturedImageCard.setVisibility(View.GONE);
    }

    private void displayResult(ScanResponse response) {
        if (response == null) return;
        lastResponse = response;
        String result = response.getResultCode() == null ? "RECORDED" : response.getResultCode();
        resultStatusChip.setText(result.replace("_", " "));
        applyStatusStyle(resultStatusChip, result);
        resultCodeTv.setText(response.isIdempotentReplay() ? "Synchronized retry" : "Scan #" + response.getScanId());
        resultMessageTv.setText(messageFor(response.getMessageKey(), result));
        boolean canCorrectHere = response.isCorrectionAllowed()
                && response.getCurrentAcceptedScanId() != null;
        correctButton.setVisibility(canCorrectHere ? View.VISIBLE : View.GONE);
        retryButton.setVisibility(View.GONE);
    }

    private void displayError(String message) {
        resultStatusChip.setText("ERROR");
        resultStatusChip.setBackgroundResource(R.drawable.bg_chip_danger);
        resultStatusChip.setTextColor(getResources().getColor(R.color.danger));
        resultCodeTv.setText("Failed");
        resultMessageTv.setText(message != null ? message : "Scan failed");
        correctButton.setVisibility(View.GONE);
    }

    private void observeLocations() {
        viewModel.getLocationsState().observe(this, state -> {


            if (state == null) return;
            hierarchyLoading = state.getStatus() == Resource.Status.LOADING;
            if (state.getStatus() == Resource.Status.SUCCESS) bindLocations(state.getData());
            else if (state.getStatus() == Resource.Status.ERROR) displayError(state.getMessage());
            updateBusyState();
        });
    }

    private void bindLocations(List<HierarchyOptionResponse> values) {
        List<HierarchyOptionResponse> safe = values == null ? Collections.emptyList() : values;
        ArrayAdapter<HierarchyOptionResponse> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, safe);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
        locationSpinner.setEnabled(!safe.isEmpty());
    }

    private BigDecimal readQuantity() {
        String value = textOf(quantityEditText);
        if (value.isEmpty()) { Toast.makeText(this, "Counted quantity is required", Toast.LENGTH_LONG).show(); return null; }
        try {
            BigDecimal quantity = new BigDecimal(value);
            if (quantity.signum() < 0 || quantity.scale() > 3 || quantity.precision() > 18) {
                Toast.makeText(this, "Quantity must be non-negative with at most 3 decimal places", Toast.LENGTH_LONG).show();
                return null;
            }
            return quantity;
        } catch (NumberFormatException error) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private String messageFor(String key, String fallback) {
        if ("scan.recorded".equals(key)) return "The counted quantity was recorded.";
        if ("scan.recorded_for_review".equals(key)) return "The item was recorded for supervisor review.";
        if ("scan.already_counted".equals(key)) return "This item was already counted. You may submit a correction.";
        if ("scan.correction_recorded".equals(key)) return "The count correction was recorded.";
        return fallback.replace('_', ' ');
    }

    private void requestCorrectionReason() {
        if (lastResponse == null || lastResponse.getCurrentAcceptedScanId() == null || selectedLocation == null) return;
        BigDecimal quantity = readQuantity();
        if (quantity == null) return;
        boolean correctingFirstAccepted = lastResponse.getCurrentAcceptedScanId().equals(lastResponse.getScanId());
        boolean valuesUnchanged = Objects.equals(selectedLocation.getId(), lastSubmittedLocationId)
                && lastSubmittedQuantity != null && lastSubmittedQuantity.compareTo(quantity) == 0;
        if (correctingFirstAccepted && valuesUnchanged) {
            Toast.makeText(this, "Change the location or counted quantity before submitting a correction", Toast.LENGTH_LONG).show();
            return;
        }
        EditText input = new EditText(this);
        input.setHint("Correction reason");
        new AlertDialog.Builder(this).setTitle("Correct spare-part count").setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String reason = input.getText() == null ? "" : input.getText().toString().trim();
                    if (reason.isEmpty()) { Toast.makeText(this, "Correction reason is required", Toast.LENGTH_LONG).show(); return; }
                    if (scanImageRequired && (lastImage == null || lastImage.length == 0)) {
                        Toast.makeText(this, "Scan the barcode again to capture the correction image", Toast.LENGTH_LONG).show(); return;
                    }
                    viewModel.correctSparePart(taskId, lastResponse.getCurrentAcceptedScanId(), branchId,
                            selectedLocation.getId(), quantity, reason, lastSymbology, lastImage);
                }).show();
    }

    private void applyStatusStyle(TextView chip, String status) {
        String normalized = status.toUpperCase();
        if (normalized.contains("MISMATCH") || normalized.contains("FAIL")) {
            chip.setBackgroundResource(R.drawable.bg_chip_danger);
            chip.setTextColor(getResources().getColor(R.color.danger));
        } else if (normalized.contains("WARN") || normalized.contains("DUPLICATE")
                || normalized.contains("CONFLICT") || normalized.contains("EXTRA")
                || normalized.contains("NOT_IN_TASK") || normalized.contains("REVIEW")
                || normalized.contains("ALREADY")) {
            chip.setBackgroundResource(R.drawable.bg_chip_warning);
            chip.setTextColor(getResources().getColor(R.color.warning));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_success);
            chip.setTextColor(getResources().getColor(R.color.success));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshImageCaptureSettings();
        if (scannerManager != null) scannerManager.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (imageCoordinator != null) imageCoordinator.cancelPending();
        if (scannerManager != null) scannerManager.unregister(this);
        updateBusyState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageCoordinator != null) imageCoordinator.cancelPending();
        if (scannerManager != null) scannerManager.close();
    }
}
