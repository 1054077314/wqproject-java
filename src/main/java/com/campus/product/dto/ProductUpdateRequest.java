package com.campus.product.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductUpdateRequest {

    private String title;
    private String description;
    private BigDecimal price;
    private Long category;
    private String contactInfo;
    private List<String> uploadedImages;
    private String keepImageIds;

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

    public String getKeepImageIds() {
        return keepImageIds;
    }

    public void setKeepImageIds(String keepImageIds) {
        this.keepImageIds = keepImageIds;
    }
}
