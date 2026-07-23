package com.campus.user.mapper;

import com.campus.user.entity.Token;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TokenMapper {

    @Select("SELECT `key`, user_id, created_at, expires_at FROM tokens WHERE `key` = #{key}")
    Token findByKey(String key);

    @Insert("INSERT INTO tokens (`key`, user_id, created_at, expires_at) VALUES (#{key}, #{userId}, #{createdAt}, #{expiresAt})")
    int insert(Token token);

    @Delete("DELETE FROM tokens WHERE `key` = #{key}")
    int deleteByKey(String key);
}
