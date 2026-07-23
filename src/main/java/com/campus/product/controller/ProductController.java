package com.campus.product.controller;

import com.campus.common.ApiResponse;
import com.campus.common.PageResult;
import com.campus.product.dto.ProductCreateRequest;
import com.campus.product.dto.ProductReviewRequest;
import com.campus.product.dto.ProductUpdateRequest;
import com.campus.product.service.ProductService;
import com.campus.product.vo.MyProductVo;
import com.campus.product.vo.ProductDetailVo;
import com.campus.product.vo.ProductListItemVo;
import com.campus.product.vo.ProductPayloadVo;
import com.campus.security.SecurityUtils;
import com.campus.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products/")
    public ResponseEntity<ApiResponse<PageResult<ProductListItemVo>>> list(
            HttpServletRequest request,
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(productService.listActive(request, categoryId, search)));
    }

    @PostMapping(value = "/products/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductPayloadVo>> createJson(@Valid @RequestBody ProductCreateRequest request) {
        UserPrincipal user = SecurityUtils.requireUser();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("发布成功", productService.create(request, null, user)));
    }

    @PostMapping(value = "/products/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductPayloadVo>> createMultipart(
            @Valid @ModelAttribute ProductCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        UserPrincipal user = SecurityUtils.requireUser();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("发布成功", productService.create(request, files, user)));
    }

    @GetMapping("/products/{id}/")
    public ResponseEntity<ApiResponse<ProductDetailVo>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.detail(id, SecurityUtils.currentUser().orElse(null))));
    }

    @PutMapping(value = "/products/{id}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductPayloadVo>> updateJson(@PathVariable Long id,
                                                                    @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("更新成功",
                productService.update(id, request, null, SecurityUtils.requireUser())));
    }

    @PutMapping(value = "/products/{id}/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductPayloadVo>> updateMultipart(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(ApiResponse.ok("更新成功",
                productService.update(id, request, files, SecurityUtils.requireUser())));
    }

    @DeleteMapping("/products/{id}/")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.offline(id, SecurityUtils.requireUser());
        return ResponseEntity.ok(ApiResponse.ok("下架成功", null));
    }

    @GetMapping("/my-products/")
    public ResponseEntity<ApiResponse<PageResult<MyProductVo>>> myProducts(
            HttpServletRequest request,
            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(
                productService.myProducts(request, status, SecurityUtils.requireUser())));
    }

    @PostMapping("/admin/products/{id}/review/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> review(@PathVariable Long id,
                                                    @Valid @RequestBody ProductReviewRequest request) {
        productService.review(id, request);
        String msg = "approve".equals(request.getAction()) ? "审核通过" : "已驳回";
        return ResponseEntity.ok(ApiResponse.ok(msg, null));
    }

    @GetMapping("/admin/pending-products/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<ProductListItemVo>>> pending(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.pendingList(request)));
    }
}
