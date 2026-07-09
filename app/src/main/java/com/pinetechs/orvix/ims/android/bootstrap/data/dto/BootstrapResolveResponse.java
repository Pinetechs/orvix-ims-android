package com.pinetechs.orvix.ims.android.bootstrap.data.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BootstrapResolveResponse {

    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        @SerializedName("client")
        @Expose
        private Client client;
        @SerializedName("theme")
        @Expose
        private Theme theme;
        @SerializedName("maintenance")
        @Expose
        private Maintenance maintenance;
        @SerializedName("update")
        @Expose
        private Update update;

        public Client getClient() {
            return client;
        }

        public void setClient(Client client) {
            this.client = client;
        }

        public Theme getTheme() {
            return theme;
        }

        public void setTheme(Theme theme) {
            this.theme = theme;
        }

        public Maintenance getMaintenance() {
            return maintenance;
        }

        public void setMaintenance(Maintenance maintenance) {
            this.maintenance = maintenance;
        }

        public Update getUpdate() {
            return update;
        }

        public void setUpdate(Update update) {
            this.update = update;
        }
    }

    public static class Client {

        @SerializedName("clientCode")
        @Expose
        private String clientCode;
        @SerializedName("clientName")
        @Expose
        private String clientName;
        @SerializedName("apiBaseUrl")
        @Expose
        private String apiBaseUrl;
        @SerializedName("active")
        @Expose
        private Boolean active;

        public String getClientCode() {
            return clientCode;
        }

        public void setClientCode(String clientCode) {
            this.clientCode = clientCode;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }

    public static class Maintenance {

        @SerializedName("enabled")
        @Expose
        private Boolean enabled;
        @SerializedName("message")
        @Expose
        private Object message;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Object getMessage() {
            return message;
        }

        public void setMessage(Object message) {
            this.message = message;
        }
    }

    public static class Theme {

        @SerializedName("logoUrl")
        @Expose
        private String logoUrl;
        @SerializedName("splashUrl")
        @Expose
        private Object splashUrl;
        @SerializedName("primaryColor")
        @Expose
        private String primaryColor;
        @SerializedName("secondaryColor")
        @Expose
        private String secondaryColor;

        public String getLogoUrl() {
            return logoUrl;
        }

        public void setLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
        }

        public Object getSplashUrl() {
            return splashUrl;
        }

        public void setSplashUrl(Object splashUrl) {
            this.splashUrl = splashUrl;
        }

        public String getPrimaryColor() {
            return primaryColor;
        }

        public void setPrimaryColor(String primaryColor) {
            this.primaryColor = primaryColor;
        }

        public String getSecondaryColor() {
            return secondaryColor;
        }

        public void setSecondaryColor(String secondaryColor) {
            this.secondaryColor = secondaryColor;
        }
    }

    public static class Update {

        @SerializedName("required")
        @Expose
        private Boolean required;
        @SerializedName("force")
        @Expose
        private Boolean force;
        @SerializedName("currentVersionCode")
        @Expose
        private Integer currentVersionCode;
        @SerializedName("latestVersionCode")
        @Expose
        private Integer latestVersionCode;
        @SerializedName("latestVersionName")
        @Expose
        private String latestVersionName;
        @SerializedName("minSupportedVersionCode")
        @Expose
        private Integer minSupportedVersionCode;
        @SerializedName("apkUrl")
        @Expose
        private String apkUrl;
        @SerializedName("checksumSha256")
        @Expose
        private String checksumSha256;
        @SerializedName("apkFileSize")
        @Expose
        private Integer apkFileSize;
        @SerializedName("releaseNotes")
        @Expose
        private String releaseNotes;

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }

        public Boolean getForce() {
            return force;
        }

        public void setForce(Boolean force) {
            this.force = force;
        }

        public Integer getCurrentVersionCode() {
            return currentVersionCode;
        }

        public void setCurrentVersionCode(Integer currentVersionCode) {
            this.currentVersionCode = currentVersionCode;
        }

        public Integer getLatestVersionCode() {
            return latestVersionCode;
        }

        public void setLatestVersionCode(Integer latestVersionCode) {
            this.latestVersionCode = latestVersionCode;
        }

        public String getLatestVersionName() {
            return latestVersionName;
        }

        public void setLatestVersionName(String latestVersionName) {
            this.latestVersionName = latestVersionName;
        }

        public Integer getMinSupportedVersionCode() {
            return minSupportedVersionCode;
        }

        public void setMinSupportedVersionCode(Integer minSupportedVersionCode) {
            this.minSupportedVersionCode = minSupportedVersionCode;
        }

        public String getApkUrl() {
            return apkUrl;
        }

        public void setApkUrl(String apkUrl) {
            this.apkUrl = apkUrl;
        }

        public String getChecksumSha256() {
            return checksumSha256;
        }

        public void setChecksumSha256(String checksumSha256) {
            this.checksumSha256 = checksumSha256;
        }

        public Integer getApkFileSize() {
            return apkFileSize;
        }

        public void setApkFileSize(Integer apkFileSize) {
            this.apkFileSize = apkFileSize;
        }

        public String getReleaseNotes() {
            return releaseNotes;
        }

        public void setReleaseNotes(String releaseNotes) {
            this.releaseNotes = releaseNotes;
        }
    }
}
