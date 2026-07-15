package com.pinetechs.orvix.ims.android.scan.data.dto;

import java.math.BigDecimal;

public class ScanCorrectionRequest {
    private String clientScanId;
    private String reason;
    private Long branchId;
    private Long locationId;
    private Long floorId;
    private Long placeId;
    private BigDecimal countedQty;
    private String deviceScannedAt;
    private String deviceId;
    private String symbology;
    private String imageSource;

    /** Required by Gson and used by the correction request factory. */
    public ScanCorrectionRequest() {
    }

    public String getClientScanId() { return clientScanId; }
    public void setClientScanId(String value) { clientScanId = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { reason = value; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long value) { branchId = value; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long value) { locationId = value; }
    public Long getFloorId() { return floorId; }
    public void setFloorId(Long value) { floorId = value; }
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long value) { placeId = value; }
    public BigDecimal getCountedQty() { return countedQty; }
    public void setCountedQty(BigDecimal value) { countedQty = value; }
    public String getDeviceScannedAt() { return deviceScannedAt; }
    public void setDeviceScannedAt(String value) { deviceScannedAt = value; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String value) { deviceId = value; }
    public String getSymbology() { return symbology; }
    public void setSymbology(String value) { symbology = value; }
    public String getImageSource() { return imageSource; }
    public void setImageSource(String value) { imageSource = value; }
}
