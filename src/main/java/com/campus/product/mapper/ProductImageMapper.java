package com.campus.product.mapper;

import com.campus.product.entity.ProductImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductImageMapper {

    @Select("SELECT id, product_id, image, is_deleted AS deleted, created_at, updated_at FROM product_images WHERE product_id = #{productId} AND is_deleted = FALSE ORDER BY id ASC")
    List<ProductImage> findByProductId(Long productId);

    @Insert("INSERT INTO product_images (product_id, image, is_deleted, created_at, updated_at) VALUES (#{productId}, #{image}, #{deleted}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductImage image);

    @Update("UPDATE product_images SET is_deleted = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int softDelete(Long id);
}
