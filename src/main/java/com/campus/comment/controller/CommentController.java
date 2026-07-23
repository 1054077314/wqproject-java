package com.campus.comment.controller;

import com.campus.comment.service.CommentService;
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
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/comments/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(String.valueOf(body.get("product_id")));
        String content = body.get("content") == null ? null : String.valueOf(body.get("content"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("留言成功",
                        commentService.create(productId, content, SecurityUtils.requireUser())));
    }

    @GetMapping("/products/{productId}/comments/")
    public PageResult<Map<String, Object>> list(HttpServletRequest request, @PathVariable Long productId) {
        return commentService.listByProduct(request, productId);
    }
}
