package com.campus.appointment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class AppointmentCreateRequest {

    @NotNull(message = "商品不能为空")
    @JsonProperty("product_id")
    private Long productId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
