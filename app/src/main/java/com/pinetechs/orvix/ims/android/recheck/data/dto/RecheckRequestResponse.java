package com.pinetechs.orvix.ims.android.recheck.data.dto;

import java.util.Collections;
import java.util.List;

public class RecheckRequestResponse {
    private Long id;
    private String requestNumber;
    private Long taskId;
    private String taskNumber;
    private String taskName;
    private String inventoryDomain;
    private String status;
    private String workAreaKey;
    private String workAreaLabel;
    private String instructions;
    private boolean imageRequired;
    private String dueAt;
    private ReviewUserResponse assignedTo;
    private ReviewUserResponse requestedBy;
    private List<RecheckItemResponse> items;
    private String startedAt;
    private String submittedAt;
    private String completedAt;
    private String cancelledAt;
    private String cancellationReason;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getTaskNumber() { return taskNumber; }
    public void setTaskNumber(String taskNumber) { this.taskNumber = taskNumber; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getInventoryDomain() { return inventoryDomain; }
    public void setInventoryDomain(String inventoryDomain) { this.inventoryDomain = inventoryDomain; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getWorkAreaKey() { return workAreaKey; }
    public void setWorkAreaKey(String workAreaKey) { this.workAreaKey = workAreaKey; }
    public String getWorkAreaLabel() { return workAreaLabel; }
    public void setWorkAreaLabel(String workAreaLabel) { this.workAreaLabel = workAreaLabel; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public boolean isImageRequired() { return imageRequired; }
    public void setImageRequired(boolean imageRequired) { this.imageRequired = imageRequired; }
    public String getDueAt() { return dueAt; }
    public void setDueAt(String dueAt) { this.dueAt = dueAt; }
    public ReviewUserResponse getAssignedTo() { return assignedTo; }
    public void setAssignedTo(ReviewUserResponse assignedTo) { this.assignedTo = assignedTo; }
    public ReviewUserResponse getRequestedBy() { return requestedBy; }
    public void setRequestedBy(ReviewUserResponse requestedBy) { this.requestedBy = requestedBy; }
    public List<RecheckItemResponse> getItems() {
        return items == null ? Collections.emptyList() : items;
    }
    public void setItems(List<RecheckItemResponse> items) { this.items = items; }
    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public String getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int submittedItemCount() {
        int count = 0;
        for (RecheckItemResponse item : getItems()) {
            if (item != null && !"PENDING".equalsIgnoreCase(item.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public boolean canStart() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean canWork() {
        return "IN_PROGRESS".equalsIgnoreCase(status);
    }
}
