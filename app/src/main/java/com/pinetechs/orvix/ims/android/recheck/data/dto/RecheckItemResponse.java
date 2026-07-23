package com.pinetechs.orvix.ims.android.recheck.data.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class RecheckItemResponse {
    private Long id;
    private String status;
    private List<RecheckIssueResponse> issues;
    private Long referenceItemId;
    private Long resolvedItemId;
    private Long previousScanId;
    private Long acceptedScanId;
    private String itemCode;
    private String itemDescription;
    private String expectedLocation;
    private String previousResult;
    private BigDecimal expectedQuantity;
    private String result;
    private String scannedCode;
    private Long branchId;
    private Long locationId;
    private Long floorId;
    private Long placeId;
    private BigDecimal countedQuantity;
    private String reasonCode;
    private String note;
    private boolean hasEvidenceImage;
    private String evidenceImageUrl;
    private String submittedAt;
    private String reviewedAt;
    private ReviewUserResponse reviewedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<RecheckIssueResponse> getIssues() {
        return issues == null ? Collections.emptyList() : issues;
    }
    public void setIssues(List<RecheckIssueResponse> issues) { this.issues = issues; }
    public Long getReferenceItemId() { return referenceItemId; }
    public void setReferenceItemId(Long referenceItemId) { this.referenceItemId = referenceItemId; }
    public Long getResolvedItemId() { return resolvedItemId; }
    public void setResolvedItemId(Long resolvedItemId) { this.resolvedItemId = resolvedItemId; }
    public Long getPreviousScanId() { return previousScanId; }
    public void setPreviousScanId(Long previousScanId) { this.previousScanId = previousScanId; }
    public Long getAcceptedScanId() { return acceptedScanId; }
    public void setAcceptedScanId(Long acceptedScanId) { this.acceptedScanId = acceptedScanId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
    public String getExpectedLocation() { return expectedLocation; }
    public void setExpectedLocation(String expectedLocation) { this.expectedLocation = expectedLocation; }
    public String getPreviousResult() { return previousResult; }
    public void setPreviousResult(String previousResult) { this.previousResult = previousResult; }
    public BigDecimal getExpectedQuantity() { return expectedQuantity; }
    public void setExpectedQuantity(BigDecimal expectedQuantity) { this.expectedQuantity = expectedQuantity; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getScannedCode() { return scannedCode; }
    public void setScannedCode(String scannedCode) { this.scannedCode = scannedCode; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getFloorId() { return floorId; }
    public void setFloorId(Long floorId) { this.floorId = floorId; }
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
    public BigDecimal getCountedQuantity() { return countedQuantity; }
    public void setCountedQuantity(BigDecimal countedQuantity) { this.countedQuantity = countedQuantity; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isHasEvidenceImage() { return hasEvidenceImage; }
    public void setHasEvidenceImage(boolean hasEvidenceImage) { this.hasEvidenceImage = hasEvidenceImage; }
    public String getEvidenceImageUrl() { return evidenceImageUrl; }
    public void setEvidenceImageUrl(String evidenceImageUrl) { this.evidenceImageUrl = evidenceImageUrl; }
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
    public ReviewUserResponse getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(ReviewUserResponse reviewedBy) { this.reviewedBy = reviewedBy; }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }
}
