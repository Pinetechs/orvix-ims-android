package com.pinetechs.orvix.ims.android.scan.presentation.vehicle;

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
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerFactory;
import com.pinetechs.orvix.ims.android.core.hardware.ScannerInterface;
import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;
import com.pinetechs.orvix.ims.android.scan.presentation.common.ScanImageCoordinator;

public class VehicleScanActivity extends AppCompatActivity {

    private static final String TAG = "VehicleScanActivity";

    private VehicleScanViewModel viewModel;
    private ScannerInterface scannerManager;
    private ScanImageCoordinator imageCoordinator;

    private TextInputEditText vinEditText;
    private Button submitButton;
    private Button correctButton;
    private Button retryButton;
    private ProgressBar progressBar;
    private View capturedImageCard;
    private ImageView capturedBarcodeImageView;

    private TextView resultStatusChip;
    private TextView resultCodeTv;
    private TextView resultMessageTv;
    private TextView symbologyBadge;

    private Long taskId;
    private Long locationId;
    private String taskNumber;
    private String locationCode;
    private String locationName;
    private boolean scanImageRequired;
    private boolean showCapturedImage;
    private boolean apiLoading;
    private byte[] lastImage;
    private String lastSymbology = "UNKNOWN";
    private ScanResponse lastResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_scan);

        initData();
        initViews();
        setupHeader();

        viewModel = new ViewModelProvider(this).get(VehicleScanViewModel.class);
        observeScanState();
        initScanner();
    }

    private void initData() {
        taskId = getIntent().getLongExtra("task_id", -1L);
        if (taskId == -1L) taskId = null;
        taskNumber = getIntent().getStringExtra("task_number");
        long rawLocationId = getIntent().getLongExtra("work_area_id", -1L);
        locationId = rawLocationId < 0 ? null : rawLocationId;
        locationCode = getIntent().getStringExtra("location_code");
        locationName = getIntent().getStringExtra("location_name");
        scanImageRequired = getIntent().getBooleanExtra("scan_image_required", false);
    }

    private void initViews() {
        vinEditText = findViewById(R.id.vinEditText);
        submitButton = findViewById(R.id.submitScanButton);
        correctButton = findViewById(R.id.correctScanButton);
        retryButton = findViewById(R.id.retryScanButton);
        progressBar = findViewById(R.id.progressBar);
        capturedImageCard = findViewById(R.id.capturedImageCard);
        capturedBarcodeImageView = findViewById(R.id.capturedBarcodeImageView);

        resultStatusChip = findViewById(R.id.resultStatusChip);
        resultCodeTv = findViewById(R.id.resultCodeTextView);
        resultMessageTv = findViewById(R.id.resultMessageTextView);
        symbologyBadge = findViewById(R.id.symbologyBadge);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        submitButton.setOnClickListener(v -> performManualScan());
        correctButton.setOnClickListener(v -> requestCorrectionReason());
        retryButton.setOnClickListener(v -> viewModel.retryLastScan());

        vinEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performManualScan();
                return true;
            }
            return false;
        });
    }

    private void setupHeader() {
        TextView locationTv = findViewById(R.id.locationNameTextView);
        TextView taskRefTv = findViewById(R.id.taskRefTextView);

        locationTv.setText(locationName != null
                ? locationName
                : (locationCode != null ? locationCode : "Unknown Location"));
        taskRefTv.setText("Task: " + (taskNumber != null ? taskNumber : "---"));
    }

    private void initScanner() {
        refreshImageCaptureSettings();

        scannerManager = ScannerFactory.getScanner(this, ScannerProfile.VEHICLE);
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
                            Toast.makeText(VehicleScanActivity.this, message, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );

        scannerManager.setOnScanListener((data, type) -> runOnUiThread(() -> {
            if (isBusy()) return;
            clearCapturedImage();
            vinEditText.setText(data);
            
            if (symbologyBadge != null) {
                BarcodeSymbology symbology = BarcodeSymbology.fromStorageName(type);
                String displayType = (symbology != null) ? symbology.getDisplayName() : type;
                symbologyBadge.setText(displayType);
                symbologyBadge.setVisibility(View.VISIBLE);
            }

            imageCoordinator.handleHardwareScan(data, type);
            updateBusyState();
        }));

    }

    private void refreshImageCaptureSettings() {
        showCapturedImage = new SessionManager(this)
                .getScannerProfileSettings(ScannerProfile.VEHICLE)
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

        String vin = textOf(vinEditText);
        if (vin.isEmpty()) {
            Toast.makeText(this, "Please enter or scan a VIN", Toast.LENGTH_SHORT).show();
            return;
        }
        submitScan(vin, "MANUAL", null);
    }

    private void submitScan(String vin, String symbology, byte[] scanImage) {
        if (apiLoading) return;
        String normalizedVin = vin != null ? vin.trim() : "";
        if (normalizedVin.isEmpty()) {
            if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
            Toast.makeText(this, "Please enter or scan a VIN", Toast.LENGTH_SHORT).show();
            return;
        }
        if (locationId == null) {
            if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
            displayError("Location ID is missing. Return to the work-area list and select the location again.");
            return;
        }
        lastImage = scanImage;
        lastSymbology = symbology == null ? "UNKNOWN" : symbology;
        viewModel.scanVehicle(taskId, normalizedVin, locationId, lastSymbology, scanImage);
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
                vinEditText.setText("");
                vinEditText.requestFocus();
            } else if (state.getStatus() == Resource.Status.ERROR) {
                apiLoading = false;
                if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
                updateBusyState();
                displayError(state.getMessage());
                retryButton.setVisibility(state.getMessage() != null && !state.getMessage().startsWith("[")
                        ? View.VISIBLE : View.GONE);
                vinEditText.requestFocus();
            }
        });
    }

    private boolean isBusy() {
        return apiLoading || (imageCoordinator != null && imageCoordinator.isWaitingForImage());
    }

    private void updateBusyState() {
        boolean busy = isBusy();
        progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!busy);
        vinEditText.setEnabled(!busy);
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
                && response.getCurrentAcceptedScanId() != null
                && !response.getCurrentAcceptedScanId().equals(response.getScanId());
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

    private String messageFor(String key, String fallback) {
        if ("scan.matched".equals(key)) return "Vehicle matched the selected location.";
        if ("scan.location_mismatch".equals(key)) return "The vehicle was recorded in a different location. Move to the correct location and scan again to correct it.";
        if ("scan.duplicate".equals(key)) return "This vehicle was already scanned in the same location.";
        if ("scan.location_conflict".equals(key)) return "This vehicle has an accepted scan in another location. You may correct it to the current location.";
        if ("scan.recorded_for_review".equals(key)) return "Vehicle is not in the task and was recorded for review.";
        if ("scan.correction_recorded".equals(key)) return "The vehicle location correction was recorded.";
        return fallback.replace('_', ' ');
    }

    private void requestCorrectionReason() {
        if (lastResponse == null || lastResponse.getCurrentAcceptedScanId() == null || locationId == null) return;
        EditText input = new EditText(this);
        input.setHint("Correction reason");
        new AlertDialog.Builder(this)
                .setTitle("Correct vehicle location")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String reason = input.getText() == null ? "" : input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Correction reason is required", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (scanImageRequired && (lastImage == null || lastImage.length == 0)) {
                        Toast.makeText(this, "Scan the barcode again to capture the correction image", Toast.LENGTH_LONG).show();
                        return;
                    }
                    viewModel.correctVehicle(taskId, lastResponse.getCurrentAcceptedScanId(), locationId,
                            reason, lastSymbology, lastImage);
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
