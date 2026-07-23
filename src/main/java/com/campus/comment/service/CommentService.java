package com.campus.comment.service;

import com.campus.comment.entity.Comment;
import com.campus.comment.mapper.CommentMapper;
import com.campus.comment.vo.CommentVo;
import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.PageUtils;
import com.campus.config.AppProperties;
import com.campus.product.mapper.ProductMapper;
import com.campus.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final ProductMapper productMapper;
    private final AppProperties appProperties;

    public CommentService(CommentMapper commentMapper, ProductMapper productMapper, AppProperties appProperties) {
        this.commentMapper = commentMapper;
        this.productMapper = productMapper;
        this.appProperties = appProperties;
    }

    @Transactional
    public CommentVo create(Long productId, String content, UserPrincipal principal) {
        if (productMapper.findById(productId) == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, "留言内容不能为空");
        }
        Comment comment = new Comment();
        comment.setUserId(principal.getId());
        comment.setProductId(productId);
        comment.setContent(content.trim());
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        return toVo(commentMapper.findById(comment.getId()));
    }

    public PageResult<CommentVo> listByProduct(HttpServletRequest request, Long productId) {
        if (productMapper.findById(productId) == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return PageUtils.paginate(request, appProperties.getPageSize(), () ->
                commentMapper.findByProductId(productId).stream().map(this::toVo).collect(Collectors.toList()));
    }

    private CommentVo toVo(Comment c) {
        CommentVo vo = new CommentVo();
        vo.setId(c.getId());
        vo.setContent(c.getContent());
        vo.setUsername(c.getUsername());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }
}
