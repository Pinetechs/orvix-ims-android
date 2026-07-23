package com.pinetechs.orvix.ims.android.recheck.data.dto;

public class RecheckIssueResponse {
    private Long id;
    private String issueType;
    private String status;
    private Long sourceScanId;
    private Long currentScanId;
    private String scanImageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getSourceScanId() { return sourceScanId; }
    public void setSourceScanId(Long sourceScanId) { this.sourceScanId = sourceScanId; }
    public Long getCurrentScanId() { return currentScanId; }
    public void setCurrentScanId(Long currentScanId) { this.currentScanId = currentScanId; }
    public String getScanImageUrl() { return scanImageUrl; }
    public void setScanImageUrl(String scanImageUrl) { this.scanImageUrl = scanImageUrl; }
}
