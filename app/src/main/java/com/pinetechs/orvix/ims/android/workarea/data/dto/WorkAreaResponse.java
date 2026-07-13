package com.pinetechs.orvix.ims.android.workarea.data.dto;

public class WorkAreaResponse {
    private Long id;
    private String type;
    private String code;
    private String name;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer matchedRecords;
    private Integer progress;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }
    public Integer getProcessedRecords() { return processedRecords; }
    public void setProcessedRecords(Integer processedRecords) { this.processedRecords = processedRecords; }
    public Integer getMatchedRecords() { return matchedRecords; }
    public void setMatchedRecords(Integer matchedRecords) { this.matchedRecords = matchedRecords; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
}
