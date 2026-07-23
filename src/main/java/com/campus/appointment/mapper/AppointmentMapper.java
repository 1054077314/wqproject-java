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

    @Select("SELECT COUNT(*) FROM appointments WHERE product_id = #{productId} AND status = 'pending'")
    long countPendingByProduct(Long productId);

    @Select("""
            SELECT COUNT(*) FROM appointments
            WHERE buyer_id = #{buyerId} AND product_id = #{productId}
              AND status IN ('pending', 'confirmed')
            """)
    long countExists(@Param("buyerId") Long buyerId, @Param("productId") Long productId);

    default boolean exists(Long buyerId, Long productId) {
        return countExists(buyerId, productId) > 0;
    }

    @Select("""
            SELECT a.id, a.buyer_id, a.product_id, a.status, a.created_at, a.updated_at,
                   p.title AS product_title, p.price AS product_price, u.username AS buyer_username
            FROM appointments a
            JOIN products p ON p.id = a.product_id
            JOIN users u ON u.id = a.buyer_id
            WHERE a.buyer_id = #{buyerId} AND a.product_id = #{productId}
            """)
    Appointment findByBuyerAndProduct(@Param("buyerId") Long buyerId, @Param("productId") Long productId);

    @Insert("INSERT INTO appointments (buyer_id, product_id, status, created_at, updated_at) VALUES (#{buyerId}, #{productId}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Appointment appointment);

    /** Optimistic CAS: only update when current status matches expected. */
    @Update("""
            UPDATE appointments SET status = #{newStatus}, updated_at = #{updatedAt}
            WHERE id = #{id} AND status = #{expectedStatus}
            """)
    int casStatus(@Param("id") Long id,
                   @Param("expectedStatus") String expectedStatus,
                   @Param("newStatus") String newStatus,
                   @Param("updatedAt") java.time.LocalDateTime updatedAt);

    /** Reopen a closed appointment under unique(buyer, product). */
    @Update("""
            UPDATE appointments SET status = 'pending', updated_at = #{updatedAt}
            WHERE id = #{id} AND status IN ('cancelled', 'rejected')
            """)
    int reopenIfClosed(@Param("id") Long id, @Param("updatedAt") java.time.LocalDateTime updatedAt);

    /** Close competing requests when one appointment is confirmed. */
    @Update("""
            UPDATE appointments SET status = 'rejected', updated_at = #{updatedAt}
            WHERE product_id = #{productId} AND status = 'pending' AND id <> #{excludeId}
            """)
    int rejectOtherPending(@Param("productId") Long productId,
                           @Param("excludeId") Long excludeId,
                           @Param("updatedAt") java.time.LocalDateTime updatedAt);

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
