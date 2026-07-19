package com.pinetechs.orvix.ims.android.scan.presentation.asset;

import android.app.Dialog;
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
import com.pinetechs.orvix.ims.android.core.hardware.model.ScannerProfile;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.scan.data.dto.ScanResponse;
import com.pinetechs.orvix.ims.android.scan.presentation.common.ScanImageCoordinator;
import com.pinetechs.orvix.ims.android.core.presentation.BaseActivity;
import com.pinetechs.orvix.ims.android.scan.presentation.common.ScanResultDialog;

import com.pinetechs.orvix.ims.android.task.presentation.TaskListActivity;
import android.content.Intent;

import java.util.Objects;

public class AssetScanActivity extends BaseActivity {

    private static final String TAG = "AssetScanActivity";

    private AssetScanViewModel viewModel;
    private ScannerInterface scannerManager;
    private ScanImageCoordinator imageCoordinator;

    private TextInputEditText barcodeEditText;
    private Button submitButton;
    private Button correctButton;
    private Button retryButton;
    private TextView selectedPathTextView;
    private ProgressBar progressBar;
    private View capturedImageCard;
    private ImageView capturedBarcodeImageView;
    private TextView resultStatusChip;
    private TextView resultCodeTv;
    private TextView resultMessageTv;
    private TextView symbologyBadge;

    private Long taskId;
    private Long locationId;
    private Long floorId;
    private Long placeId;
    private String floorName;
    private String placeName;
    private String taskNumber;
    private String locationCode;
    private String locationName;
    private boolean scanImageRequired;
    private boolean showCapturedImage;
    private boolean apiLoading;
    private byte[] lastImage;
    private String lastSymbology = "UNKNOWN";
    private ScanResponse lastResponse;
    private Long lastSubmittedFloorId;
    private Long lastSubmittedPlaceId;
    private boolean resultOverlayVisible;
    private boolean correctionDialogVisible;
    private Dialog resultDialog;
    private Dialog correctionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_scan);

        initData();
        initViews();
        setupHeader();

        viewModel = new ViewModelProvider(this).get(AssetScanViewModel.class);
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
        long rawFloorId = getIntent().getLongExtra("floor_id", -1L);
        floorId = rawFloorId < 0 ? null : rawFloorId;
        long rawPlaceId = getIntent().getLongExtra("place_id", -1L);
        placeId = rawPlaceId < 0 ? null : rawPlaceId;
        floorName = getIntent().getStringExtra("floor_name");
        placeName = getIntent().getStringExtra("place_name");
        scanImageRequired = getIntent().getBooleanExtra("scan_image_required", false);
    }

    private void initViews() {
        barcodeEditText = findViewById(R.id.barcodeEditText);
        submitButton = findViewById(R.id.submitScanButton);
        correctButton = findViewById(R.id.correctScanButton);
        retryButton = findViewById(R.id.retryScanButton);
        selectedPathTextView = findViewById(R.id.selectedPathTextView);
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
        selectedPathTextView.setText("Floor: " + nonBlank(floorName, "---")
                + "  /  Place: " + nonBlank(placeName, "---"));

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
        TextView locationTv = findViewById(R.id.locationNameTextView);
        TextView taskRefTv = findViewById(R.id.taskRefTextView);

        locationTv.setText(locationName != null
                ? locationName
                : (locationCode != null ? locationCode : "Unknown Point"));
        taskRefTv.setText("Task: " + (taskNumber != null ? taskNumber : "---"));
    }

    private void initScanner() {
        refreshImageCaptureSettings();

        scannerManager = ScannerFactory.getScanner(this, ScannerProfile.ASSET);
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
                            Toast.makeText(AssetScanActivity.this, message, Toast.LENGTH_LONG).show();
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
                .getScannerProfileSettings(ScannerProfile.ASSET)
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
            Toast.makeText(this, "Please enter or scan an asset tag", Toast.LENGTH_SHORT).show();
            return;
        }
        submitScan(barcode, "MANUAL", null);
    }

    private void submitScan(String barcode, String symbology, byte[] scanImage) {
        if (apiLoading) return;
        String normalizedBarcode = barcode != null ? barcode.trim() : "";
        if (normalizedBarcode.isEmpty()) {
            if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
            Toast.makeText(this, "Please enter or scan an asset tag", Toast.LENGTH_SHORT).show();
            return;
        }
        if (locationId == null || floorId == null || placeId == null) {
            if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
            Toast.makeText(this, "Return and select a floor and place before scanning", Toast.LENGTH_LONG).show();
            return;
        }
        lastImage = scanImage;
        lastSymbology = symbology == null ? "UNKNOWN" : symbology;
        lastSubmittedFloorId = floorId;
        lastSubmittedPlaceId = placeId;
        viewModel.scanAsset(taskId, normalizedBarcode, locationId, floorId,
                placeId, lastSymbology, scanImage);
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
                updateBusyState();
                if (state.getData() == null) {
                    if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
                    displayError("Empty scan response");
                } else {
                    displayResult(state.getData());
                }
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
                
                
                if (state.getMessage().contains("is not open")) {
                    restToTaskPage();
                }
                
            }
        });
    }

    private void restToTaskPage() {
        Toast.makeText(this, R.string.err_task_not_open, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, TaskListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private boolean isBusy() {
        return isOperationBusy() || floorId == null || placeId == null;
    }

    private boolean isOperationBusy() {
        return apiLoading || resultOverlayVisible || correctionDialogVisible
                || (imageCoordinator != null && imageCoordinator.isWaitingForImage());
    }

    private void updateBusyState() {
        boolean busy = isBusy();
        progressBar.setVisibility(isOperationBusy() ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!busy);
        barcodeEditText.setEnabled(!busy);
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
        String result = response.getResultCode() == null ? getString(R.string.recorded_status) : response.getResultCode();
        resultStatusChip.setText(result.replace("_", " "));
        applyStatusStyle(resultStatusChip, result);
        String scanReference = response.isIdempotentReplay() 
                ? getString(R.string.synchronized_retry) 
                : getString(R.string.scan_id_label, response.getScanId());
        resultCodeTv.setText(ScanResultDialog.itemTitle(this, response.getItem()) + "  •  " + scanReference);
        String fields = response.getMismatchFields().isEmpty() ? "" : " (" + String.join(", ", response.getMismatchFields()) + ")";
        resultMessageTv.setText(compactResultMessage(response, messageFor(response.getMessageKey(), result) + fields));
        boolean canCorrectHere = response.isCorrectionAllowed()
                && response.getCurrentAcceptedScanId() != null;
        correctButton.setVisibility(canCorrectHere ? View.VISIBLE : View.GONE);
        retryButton.setVisibility(View.GONE);
        showResultOverlay(response, messageFor(response.getMessageKey(), result) + fields);
    }
    
    
    
    private void backToTaskActivety(){
        
        
    }
    

    private void showResultOverlay(ScanResponse response, String message) {
        resultOverlayVisible = true;
        updateBusyState();
        resultDialog = ScanResultDialog.show(this, response, message, new ScanResultDialog.Callback() {
            @Override public void onCorrect() { requestCorrectionReason(); }
            @Override public void onDismiss() {
                resultOverlayVisible = false;
                resultDialog = null;
                if (!correctionDialogVisible && imageCoordinator != null) imageCoordinator.onSubmissionFinished();
                updateBusyState();
                barcodeEditText.requestFocus();
            }
        });
    }

    private String compactResultMessage(ScanResponse response, String message) {
        String details = ScanResultDialog.itemDetails(this, response.getItem());
        String location = ScanResultDialog.locationDetails(this, response.getActualLocation());
        return message + (details.isEmpty() ? "" : "\n" + details)
                + (location.isEmpty() ? "" : "\n" + location);
    }

    private void displayError(String message) {
        resultStatusChip.setText(R.string.error_label);
        resultStatusChip.setBackgroundResource(R.drawable.bg_chip_danger);
        resultStatusChip.setTextColor(getResources().getColor(R.color.danger));
        resultCodeTv.setText(R.string.failed_label);
        resultMessageTv.setText(message != null ? message : getString(R.string.failed_label));
        correctButton.setVisibility(View.GONE);
    }

    private String messageFor(String key, String fallback) {
        if ("scan.matched".equals(key)) return getString(R.string.msg_scan_matched);
        if ("scan.location_mismatch".equals(key)) return getString(R.string.msg_scan_location_mismatch);
        if ("scan.duplicate".equals(key)) return getString(R.string.msg_scan_duplicate);
        if ("scan.location_conflict".equals(key)) return getString(R.string.msg_scan_location_conflict);
        if ("scan.recorded_for_review".equals(key)) return getString(R.string.msg_scan_recorded_for_review);
        if ("scan.correction_recorded".equals(key)) return getString(R.string.msg_scan_correction_recorded);
        return fallback.replace('_', ' ');
    }

    private void requestCorrectionReason() {
        if (lastResponse == null || lastResponse.getCurrentAcceptedScanId() == null
                || floorId == null || placeId == null) return;
        boolean correctingFirstAccepted = lastResponse.getCurrentAcceptedScanId().equals(lastResponse.getScanId());
        boolean pathUnchanged = Objects.equals(floorId, lastSubmittedFloorId)
                && Objects.equals(placeId, lastSubmittedPlaceId);
        if (correctingFirstAccepted && pathUnchanged) {
            Toast.makeText(this, R.string.err_change_value_for_correction, Toast.LENGTH_LONG).show();
            return;
        }
        EditText input = new EditText(this);
        input.setHint(R.string.hint_correction_reason);
        correctionDialogVisible = true;
        updateBusyState();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_correct_location)
                .setView(input)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.submit, null)
                .create();
        correctionDialog = dialog;
        dialog.setOnDismissListener(ignored -> finishCorrectionDialog());
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String reason = input.getText() == null ? "" : input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        input.setError(getString(R.string.err_reason_required));
                        return;
                    }
                    if (scanImageRequired && (lastImage == null || lastImage.length == 0)) {
                        Toast.makeText(this, R.string.err_rescan_for_image, Toast.LENGTH_LONG).show();
                        return;
                    }
                    dialog.setOnDismissListener(null);
                    correctionDialogVisible = false;
                    correctionDialog = null;
                    dialog.dismiss();
                    viewModel.correctAsset(taskId, lastResponse.getCurrentAcceptedScanId(), locationId,
                            floorId, placeId, reason, lastSymbology, lastImage);
                }));
        dialog.show();
    }

    private void finishCorrectionDialog() {
        correctionDialogVisible = false;
        correctionDialog = null;
        if (!apiLoading && imageCoordinator != null) imageCoordinator.onSubmissionFinished();
        updateBusyState();
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
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
        if (correctionDialog != null && correctionDialog.isShowing()) correctionDialog.dismiss();
        if (resultDialog != null && resultDialog.isShowing()) resultDialog.dismiss();
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
