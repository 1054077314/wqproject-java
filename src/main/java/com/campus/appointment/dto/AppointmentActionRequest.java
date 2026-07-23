package com.campus.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AppointmentActionRequest {

    @NotBlank(message = "操作不能为空")
    @Pattern(regexp = "confirm|reject|cancel", message = "操作只能是 confirm、reject 或 cancel")
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
