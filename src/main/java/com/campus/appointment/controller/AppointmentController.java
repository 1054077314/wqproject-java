package com.campus.appointment.controller;

import com.campus.appointment.dto.AppointmentActionRequest;
import com.campus.appointment.dto.AppointmentCreateRequest;
import com.campus.appointment.service.AppointmentService;
import com.campus.appointment.vo.AppointmentVo;
import com.campus.common.ApiResponse;
import com.campus.common.PageResult;
import com.campus.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/appointments/")
    public ResponseEntity<ApiResponse<AppointmentVo>> create(@Valid @RequestBody AppointmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("预约成功",
                        appointmentService.create(request.getProductId(), SecurityUtils.requireUser())));
    }

    @GetMapping("/my-appointments/as-buyer/")
    public ResponseEntity<ApiResponse<PageResult<AppointmentVo>>> asBuyer(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                appointmentService.asBuyer(request, SecurityUtils.requireUser())));
    }

    @GetMapping("/my-appointments/as-seller/")
    public ResponseEntity<ApiResponse<PageResult<AppointmentVo>>> asSeller(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                appointmentService.asSeller(request, SecurityUtils.requireUser())));
    }

    @PatchMapping("/appointments/{id}/")
    public ResponseEntity<ApiResponse<AppointmentVo>> update(@PathVariable Long id,
                                                             @Valid @RequestBody AppointmentActionRequest request) {
        String msg = "cancel".equals(request.getAction()) ? "已取消预约" : "操作成功";
        return ResponseEntity.ok(ApiResponse.ok(msg,
                appointmentService.updateStatus(id, request.getAction(), SecurityUtils.requireUser())));
    }
}
