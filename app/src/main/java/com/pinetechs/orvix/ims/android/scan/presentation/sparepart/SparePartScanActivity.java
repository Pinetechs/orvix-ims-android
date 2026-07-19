package com.pinetechs.orvix.ims.android.scan.presentation.sparepart;

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

import java.math.BigDecimal;
import java.util.Objects;

public class SparePartScanActivity extends BaseActivity {

    private static final String TAG = "SparePartScanActivity";

    private SparePartScanViewModel viewModel;
    private ScannerInterface scannerManager;
    private ScanImageCoordinator imageCoordinator;

    private TextInputEditText barcodeEditText;
    private Button submitButton;
    private Button correctButton;
    private Button retryButton;
    private TextView selectedLocationTextView;
    private ProgressBar progressBar;
    private View capturedImageCard;
    private ImageView capturedBarcodeImageView;
    private TextView resultStatusChip;
    private TextView resultCodeTv;
    private TextView resultMessageTv;
    private TextView symbologyBadge;

    private Long taskId;
    private Long branchId;
    private Long selectedLocationId;
    private String selectedLocationCode;
    private String selectedLocationName;
    private String taskNumber;
    private String branchCode;
    private String branchName;
    private boolean scanImageRequired;
    private boolean showCapturedImage;
    private boolean apiLoading;
    private byte[] lastImage;
    private String lastSymbology = "UNKNOWN";
    private ScanResponse lastResponse;
    private Long lastSubmittedLocationId;
    private BigDecimal lastSubmittedQuantity;
    private boolean quantityDialogVisible;
    private boolean resultOverlayVisible;
    private boolean correctionDialogVisible;
    private Dialog quantityDialog;
    private Dialog resultDialog;
    private Dialog correctionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sparepart_scan);

        initData();
        initViews();
        setupHeader();

        viewModel = new ViewModelProvider(this).get(SparePartScanViewModel.class);
        observeScanState();
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
        long rawSelectedLocationId = getIntent().getLongExtra("selected_location_id", -1L);
        selectedLocationId = rawSelectedLocationId < 0 ? null : rawSelectedLocationId;
        selectedLocationCode = getIntent().getStringExtra("selected_location_code");
        selectedLocationName = getIntent().getStringExtra("selected_location_name");
        scanImageRequired = getIntent().getBooleanExtra("scan_image_required", false);
    }

    private void initViews() {
        barcodeEditText = findViewById(R.id.barcodeEditText);
        submitButton = findViewById(R.id.submitScanButton);
        correctButton = findViewById(R.id.correctScanButton);
        retryButton = findViewById(R.id.retryScanButton);
        selectedLocationTextView = findViewById(R.id.selectedLocationTextView);
        progressBar = findViewById(R.id.progressBar);
        capturedImageCard = findViewById(R.id.capturedImageCard);
        capturedBarcodeImageView = findViewById(R.id.capturedBarcodeImageView);
        resultStatusChip = findViewById(R.id.resultStatusChip);
        resultCodeTv = findViewById(R.id.resultCodeTextView);
        resultMessageTv = findViewById(R.id.resultMessageTextView);
        symbologyBadge = findViewById(R.id.symbologyBadge);
        selectedLocationTextView.setText(nonBlank(selectedLocationName,
                nonBlank(selectedLocationCode, "No location selected")));

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        submitButton.setOnClickListener(v -> performManualScan());
        correctButton.setOnClickListener(v -> requestCorrectionReason());
        retryButton.setOnClickListener(v -> viewModel.retryLastScan());
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
                        runOnUiThread(() -> promptForQuantity(barcode, barcodeType, uploadImage));
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
                    R.string.err_image_required_hardware,
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String barcode = textOf(barcodeEditText);
        if (barcode.isEmpty()) {
            Toast.makeText(this, R.string.barcode_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        promptForQuantity(barcode, "MANUAL", null);
    }

    private void promptForQuantity(String barcode, String symbology, byte[] scanImage) {
        if (quantityDialogVisible || apiLoading) return;
        View content = getLayoutInflater().inflate(R.layout.dialog_spare_quantity, null);
        TextInputEditText quantityInput = content.findViewById(R.id.dialogQuantityEditText);
        TextView barcodeText = content.findViewById(R.id.dialogBarcodeTextView);
        barcodeText.setText(barcode);
        quantityInput.setText(lastSubmittedQuantity == null ? "1" : lastSubmittedQuantity.toPlainString());
        quantityInput.selectAll();

        quantityDialogVisible = true;
        updateBusyState();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_enter_quantity)
                .setView(content)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.apply, null)
                .create();
        quantityDialog = dialog;
        dialog.setOnDismissListener(ignored -> {
            quantityDialogVisible = false;
            quantityDialog = null;
            if (!apiLoading && imageCoordinator != null) imageCoordinator.onSubmissionFinished();
            updateBusyState();
        });
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            BigDecimal quantity = readQuantity(quantityInput);
            if (quantity == null) return;
            dialog.setOnDismissListener(null);
            quantityDialogVisible = false;
            quantityDialog = null;
            dialog.dismiss();
            submitScan(barcode, symbology, scanImage, quantity);
        }));
        dialog.show();
        quantityInput.requestFocus();
    }

    private void submitScan(String barcode, String symbology, byte[] scanImage, BigDecimal quantity) {
        if (apiLoading) return;
        String normalizedBarcode = barcode != null ? barcode.trim() : "";
        if (normalizedBarcode.isEmpty()) {
            if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
            Toast.makeText(this, R.string.barcode_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        if (branchId == null || selectedLocationId == null || quantity == null) {
            if (imageCoordinator != null) imageCoordinator.onSubmissionFinished();
            Toast.makeText(this, R.string.err_no_location_selected, Toast.LENGTH_LONG).show();
            return;
        }
        lastImage = scanImage;
        lastSymbology = symbology == null ? "UNKNOWN" : symbology;
        lastSubmittedLocationId = selectedLocationId;
        lastSubmittedQuantity = quantity;
        viewModel.scanSparePart(taskId, normalizedBarcode, branchId, selectedLocationId,
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

                if (state.getMessage() != null && state.getMessage().contains("is not open")) {
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
        return isOperationBusy() || selectedLocationId == null;
    }

    private boolean isOperationBusy() {
        return apiLoading || quantityDialogVisible || resultOverlayVisible || correctionDialogVisible
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
        resultMessageTv.setText(compactResultMessage(response, messageFor(response.getMessageKey(), result)));
        boolean canCorrectHere = response.isCorrectionAllowed()
                && response.getCurrentAcceptedScanId() != null;
        correctButton.setVisibility(canCorrectHere ? View.VISIBLE : View.GONE);
        retryButton.setVisibility(View.GONE);
        showResultOverlay(response, messageFor(response.getMessageKey(), result));
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

    private BigDecimal readQuantity(TextInputEditText input) {
        String value = textOf(input);
        if (value.isEmpty()) { Toast.makeText(this, R.string.err_qty_invalid, Toast.LENGTH_LONG).show(); return null; }
        try {
            BigDecimal quantity = new BigDecimal(value);
            if (quantity.signum() < 0 || quantity.scale() > 3 || quantity.precision() > 18) {
                Toast.makeText(this, R.string.err_qty_non_negative, Toast.LENGTH_LONG).show();
                return null;
            }
            return quantity;
        } catch (NumberFormatException error) {
            Toast.makeText(this, R.string.err_qty_invalid, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private String messageFor(String key, String fallback) {
        if ("scan.recorded".equals(key)) return getString(R.string.msg_scan_recorded);
        if ("scan.recorded_for_review".equals(key)) return getString(R.string.msg_scan_recorded_for_review);
        if ("scan.already_counted".equals(key)) return getString(R.string.msg_scan_already_counted);
        if ("scan.correction_recorded".equals(key)) return getString(R.string.msg_scan_correction_recorded_spare);
        return fallback.replace('_', ' ');
    }

    private void requestCorrectionReason() {
        if (lastResponse == null || lastResponse.getCurrentAcceptedScanId() == null || selectedLocationId == null) return;
        View content = getLayoutInflater().inflate(R.layout.dialog_spare_correction, null);
        TextInputEditText quantityInput = content.findViewById(R.id.correctionQuantityEditText);
        TextInputEditText reasonInput = content.findViewById(R.id.correctionReasonEditText);
        quantityInput.setText(lastSubmittedQuantity == null ? "1" : lastSubmittedQuantity.toPlainString());
        
        correctionDialogVisible = true;
        updateBusyState();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.correct_scan)
                .setView(content)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.submit, null)
                .create();
        correctionDialogVisible = true;
        correctionDialog = dialog;
        updateBusyState();
        dialog.setOnDismissListener(ignored -> finishCorrectionDialog());
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            BigDecimal quantity = readQuantity(quantityInput);
            if (quantity == null) return;
            String reason = textOf(reasonInput);
            if (reason.isEmpty()) {
                reasonInput.setError(getString(R.string.err_reason_required));
                return;
            }
            boolean correctingFirstAccepted = lastResponse.getCurrentAcceptedScanId().equals(lastResponse.getScanId());
            boolean valuesUnchanged = Objects.equals(selectedLocationId, lastSubmittedLocationId)
                    && lastSubmittedQuantity != null && lastSubmittedQuantity.compareTo(quantity) == 0;
            if (correctingFirstAccepted && valuesUnchanged) {
                quantityInput.setError(getString(R.string.err_change_value_for_correction));
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
            lastSubmittedLocationId = selectedLocationId;
            lastSubmittedQuantity = quantity;
            viewModel.correctSparePart(taskId, lastResponse.getCurrentAcceptedScanId(), branchId,
                    selectedLocationId, quantity, reason, lastSymbology, lastImage);
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
        if (quantityDialog != null && quantityDialog.isShowing()) quantityDialog.dismiss();
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
