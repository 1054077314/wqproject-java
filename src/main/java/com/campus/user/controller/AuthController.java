package com.campus.user.controller;

import com.campus.common.ApiResponse;
import com.campus.common.PageResult;
import com.campus.security.SecurityUtils;
import com.campus.security.UserPrincipal;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.RegisterRequest;
import com.campus.user.dto.ToggleActiveRequest;
import com.campus.user.dto.UserView;
import com.campus.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("注册成功", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("登录成功", authService.login(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout(SecurityUtils.requireUser());
        return ResponseEntity.ok(ApiResponse.ok("退出成功", null));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserView>> profile() {
        return ResponseEntity.ok(ApiResponse.ok(authService.profile(SecurityUtils.requireUser())));
    }

    @GetMapping("/admin/users/")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResult<UserView> adminUsers(HttpServletRequest request) {
        return authService.listUsers(request);
    }

    @PutMapping("/admin/users/{id}/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserView>> toggleUser(@PathVariable Long id,
                                                            @Valid @RequestBody ToggleActiveRequest body) {
        UserPrincipal current = SecurityUtils.requireUser();
        return ResponseEntity.ok(ApiResponse.ok("操作成功",
                authService.toggleActive(id, body.getIsActive(), current)));
    }

    @GetMapping("/admin/statistics/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> statistics() {
        return ResponseEntity.ok(ApiResponse.ok(authService.statistics()));
    }
}
