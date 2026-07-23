package com.campus.audit.controller;

import com.campus.audit.service.AuditService;
import com.campus.audit.vo.AuditLogVo;
import com.campus.common.ApiResponse;
import com.campus.common.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/admin/audit-logs/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<AuditLogVo>>> list(
            HttpServletRequest request,
            @RequestParam(value = "action", required = false) String action) {
        return ResponseEntity.ok(ApiResponse.ok(auditService.list(request, action)));
    }
}
