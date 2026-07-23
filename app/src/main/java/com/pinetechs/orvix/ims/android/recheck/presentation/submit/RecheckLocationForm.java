package com.pinetechs.orvix.ims.android.recheck.presentation.submit;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.core.util.Resource;
import com.pinetechs.orvix.ims.android.recheck.data.dto.RecheckItemResponse;
import com.pinetechs.orvix.ims.android.workarea.data.dto.HierarchyOptionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Owns the domain-specific floor/place/location controls.
 */
public final class RecheckLocationForm {

    private final Activity activity;
    private final RecheckSubmissionViewModel viewModel;
    private final View floorLayout;
    private final View placeLayout;
    private final View spareLocationLayout;
    private final View quantityLayout;
    private final AutoCompleteTextView floorDropdown;
    private final AutoCompleteTextView placeDropdown;
    private final AutoCompleteTextView spareLocationDropdown;

    private RecheckItemResponse item;
    private Long selectedFloorId;
    private Long selectedPlaceId;
    private Long selectedSpareLocationId;

    public RecheckLocationForm(
            Activity activity,
            LifecycleOwner lifecycleOwner,
            RecheckSubmissionViewModel viewModel
    ) {
        this.activity = activity;
        this.viewModel = viewModel;
        floorLayout = activity.findViewById(R.id.assetFloorLayout);
        placeLayout = activity.findViewById(R.id.assetPlaceLayout);
        spareLocationLayout = activity.findViewById(R.id.spareLocationLayout);
        quantityLayout = activity.findViewById(R.id.quantityLayout);
        floorDropdown = activity.findViewById(R.id.floorDropdown);
        placeDropdown = activity.findViewById(R.id.placeDropdown);
        spareLocationDropdown = activity.findViewById(R.id.spareLocationDropdown);

        setupListeners();
        observeOptions(lifecycleOwner);
    }

    public void bind(String inventoryDomain, RecheckItemResponse item) {
        this.item = item;
        selectedFloorId = item.getFloorId();
        selectedPlaceId = item.getPlaceId();
        selectedSpareLocationId = item.getLocationId();

        String domain = inventoryDomain == null
                ? ""
                : inventoryDomain.trim().toUpperCase(Locale.ROOT);
        boolean asset = "ASSET".equals(domain);
        boolean sparePart = "SPARE_PART".equals(domain);

        floorLayout.setVisibility(asset ? View.VISIBLE : View.GONE);
        placeLayout.setVisibility(asset ? View.VISIBLE : View.GONE);
        spareLocationLayout.setVisibility(sparePart ? View.VISIBLE : View.GONE);
        quantityLayout.setVisibility(sparePart ? View.VISIBLE : View.GONE);

        if (asset) {
            floorDropdown.setText(expectedLabel(), false);
            placeDropdown.setText(expectedLabel(), false);
            viewModel.loadAssetFloors();
        } else if (sparePart) {
            spareLocationDropdown.setText(expectedLabel(), false);
            viewModel.loadSparePartLocations();
        }
    }

    public Long getSelectedFloorId() {
        return selectedFloorId;
    }

    public Long getSelectedPlaceId() {
        return selectedPlaceId;
    }

    public Long getSelectedSpareLocationId() {
        return selectedSpareLocationId;
    }

    private void setupListeners() {
        floorDropdown.setOnItemClickListener((parent, view, position, id) -> {
            HierarchyOptionResponse selected =
                    (HierarchyOptionResponse) parent.getItemAtPosition(position);
            selectedFloorId = selected == null ? null : selected.getId();
            selectedPlaceId = null;
            placeDropdown.setText("", false);
            if (selectedFloorId != null) {
                viewModel.loadAssetPlaces(selectedFloorId);
            }
        });
        placeDropdown.setOnItemClickListener((parent, view, position, id) -> {
            HierarchyOptionResponse selected =
                    (HierarchyOptionResponse) parent.getItemAtPosition(position);
            selectedPlaceId = selected == null ? null : selected.getId();
        });
        spareLocationDropdown.setOnItemClickListener(
                (parent, view, position, id) -> {
                    HierarchyOptionResponse selected =
                            (HierarchyOptionResponse) parent.getItemAtPosition(position);
                    selectedSpareLocationId =
                            selected == null ? null : selected.getId();
                }
        );
    }

    private void observeOptions(LifecycleOwner owner) {
        viewModel.getFloorState().observe(owner, state ->
                renderOptions(
                        state,
                        floorDropdown,
                        item == null ? null : item.getFloorId(),
                        option -> {
                            selectedFloorId = option.getId();
                            viewModel.loadAssetPlaces(option.getId());
                        }
                ));
        viewModel.getPlaceState().observe(owner, state ->
                renderOptions(
                        state,
                        placeDropdown,
                        item == null ? null : item.getPlaceId(),
                        option -> selectedPlaceId = option.getId()
                ));
        viewModel.getLocationState().observe(owner, state ->
                renderOptions(
                        state,
                        spareLocationDropdown,
                        item == null ? null : item.getLocationId(),
                        option -> selectedSpareLocationId = option.getId()
                ));
    }

    private void renderOptions(
            Resource<List<HierarchyOptionResponse>> state,
            AutoCompleteTextView dropdown,
            Long expectedId,
            OptionSelected callback
    ) {
        if (state == null) return;
        if (state.getStatus() == Resource.Status.ERROR) {
            Toast.makeText(activity, state.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        if (state.getStatus() != Resource.Status.SUCCESS) return;

        List<HierarchyOptionResponse> options = state.getData() == null
                ? new ArrayList<>()
                : state.getData();
        dropdown.setAdapter(new ArrayAdapter<>(
                activity,
                android.R.layout.simple_dropdown_item_1line,
                options
        ));
        for (HierarchyOptionResponse option : options) {
            if (option != null && Objects.equals(expectedId, option.getId())) {
                dropdown.setText(option.toString(), false);
                callback.onSelected(option);
                break;
            }
        }
    }

    private String expectedLabel() {
        return activity.getString(R.string.recheck_expected_selection);
    }

    private interface OptionSelected {
        void onSelected(HierarchyOptionResponse option);
    }
}
