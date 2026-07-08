package com.pinetechs.orvix.ims.android.scan.data.dto;

public class ScanRequest {

    private String barcode;
    private String locationCode;
    private String scanType;

    public ScanRequest(String barcode, String locationCode, String scanType) {
        this.barcode = barcode;
        this.locationCode = locationCode;
        this.scanType = scanType;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }
}
