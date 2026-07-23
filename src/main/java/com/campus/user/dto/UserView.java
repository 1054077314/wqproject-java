package com.campus.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class UserView {

    private Long id;
    private String username;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_staff")
    private Boolean isStaff;

    private LocalDateTime createdAt;

    public UserView() {
    }

    public UserView(Long id, String username, Boolean isActive, Boolean isStaff, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.isActive = isActive;
        this.isStaff = isStaff;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsStaff() {
        return isStaff;
    }

    public void setIsStaff(Boolean isStaff) {
        this.isStaff = isStaff;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
