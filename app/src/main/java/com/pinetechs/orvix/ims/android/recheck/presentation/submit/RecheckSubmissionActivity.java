package com.pinetechs.orvix.ims.android.recheck.presentation.submit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.hardware.model.BarcodeSymbology;
import com.pinetechs.orvix.ims.android.core.presentation.BaseActivity;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckIssueResponse;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckItemResponse;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckRequestResponse;
import com.pinetechs.orvix.ims.android.recheck.presentation.RecheckUiText;
import com.pinetechs.orvix.ims.android.scan.presentation.common.ScanImageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecheckSubmissionActivity extends BaseActivity {

    public static final String EXTRA_REQUEST_ID = "recheck_request_id";
    public static final String EXTRA_ITEM_ID = "recheck_item_id";

    private enum OutcomeMode {
        FOUND,
        NOT_FOUND,
        UNABLE_TO_VERIFY
    }

    private RecheckSubmissionViewModel viewModel;
    private Long requestId;
    private Long itemId;
    private RecheckRequestResponse request;
    private RecheckItemResponse item;
    private OutcomeMode outcomeMode = OutcomeMode.FOUND;

    private RecheckScannerSession scannerSession;
    private boolean resumed;
    private boolean submitting;

    private RecheckLocationForm locationForm;
    private byte[] evidenceImage;
    private String imageSource = "UNKNOWN";
    private String scannedSymbology = "MANUAL";
    private RecheckCameraFile pendingCameraFile;

    private TextView requestNumberTextView;
    private TextView itemCodeTextView;
    private TextView itemDescriptionTextView;
    private TextView issueTextView;
    private TextView expectedLocationTextView;
    private TextView previousResultTextView;
    private TextView expectedQuantityTextView;
    private TextView imageRuleTextView;
    private TextView foundModeButton;
    private TextView notFoundModeButton;
    private TextView unableModeButton;
    private View observedSection;
    private View unavailableSection;
    private AutoCompleteTextView reasonDropdown;
    private TextInputEditText barcodeEditText;
    private TextInputEditText quantityEditText;
    private TextInputEditText noteEditText;
    private TextView symbologyBadge;
    private View capturedImageCard;
    private ImageView capturedImageView;
    private Button captureImageButton;
    private Button submitButton;
    private Button retryButton;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private ReasonOption selectedReason;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK) {
                            clearPendingCameraFile();
                            return;
                        }
                        byte[] image = pendingCameraFile == null
                                ? null
                                : pendingCameraFile.readAndDelete();
                        pendingCameraFile = null;
                        if (image == null || image.length == 0) {
                            showError(getString(R.string.recheck_camera_image_error));
                            return;
                        }
                        setCameraEvidence(image);
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recheck_submission);

        readIds();
        bindViews();

        viewModel = new ViewModelProvider(this)
                .get(RecheckSubmissionViewModel.class);
        locationForm = new RecheckLocationForm(this, this, viewModel);
        setupInteractions();
        observeState();
        viewModel.load(requestId, itemId);
    }

    private void readIds() {
        requestId = longExtra(EXTRA_REQUEST_ID);
        itemId = longExtra(EXTRA_ITEM_ID);
    }

    private Long longExtra(String key) {
        long value = getIntent().getLongExtra(key, -1L);
        return value < 0 ? null : value;
    }

    private void bindViews() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        requestNumberTextView = findViewById(R.id.requestNumberTextView);
        itemCodeTextView = findViewById(R.id.itemCodeTextView);
        itemDescriptionTextView = findViewById(R.id.itemDescriptionTextView);
        issueTextView = findViewById(R.id.issueTextView);
        expectedLocationTextView = findViewById(R.id.expectedLocationTextView);
        previousResultTextView = findViewById(R.id.previousResultTextView);
        expectedQuantityTextView = findViewById(R.id.expectedQuantityTextView);
        imageRuleTextView = findViewById(R.id.imageRuleTextView);
        foundModeButton = findViewById(R.id.foundModeButton);
        notFoundModeButton = findViewById(R.id.notFoundModeButton);
        unableModeButton = findViewById(R.id.unableModeButton);
        observedSection = findViewById(R.id.observedSection);
        unavailableSection = findViewById(R.id.unavailableSection);
        reasonDropdown = findViewById(R.id.reasonDropdown);
        barcodeEditText = findViewById(R.id.barcodeEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        noteEditText = findViewById(R.id.noteEditText);
        symbologyBadge = findViewById(R.id.symbologyBadge);
        capturedImageCard = findViewById(R.id.capturedImageCard);
        capturedImageView = findViewById(R.id.capturedImageView);
        captureImageButton = findViewById(R.id.captureImageButton);
        submitButton = findViewById(R.id.submitButton);
        retryButton = findViewById(R.id.retryButton);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
    }

    private void setupInteractions() {
        foundModeButton.setOnClickListener(v -> setOutcomeMode(OutcomeMode.FOUND));
        notFoundModeButton.setOnClickListener(
                v -> setOutcomeMode(OutcomeMode.NOT_FOUND));
        unableModeButton.setOnClickListener(
                v -> setOutcomeMode(OutcomeMode.UNABLE_TO_VERIFY));
        captureImageButton.setOnClickListener(v -> openCamera());
        submitButton.setOnClickListener(v -> submit());
        retryButton.setOnClickListener(v -> viewModel.retry());
        setupReasonDropdown();
    }

    private void setupReasonDropdown() {
        List<ReasonOption> reasons = new ArrayList<>();
        reasons.add(new ReasonOption(
                "ITEM_MISSING", getString(R.string.recheck_reason_missing)));
        reasons.add(new ReasonOption(
                "ITEM_MOVED", getString(R.string.recheck_reason_moved)));
        reasons.add(new ReasonOption(
                "BARCODE_DAMAGED", getString(R.string.recheck_reason_barcode_damaged)));
        reasons.add(new ReasonOption(
                "ACCESS_RESTRICTED", getString(R.string.recheck_reason_access_restricted)));
        reasons.add(new ReasonOption(
                "OTHER", getString(R.string.recheck_reason_other)));

        ArrayAdapter<ReasonOption> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                reasons
        );
        reasonDropdown.setAdapter(adapter);
        reasonDropdown.setOnItemClickListener((parent, view, position, id) ->
                selectedReason = (ReasonOption) parent.getItemAtPosition(position));
    }

    private void observeState() {
        viewModel.getRequestState().observe(this, state -> {
            if (state == null) return;
            if (state.getStatus() == Resource.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
            } else if (state.getStatus() == Resource.Status.ERROR) {
                progressBar.setVisibility(View.GONE);
                showError(state.getMessage());
            } else if (state.getStatus() == Resource.Status.SUCCESS
                    && state.getData() != null) {
                progressBar.setVisibility(View.GONE);
                render(state.getData());
            }
        });

        viewModel.getSubmitState().observe(this, state -> {
            if (state == null) return;
            submitting = state.getStatus() == Resource.Status.LOADING;
            updateBusyState();

            if (state.getStatus() == Resource.Status.ERROR) {
                showError(state.getMessage());
                retryButton.setVisibility(
                        viewModel.canRetry() ? View.VISIBLE : View.GONE);
            } else if (state.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(
                        this,
                        R.string.recheck_item_submitted,
                        Toast.LENGTH_LONG
                ).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    private void render(RecheckRequestResponse request) {
        this.request = request;
        this.item = findItem(request);
        if (item == null) {
            showError(getString(R.string.recheck_item_not_found));
            submitButton.setEnabled(false);
            return;
        }
        if (!request.canWork() || !item.isPending()) {
            showError(getString(R.string.recheck_item_not_open));
            submitButton.setEnabled(false);
            return;
        }

        requestNumberTextView.setText(
                RecheckUiText.valueOrDash(request.getRequestNumber()));
        itemCodeTextView.setText(RecheckUiText.valueOrDash(item.getItemCode()));
        itemDescriptionTextView.setText(
                RecheckUiText.valueOrDash(item.getItemDescription()));
        itemDescriptionTextView.setVisibility(
                item.getItemDescription() == null ? View.GONE : View.VISIBLE);
        issueTextView.setText(issueLabels(item.getIssues()));
        expectedLocationTextView.setText(getString(
                R.string.recheck_expected_location_value,
                RecheckUiText.valueOrDash(item.getExpectedLocation())
        ));
        previousResultTextView.setText(getString(
                R.string.recheck_previous_result_value,
                RecheckUiText.valueOrDash(item.getPreviousResult())
        ));
        if (item.getExpectedQuantity() != null) {
            expectedQuantityTextView.setText(getString(
                    R.string.recheck_expected_quantity_value,
                    item.getExpectedQuantity().stripTrailingZeros().toPlainString()
            ));
            expectedQuantityTextView.setVisibility(View.VISIBLE);
        } else {
            expectedQuantityTextView.setVisibility(View.GONE);
        }
        imageRuleTextView.setText(request.isImageRequired()
                ? R.string.recheck_image_required
                : R.string.recheck_image_optional);

        locationForm.bind(request.getInventoryDomain(), item);

        if (item.getReferenceItemId() == null || item.getItemCode() == null) {
            foundModeButton.setEnabled(false);
            foundModeButton.setAlpha(0.45f);
            setOutcomeMode(OutcomeMode.UNABLE_TO_VERIFY);
        } else {
            setOutcomeMode(OutcomeMode.FOUND);
        }
        ensureScanner();
        updateBusyState();
    }

    private RecheckItemResponse findItem(RecheckRequestResponse value) {
        if (value == null || itemId == null) return null;
        for (RecheckItemResponse candidate : value.getItems()) {
            if (candidate != null && itemId.equals(candidate.getId())) {
                return candidate;
            }
        }
        return null;
    }

    private void setOutcomeMode(OutcomeMode mode) {
        outcomeMode = mode;
        boolean found = mode == OutcomeMode.FOUND;
        observedSection.setVisibility(found ? View.VISIBLE : View.GONE);
        unavailableSection.setVisibility(found ? View.GONE : View.VISIBLE);

        foundModeButton.setBackgroundResource(found
                ? R.drawable.bg_chip_success
                : R.drawable.bg_card_soft);
        notFoundModeButton.setBackgroundResource(mode == OutcomeMode.NOT_FOUND
                ? R.drawable.bg_chip_danger
                : R.drawable.bg_card_soft);
        unableModeButton.setBackgroundResource(mode == OutcomeMode.UNABLE_TO_VERIFY
                ? R.drawable.bg_chip_warning
                : R.drawable.bg_card_soft);

        if (!found && scannerSession != null) {
            scannerSession.cancelPending();
        }
        clearError();
        updateBusyState();
    }

    private void submit() {
        if (request == null || item == null || submitting) return;
        clearError();

        if (request.isImageRequired()
                && (evidenceImage == null || evidenceImage.length == 0)) {
            showError(getString(R.string.recheck_evidence_required_error));
            return;
        }

        RecheckSubmissionBuilder.BuildResult result =
                outcomeMode == OutcomeMode.FOUND
                        ? RecheckSubmissionBuilder.observed(
                                this,
                                request.getInventoryDomain(),
                                item,
                                textOf(barcodeEditText),
                                scannedSymbology,
                                imageSource,
                                locationForm.getSelectedFloorId(),
                                locationForm.getSelectedPlaceId(),
                                locationForm.getSelectedSpareLocationId(),
                                textOf(quantityEditText),
                                textOf(noteEditText)
                        )
                        : RecheckSubmissionBuilder.unavailable(
                                this,
                                outcomeMode == OutcomeMode.NOT_FOUND
                                        ? "NOT_FOUND"
                                        : "UNABLE_TO_VERIFY",
                                selectedReason == null ? null : selectedReason.code,
                                textOf(noteEditText),
                                imageSource
                        );
        if (!result.isSuccess()) {
            showError(result.getError());
            return;
        }
        retryButton.setVisibility(View.GONE);
        viewModel.submit(result.getRequest(), evidenceImage);
    }

    private void ensureScanner() {
        if (scannerSession != null || request == null) return;
        scannerSession = new RecheckScannerSession(
                this,
                request.getInventoryDomain(),
                request.isImageRequired(),
                new RecheckScannerSession.Callback() {
                    @Override
                    public String scanBlockReason() {
                        if (outcomeMode != OutcomeMode.FOUND) {
                            return getString(
                                    R.string.recheck_select_found_before_scan);
                        }
                        if ("SPARE_PART".equals(normalizedDomain())
                                && textOf(quantityEditText).isEmpty()) {
                            return getString(
                                    R.string.recheck_enter_quantity_before_scan);
                        }
                        return submitting ? "" : null;
                    }

                    @Override
                    public void onScanBlocked(String message) {
                        if (message != null && !message.isEmpty()) {
                            Toast.makeText(
                                    RecheckSubmissionActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onCaptureStarted() {
                        updateBusyState();
                    }

                    @Override
                    public void onBarcodeReady(String barcode, String barcodeType) {
                        barcodeEditText.setText(barcode);
                        scannedSymbology = valueOrUnknown(barcodeType);
                        updateSymbologyBadge(scannedSymbology);
                        updateBusyState();
                    }

                    @Override
                    public void onEvidenceReady(byte[] image, String source) {
                        setEvidence(image, source);
                    }

                    @Override
                    public void onCaptureError(String message) {
                        showError(message);
                        updateBusyState();
                    }
                }
        );
        if (resumed) {
            scannerSession.start();
        }
    }

    private void updateSymbologyBadge(String value) {
        BarcodeSymbology symbology = BarcodeSymbology.fromStorageName(value);
        symbologyBadge.setText(symbology == null
                ? valueOrUnknown(value)
                : symbology.getDisplayName());
        symbologyBadge.setVisibility(View.VISIBLE);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            showError(getString(R.string.recheck_camera_unavailable));
            return;
        }
        try {
            clearPendingCameraFile();
            pendingCameraFile = RecheckCameraFile.create(this);
            pendingCameraFile.attachTo(intent);
            cameraLauncher.launch(intent);
        } catch (IOException exception) {
            clearPendingCameraFile();
            showError(getString(R.string.recheck_camera_image_error));
        }
    }

    private void setCameraEvidence(byte[] source) {
        byte[] normalized = ScanImageUtils.toUploadJpeg(source);
        if (normalized == null) {
            showError(getString(R.string.recheck_camera_image_error));
            return;
        }
        setEvidence(normalized, "DEVICE_CAMERA");
    }

    private void clearPendingCameraFile() {
        if (pendingCameraFile != null) pendingCameraFile.delete();
        pendingCameraFile = null;
    }

    private void setEvidence(byte[] image, String source) {
        evidenceImage = image;
        imageSource = source;
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        if (bitmap != null) {
            capturedImageView.setImageBitmap(bitmap);
            capturedImageCard.setVisibility(View.VISIBLE);
        }
    }

    private String issueLabels(List<RecheckIssueResponse> issues) {
        if (issues == null || issues.isEmpty()) {
            return getString(R.string.recheck_issue);
        }
        StringBuilder value = new StringBuilder();
        for (RecheckIssueResponse issue : issues) {
            if (issue == null) continue;
            if (value.length() > 0) value.append(" • ");
            value.append(RecheckUiText.issue(this, issue.getIssueType()));
        }
        return value.length() == 0
                ? getString(R.string.recheck_issue)
                : value.toString();
    }

    private void updateBusyState() {
        boolean waitingForImage = scannerSession != null
                && scannerSession.isWaitingForImage();
        boolean busy = submitting || waitingForImage;
        progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!busy && request != null && item != null);
        captureImageButton.setEnabled(!busy);
        foundModeButton.setEnabled(
                !busy && item != null && item.getReferenceItemId() != null);
        notFoundModeButton.setEnabled(!busy);
        unableModeButton.setEnabled(!busy);
    }

    private String normalizedDomain() {
        return request == null || request.getInventoryDomain() == null
                ? ""
                : request.getInventoryDomain().trim().toUpperCase(Locale.ROOT);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private String valueOrUnknown(String value) {
        return value == null || value.trim().isEmpty()
                ? "UNKNOWN"
                : value.trim();
    }

    private void showError(String message) {
        errorTextView.setText(message == null
                ? getString(R.string.recheck_submission_error)
                : message);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        errorTextView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        if (scannerSession != null) scannerSession.start();
    }

    @Override
    protected void onPause() {
        resumed = false;
        if (scannerSession != null) scannerSession.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearPendingCameraFile();
        if (scannerSession != null) scannerSession.close();
        super.onDestroy();
    }

    private static final class ReasonOption {
        private final String code;
        private final String label;

        private ReasonOption(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
