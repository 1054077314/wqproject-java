package com.campus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String mediaRoot = "./media";
    private String mediaUrl = "/media/";
    private int tokenExpireDays = 7;
    private int pageSize = 20;
    /** Comma-separated allowed CORS origins. */
    private String corsAllowedOrigins = "http://localhost:5173,http://127.0.0.1:5173";
    private int rateLimitLoginPerMinute = 20;
    private int rateLimitRegisterPerMinute = 10;

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

    public String getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public List<String> corsOriginList() {
        if (corsAllowedOrigins == null || corsAllowedOrigins.isBlank()) {
            return List.of();
        }
        return Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public int getRateLimitLoginPerMinute() {
        return rateLimitLoginPerMinute;
    }

    public void setRateLimitLoginPerMinute(int rateLimitLoginPerMinute) {
        this.rateLimitLoginPerMinute = rateLimitLoginPerMinute;
    }

    public int getRateLimitRegisterPerMinute() {
        return rateLimitRegisterPerMinute;
    }

    public void setRateLimitRegisterPerMinute(int rateLimitRegisterPerMinute) {
        this.rateLimitRegisterPerMinute = rateLimitRegisterPerMinute;
    }
}
