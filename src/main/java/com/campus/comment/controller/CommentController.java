package com.campus.comment.controller;

import com.campus.comment.dto.CommentCreateRequest;
import com.campus.comment.service.CommentService;
import com.campus.comment.vo.CommentVo;
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
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/comments/")
    public ResponseEntity<ApiResponse<CommentVo>> create(@Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("留言成功",
                        commentService.create(request.getProductId(), request.getContent(), SecurityUtils.requireUser())));
    }

    @GetMapping("/products/{productId}/comments/")
    public ResponseEntity<ApiResponse<PageResult<CommentVo>>> list(
            HttpServletRequest request, @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.listByProduct(request, productId)));
    }
}
