package com.campus.comment.mapper;

import com.campus.comment.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Select("""
            SELECT c.id, c.user_id, c.product_id, c.content, c.created_at, u.username
            FROM comments c
            JOIN users u ON u.id = c.user_id
            WHERE c.product_id = #{productId}
            ORDER BY c.created_at ASC
            """)
    List<Comment> findByProductId(Long productId);

    @Insert("INSERT INTO comments (user_id, product_id, content, created_at) VALUES (#{userId}, #{productId}, #{content}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Select("""
            SELECT c.id, c.user_id, c.product_id, c.content, c.created_at, u.username
            FROM comments c
            JOIN users u ON u.id = c.user_id
            WHERE c.id = #{id}
            """)
    Comment findById(Long id);
}
