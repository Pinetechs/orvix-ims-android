package com.pinetechs.orvix.ims.android.scan.data.dto;

public class ScanItemResponse {

    private Long id;
    private String barcode;
    private String vin;
    private String itemCode;
    private String name;
    private String expectedLocationCode;
    private String scannedLocationCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpectedLocationCode() {
        return expectedLocationCode;
    }

    public void setExpectedLocationCode(String expectedLocationCode) {
        this.expectedLocationCode = expectedLocationCode;
    }

    public String getScannedLocationCode() {
        return scannedLocationCode;
    }

    public void setScannedLocationCode(String scannedLocationCode) {
        this.scannedLocationCode = scannedLocationCode;
    }
}
