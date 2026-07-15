package com.pinetechs.orvix.ims.android.workarea.data.dto;

public class HierarchyOptionResponse {
    private Long id;
    private String code;
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        if (name == null || name.trim().isEmpty()) return code == null ? "-" : code;
        return code == null || code.trim().isEmpty() ? name : name + " (" + code + ")";
    }
}
