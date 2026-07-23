package com.campus.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class ProductCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100)
    private String title;

    @NotBlank(message = "描述不能为空")
    @Size(max = 2000)
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须为正数")
    private BigDecimal price;

    @NotNull(message = "分类不能为空")
    private Long category;

    @NotBlank(message = "联系方式不能为空")
    @Size(max = 100)
    @JsonProperty("contact_info")
    private String contactInfo;

    /** Base64 data URLs (compatible with existing React frontend). */
    @JsonProperty("uploaded_images")
    private List<String> uploadedImages;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getCategory() {
        return category;
    }

    public void setCategory(Long category) {
        this.category = category;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public List<String> getUploadedImages() {
        return uploadedImages;
    }

    public void setUploadedImages(List<String> uploadedImages) {
        this.uploadedImages = uploadedImages;
    }
}
