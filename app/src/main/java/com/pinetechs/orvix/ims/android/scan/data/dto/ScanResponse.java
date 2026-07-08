package com.pinetechs.orvix.ims.android.scan.data.dto;

public class ScanResponse {

    private String status;
    private String message;
    private ScanItemResponse item;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ScanItemResponse getItem() {
        return item;
    }

    public void setItem(ScanItemResponse item) {
        this.item = item;
    }
}
