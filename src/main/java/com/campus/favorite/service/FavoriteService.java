package com.campus.favorite.service;

import com.campus.common.ApiResponse;
import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.PageUtils;
import com.campus.config.AppProperties;
import com.campus.favorite.entity.Favorite;
import com.campus.favorite.mapper.FavoriteMapper;
import com.campus.product.entity.Product;
import com.campus.product.mapper.ProductMapper;
import com.campus.product.service.FileStorageService;
import com.campus.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;
    private final AppProperties appProperties;

    public FavoriteService(FavoriteMapper favoriteMapper, ProductMapper productMapper,
                           FileStorageService fileStorageService, AppProperties appProperties) {
        this.favoriteMapper = favoriteMapper;
        this.productMapper = productMapper;
        this.fileStorageService = fileStorageService;
        this.appProperties = appProperties;
    }

    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggle(Long productId, UserPrincipal principal) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (product.getSellerId().equals(principal.getId())) {
            throw new BusinessException(400, "不能收藏自己的商品");
        }
        if (!"active".equals(product.getStatus())) {
            throw new BusinessException(400, "只能收藏已上架商品");
        }
        Favorite existing = favoriteMapper.findOne(principal.getId(), productId);
        if (existing != null) {
            favoriteMapper.delete(existing.getId());
            return ResponseEntity.ok(ApiResponse.ok("取消收藏", null));
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(principal.getId());
        favorite.setProductId(productId);
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(favorite);
        Favorite saved = favoriteMapper.findByUser(principal.getId()).stream()
                .filter(f -> f.getId().equals(favorite.getId()))
                .findFirst()
                .orElse(favorite);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("收藏成功", toView(saved)));
    }

    public PageResult<Map<String, Object>> myFavorites(HttpServletRequest request, UserPrincipal principal) {
        return PageUtils.paginate(request, appProperties.getPageSize(), () ->
                favoriteMapper.findByUser(principal.getId()).stream().map(this::toView).collect(Collectors.toList()));
    }

    private Map<String, Object> toView(Favorite f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", f.getId());
        m.put("product_id", f.getProductId());
        m.put("product_title", f.getProductTitle());
        m.put("product_price", f.getProductPrice());
        m.put("product_image", fileStorageService.toUrl(f.getProductImage()));
        m.put("created_at", f.getCreatedAt());
        return m;
    }
}
