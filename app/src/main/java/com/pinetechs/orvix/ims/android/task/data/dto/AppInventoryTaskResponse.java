package com.pinetechs.orvix.ims.android.task.data.dto;

public class AppInventoryTaskResponse {

    private Long id;
    private String taskNumber;
    private String inventoryDomain;
    private String status;
    private String companyName;
    private int plannedRecords;
    private int scannedRecords;
    private int mismatchRecords;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    public String getInventoryDomain() {
        return inventoryDomain;
    }

    public void setInventoryDomain(String inventoryDomain) {
        this.inventoryDomain = inventoryDomain;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getPlannedRecords() {
        return plannedRecords;
    }

    public void setPlannedRecords(int plannedRecords) {
        this.plannedRecords = plannedRecords;
    }

    public int getScannedRecords() {
        return scannedRecords;
    }

    public void setScannedRecords(int scannedRecords) {
        this.scannedRecords = scannedRecords;
    }

    public int getMismatchRecords() {
        return mismatchRecords;
    }

    public void setMismatchRecords(int mismatchRecords) {
        this.mismatchRecords = mismatchRecords;
    }
}
