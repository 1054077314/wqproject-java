package com.campus.favorite.controller;

import com.campus.common.ApiResponse;
import com.campus.common.PageResult;
import com.campus.favorite.dto.FavoriteToggleRequest;
import com.campus.favorite.service.FavoriteService;
import com.campus.favorite.vo.FavoriteVo;
import com.campus.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/favorites/")
    public ResponseEntity<ApiResponse<FavoriteVo>> toggle(@Valid @RequestBody FavoriteToggleRequest request) {
        return favoriteService.toggle(request.getProductId(), SecurityUtils.requireUser());
    }

    @GetMapping("/my-favorites/")
    public ResponseEntity<ApiResponse<PageResult<FavoriteVo>>> myFavorites(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                favoriteService.myFavorites(request, SecurityUtils.requireUser())));
    }
}
