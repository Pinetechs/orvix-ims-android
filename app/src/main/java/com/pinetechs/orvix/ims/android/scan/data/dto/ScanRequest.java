package com.pinetechs.orvix.ims.android.scan.data.dto;

import java.math.BigDecimal;

public class ScanRequest {
    private String clientScanId;
    private String code;
    private Long branchId;
    private Long locationId;
    private Long floorId;
    private Long placeId;
    private BigDecimal countedQty;
    private String deviceScannedAt;
    private String deviceId;
    private String symbology;
    private String imageSource;
    private String notes;

    /** Required by Gson and used by the domain-specific request factory. */
    public ScanRequest() {
    }

    public String getClientScanId() { return clientScanId; }
    public void setClientScanId(String clientScanId) { this.clientScanId = clientScanId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getFloorId() { return floorId; }
    public void setFloorId(Long floorId) { this.floorId = floorId; }
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
    public BigDecimal getCountedQty() { return countedQty; }
    public void setCountedQty(BigDecimal countedQty) { this.countedQty = countedQty; }
    public String getDeviceScannedAt() { return deviceScannedAt; }
    public void setDeviceScannedAt(String deviceScannedAt) { this.deviceScannedAt = deviceScannedAt; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getSymbology() { return symbology; }
    public void setSymbology(String symbology) { this.symbology = symbology; }
    public String getImageSource() { return imageSource; }
    public void setImageSource(String imageSource) { this.imageSource = imageSource; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
