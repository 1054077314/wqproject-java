package com.campus.comment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CommentCreateRequest {

    @NotNull(message = "商品不能为空")
    @JsonProperty("product_id")
    private Long productId;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言最多 500 字")
    private String content;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
