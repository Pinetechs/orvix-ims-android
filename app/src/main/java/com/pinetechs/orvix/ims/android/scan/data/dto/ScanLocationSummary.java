package com.pinetechs.orvix.ims.android.scan.data.dto;

public class ScanLocationSummary {
    private Long branchId;
    private String branchName;
    private Long locationId;
    private String locationCode;
    private String locationName;
    private Long floorId;
    private String floorName;
    private Long placeId;
    private String placeName;

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long value) { branchId = value; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String value) { branchName = value; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long value) { locationId = value; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String value) { locationCode = value; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String value) { locationName = value; }
    public Long getFloorId() { return floorId; }
    public void setFloorId(Long value) { floorId = value; }
    public String getFloorName() { return floorName; }
    public void setFloorName(String value) { floorName = value; }
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long value) { placeId = value; }
    public String getPlaceName() { return placeName; }
    public void setPlaceName(String value) { placeName = value; }
}
