package com.campus.product.mapper;

import com.campus.product.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {

    Product findById(Long id);

    List<Product> findActive(@Param("categoryId") Long categoryId, @Param("search") String search);

    List<Product> findBySeller(@Param("sellerId") Long sellerId, @Param("status") String status);

    List<Product> findPending();

    @Insert("""
            INSERT INTO products (title, description, price, category_id, seller_id, contact_info, status, reject_reason, is_deleted, created_at, updated_at)
            VALUES (#{title}, #{description}, #{price}, #{categoryId}, #{sellerId}, #{contactInfo}, #{status}, #{rejectReason}, #{deleted}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("""
            UPDATE products SET title=#{title}, description=#{description}, price=#{price}, category_id=#{categoryId},
            contact_info=#{contactInfo}, status=#{status}, reject_reason=#{rejectReason}, updated_at=#{updatedAt}
            WHERE id=#{id}
            """)
    int update(Product product);

    @Update("UPDATE products SET status = #{status}, reject_reason = #{rejectReason}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("rejectReason") String rejectReason);

    @Select("SELECT COUNT(*) FROM products WHERE is_deleted = FALSE")
    long countAll();

    @Select("SELECT COUNT(*) FROM products WHERE is_deleted = FALSE AND category_id = #{categoryId}")
    long countByCategory(Long categoryId);

    @Select("SELECT COUNT(*) FROM products WHERE is_deleted = FALSE AND status = #{status}")
    long countByStatusValue(String status);

    @Select("SELECT status, COUNT(*) AS cnt FROM products WHERE is_deleted = FALSE GROUP BY status")
    List<Map<String, Object>> countByStatusRaw();

    default Map<String, Long> countByStatus() {
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> row : countByStatusRaw()) {
            Object status = row.get("status");
            Object cnt = row.get("cnt");
            if (status != null && cnt != null) {
                map.put(String.valueOf(status), ((Number) cnt).longValue());
            }
        }
        return map;
    }

    @Select("SELECT COUNT(*) FROM products WHERE is_deleted = FALSE AND CAST(created_at AS DATE) = CURRENT_DATE")
    long countToday();
}
