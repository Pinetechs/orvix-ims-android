package com.pinetechs.orvix.ims.android.workarea.data.dto;

public class HierarchyOptionResponse {
    private Long id;
    private String code;
    private String name;
    private long scanCount;
    private String lastScanAt;
    private String progressStatus;
    private boolean completionEnabled;
    private boolean canComplete;
    private String completedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getScanCount() { return scanCount; }
    public void setScanCount(long scanCount) { this.scanCount = scanCount; }
    public String getLastScanAt() { return lastScanAt; }
    public void setLastScanAt(String lastScanAt) { this.lastScanAt = lastScanAt; }
    public String getProgressStatus() { return progressStatus; }
    public void setProgressStatus(String progressStatus) { this.progressStatus = progressStatus; }
    public boolean isCompletionEnabled() { return completionEnabled; }
    public void setCompletionEnabled(boolean completionEnabled) { this.completionEnabled = completionEnabled; }
    public boolean isCanComplete() { return canComplete; }
    public void setCanComplete(boolean canComplete) { this.canComplete = canComplete; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    @Override
    public String toString() {
        if (name == null || name.trim().isEmpty()) return code == null ? "-" : code;
        return code == null || code.trim().isEmpty() ? name : name + " (" + code + ")";
    }
}
