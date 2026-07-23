package com.campus.category.mapper;

import com.campus.category.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT id, name, sort_order, created_at FROM categories ORDER BY sort_order ASC, id ASC")
    List<Category> findAll();

    @Select("SELECT id, name, sort_order, created_at FROM categories WHERE id = #{id}")
    Category findById(Long id);

    @Select("SELECT id, name, sort_order, created_at FROM categories WHERE name = #{name}")
    Category findByName(String name);

    @Insert("INSERT INTO categories (name, sort_order, created_at) VALUES (#{name}, #{sortOrder}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE categories SET name = #{name}, sort_order = #{sortOrder} WHERE id = #{id}")
    int update(Category category);

    @Delete("DELETE FROM categories WHERE id = #{id}")
    int delete(Long id);
}
