package com.campus.product.dto;

import jakarta.validation.constraints.NotBlank;

public class ProductReviewRequest {

    @NotBlank(message = "action 不能为空")
    private String action;

    private String rejectReason;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
