package com.campus.favorite.mapper;

import com.campus.favorite.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId} AND product_id = #{productId}")
    long countExists(@Param("userId") Long userId, @Param("productId") Long productId);

    default boolean exists(Long userId, Long productId) {
        return countExists(userId, productId) > 0;
    }

    @Select("SELECT id, user_id, product_id, created_at FROM favorites WHERE user_id = #{userId} AND product_id = #{productId}")
    Favorite findOne(@Param("userId") Long userId, @Param("productId") Long productId);

    @Insert("INSERT INTO favorites (user_id, product_id, created_at) VALUES (#{userId}, #{productId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Favorite favorite);

    @Delete("DELETE FROM favorites WHERE id = #{id}")
    int delete(Long id);

    @Select("""
            SELECT f.id, f.user_id, f.product_id, f.created_at,
                   p.title AS product_title, p.price AS product_price,
                   (SELECT pi.image FROM product_images pi
                    WHERE pi.product_id = p.id AND pi.is_deleted = FALSE
                    ORDER BY pi.id ASC LIMIT 1) AS product_image
            FROM favorites f
            JOIN products p ON p.id = f.product_id
            WHERE f.user_id = #{userId}
              AND p.is_deleted = FALSE
              AND p.status = 'active'
            ORDER BY f.created_at DESC
            """)
    List<Favorite> findByUser(Long userId);
}
