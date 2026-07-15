package com.pinetechs.orvix.ims.android.scan.data.dto;

import java.util.ArrayList;
import java.util.List;

public class ScanResponse {
    private Long scanId;
    private Long currentAcceptedScanId;
    private Long itemId;
    private Long imageFileId;
    private String inventoryDomain;
    private String clientScanId;
    private String eventType;
    private String resultCode;
    private String messageKey;
    private boolean accepted;
    private boolean correctionAllowed;
    private boolean idempotentReplay;
    private String serverScannedAt;
    private List<String> mismatchFields = new ArrayList<>();

    public Long getScanId() { return scanId; }
    public void setScanId(Long scanId) { this.scanId = scanId; }
    public Long getCurrentAcceptedScanId() { return currentAcceptedScanId; }
    public void setCurrentAcceptedScanId(Long value) { this.currentAcceptedScanId = value; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getImageFileId() { return imageFileId; }
    public void setImageFileId(Long imageFileId) { this.imageFileId = imageFileId; }
    public String getInventoryDomain() { return inventoryDomain; }
    public void setInventoryDomain(String inventoryDomain) { this.inventoryDomain = inventoryDomain; }
    public String getClientScanId() { return clientScanId; }
    public void setClientScanId(String clientScanId) { this.clientScanId = clientScanId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getResultCode() { return resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }
    public String getMessageKey() { return messageKey; }
    public void setMessageKey(String messageKey) { this.messageKey = messageKey; }
    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
    public boolean isCorrectionAllowed() { return correctionAllowed; }
    public void setCorrectionAllowed(boolean value) { this.correctionAllowed = value; }
    public boolean isIdempotentReplay() { return idempotentReplay; }
    public void setIdempotentReplay(boolean value) { this.idempotentReplay = value; }
    public String getServerScannedAt() { return serverScannedAt; }
    public void setServerScannedAt(String serverScannedAt) { this.serverScannedAt = serverScannedAt; }
    public List<String> getMismatchFields() { return mismatchFields; }
    public void setMismatchFields(List<String> value) {
        mismatchFields = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }
}
