package com.campus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String mediaRoot = "./media";
    private String mediaUrl = "/media/";
    private int tokenExpireDays = 7;
    private int pageSize = 20;

    public String getMediaRoot() {
        return mediaRoot;
    }

    public void setMediaRoot(String mediaRoot) {
        this.mediaRoot = mediaRoot;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public int getTokenExpireDays() {
        return tokenExpireDays;
    }

    public void setTokenExpireDays(int tokenExpireDays) {
        this.tokenExpireDays = tokenExpireDays;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
