package com.pinetechs.orvix.ims.android.scan.data.dto;

import java.math.BigDecimal;

public class ScanItemSummary {
    private String code;
    private String barcode;
    private String displayName;
    private String category;
    private String type;
    private String brand;
    private String make;
    private String model;
    private Integer modelYear;
    private String color;
    private String condition;
    private BigDecimal countedQuantity;

    public String getCode() { return code; }
    public void setCode(String value) { code = value; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String value) { barcode = value; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String value) { displayName = value; }
    public String getCategory() { return category; }
    public void setCategory(String value) { category = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public String getBrand() { return brand; }
    public void setBrand(String value) { brand = value; }
    public String getMake() { return make; }
    public void setMake(String value) { make = value; }
    public String getModel() { return model; }
    public void setModel(String value) { model = value; }
    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer value) { modelYear = value; }
    public String getColor() { return color; }
    public void setColor(String value) { color = value; }
    public String getCondition() { return condition; }
    public void setCondition(String value) { condition = value; }
    public BigDecimal getCountedQuantity() { return countedQuantity; }
    public void setCountedQuantity(BigDecimal value) { countedQuantity = value; }
}
