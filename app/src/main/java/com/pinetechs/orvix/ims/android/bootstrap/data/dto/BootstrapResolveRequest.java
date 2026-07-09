package com.pinetechs.orvix.ims.android.bootstrap.data.dto;

public class BootstrapResolveRequest {

    private String clientCode;

    private final String  platform = "ANDROID";

    private Integer currentVersionCode;

    private String deviceId;

    public BootstrapResolveRequest(String clientCode, Integer currentVersionCode, String deviceId) {
        this.clientCode = clientCode;
        this.currentVersionCode = currentVersionCode;
        this.deviceId = deviceId;
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getPlatform() {
        return platform;
    }

    public Integer getCurrentVersionCode() {
        return currentVersionCode;
    }

    public void setCurrentVersionCode(Integer currentVersionCode) {
        this.currentVersionCode = currentVersionCode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
