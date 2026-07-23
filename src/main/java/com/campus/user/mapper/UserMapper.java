package com.campus.user.mapper;

import com.campus.user.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT id, password, last_login, is_superuser AS superuser, username, is_active AS active, is_staff AS staff, created_at FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT id, password, last_login, is_superuser AS superuser, username, is_active AS active, is_staff AS staff, created_at FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT COUNT(*) FROM users")
    long countAll();

    @Select("SELECT id, password, last_login, is_superuser AS superuser, username, is_active AS active, is_staff AS staff, created_at FROM users ORDER BY created_at DESC")
    List<User> findAllOrderByCreatedAtDesc();

    @Insert("INSERT INTO users (password, is_superuser, username, is_active, is_staff, created_at) " +
            "VALUES (#{password}, #{superuser}, #{username}, #{active}, #{staff}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET is_active = #{active} WHERE id = #{id}")
    int updateActive(@Param("id") Long id, @Param("active") boolean active);
}
