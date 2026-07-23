package com.campus.favorite.controller;

import com.campus.common.ApiResponse;
import com.campus.common.PageResult;
import com.campus.favorite.service.FavoriteService;
import com.campus.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/favorites/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggle(@RequestBody Map<String, Long> body) {
        return favoriteService.toggle(body.get("product_id"), SecurityUtils.requireUser());
    }

    @GetMapping("/my-favorites/")
    public PageResult<Map<String, Object>> myFavorites(HttpServletRequest request) {
        return favoriteService.myFavorites(request, SecurityUtils.requireUser());
    }
}
