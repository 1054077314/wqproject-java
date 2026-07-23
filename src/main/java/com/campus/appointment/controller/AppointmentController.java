package com.campus.appointment.controller;

import com.campus.appointment.service.AppointmentService;
import com.campus.common.ApiResponse;
import com.campus.common.PageResult;
import com.campus.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/appointments/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Long> body) {
        Long productId = body.get("product_id");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("预约成功",
                        appointmentService.create(productId, SecurityUtils.requireUser())));
    }

    @GetMapping("/my-appointments/as-buyer/")
    public PageResult<Map<String, Object>> asBuyer(HttpServletRequest request) {
        return appointmentService.asBuyer(request, SecurityUtils.requireUser());
    }

    @GetMapping("/my-appointments/as-seller/")
    public PageResult<Map<String, Object>> asSeller(HttpServletRequest request) {
        return appointmentService.asSeller(request, SecurityUtils.requireUser());
    }

    @PatchMapping("/appointments/{id}/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable Long id,
                                                                   @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok("操作成功",
                appointmentService.updateStatus(id, body.get("action"), SecurityUtils.requireUser())));
    }
}
