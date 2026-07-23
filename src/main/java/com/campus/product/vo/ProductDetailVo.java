package com.campus.product.vo;

import com.campus.comment.vo.CommentVo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailVo {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private List<ProductImageVo> images = new ArrayList<>();
    private String categoryName;
    private String contactInfo;
    private String status;
    private LocalDateTime createdAt;
    private String sellerUsername;
    private Long sellerId;
    private List<CommentVo> comments = new ArrayList<>();
    private long appointmentCount;
    @JsonProperty("is_favorited")
    private boolean favorited;
    @JsonProperty("is_appointed")
    private boolean appointed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<ProductImageVo> getImages() {
        return images;
    }

    public void setImages(List<ProductImageVo> images) {
        this.images = images;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public List<CommentVo> getComments() {
        return comments;
    }

    public void setComments(List<CommentVo> comments) {
        this.comments = comments;
    }

    public long getAppointmentCount() {
        return appointmentCount;
    }

    public void setAppointmentCount(long appointmentCount) {
        this.appointmentCount = appointmentCount;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public boolean isAppointed() {
        return appointed;
    }

    public void setAppointed(boolean appointed) {
        this.appointed = appointed;
    }
}
