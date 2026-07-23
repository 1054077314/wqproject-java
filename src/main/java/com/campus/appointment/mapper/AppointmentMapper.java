package com.campus.appointment.mapper;

import com.campus.appointment.entity.Appointment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AppointmentMapper {

    @Select("""
            SELECT a.id, a.buyer_id, a.product_id, a.status, a.created_at, a.updated_at,
                   p.title AS product_title, p.price AS product_price, u.username AS buyer_username
            FROM appointments a
            JOIN products p ON p.id = a.product_id
            JOIN users u ON u.id = a.buyer_id
            WHERE a.id = #{id}
            """)
    Appointment findById(Long id);

    @Select("SELECT COUNT(*) FROM appointments WHERE product_id = #{productId}")
    long countByProduct(Long productId);

    @Select("SELECT COUNT(*) FROM appointments WHERE buyer_id = #{buyerId} AND product_id = #{productId}")
    long countExists(@Param("buyerId") Long buyerId, @Param("productId") Long productId);

    default boolean exists(Long buyerId, Long productId) {
        return countExists(buyerId, productId) > 0;
    }

    @Insert("INSERT INTO appointments (buyer_id, product_id, status, created_at, updated_at) VALUES (#{buyerId}, #{productId}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Appointment appointment);

    @Update("UPDATE appointments SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(Appointment appointment);

    @Select("""
            SELECT a.id, a.buyer_id, a.product_id, a.status, a.created_at, a.updated_at,
                   p.title AS product_title, p.price AS product_price, u.username AS buyer_username
            FROM appointments a
            JOIN products p ON p.id = a.product_id
            JOIN users u ON u.id = a.buyer_id
            WHERE a.buyer_id = #{buyerId}
            ORDER BY a.created_at DESC
            """)
    List<Appointment> findByBuyer(Long buyerId);

    @Select("""
            SELECT a.id, a.buyer_id, a.product_id, a.status, a.created_at, a.updated_at,
                   p.title AS product_title, p.price AS product_price, u.username AS buyer_username
            FROM appointments a
            JOIN products p ON p.id = a.product_id
            JOIN users u ON u.id = a.buyer_id
            WHERE p.seller_id = #{sellerId}
            ORDER BY a.created_at DESC
            """)
    List<Appointment> findBySeller(Long sellerId);
}
