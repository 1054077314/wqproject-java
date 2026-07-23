package com.campus.category.service;

import com.campus.category.dto.CategoryRequest;
import com.campus.category.entity.Category;
import com.campus.category.mapper.CategoryMapper;
import com.campus.common.BusinessException;
import com.campus.config.CacheConfig;
import com.campus.product.mapper.ProductMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    public CategoryService(CategoryMapper categoryMapper, ProductMapper productMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    @Cacheable(cacheNames = CacheConfig.CATEGORIES, key = "'simple'")
    public List<Map<String, Object>> listSimple() {
        return categoryMapper.findAll().stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            return m;
        }).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = CacheConfig.CATEGORIES, key = "'all'")
    public List<Category> listAll() {
        return categoryMapper.findAll();
    }

    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.CATEGORIES, CacheConfig.STATISTICS}, allEntries = true)
    public Category create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName().trim());
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setCreatedAt(LocalDateTime.now());
        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(409, "分类名称已存在");
        }
        return category;
    }

    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.CATEGORIES, CacheConfig.STATISTICS}, allEntries = true)
    public Category update(Long id, CategoryRequest request) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new BusinessException(400, "分类名称不能为空");
            }
            category.setName(name);
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        try {
            categoryMapper.update(category);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(409, "分类名称已存在");
        }
        return category;
    }

    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.CATEGORIES, CacheConfig.STATISTICS}, allEntries = true)
    public void delete(Long id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (productMapper.countByCategory(id) > 0) {
            throw new BusinessException(400, "该分类下有商品，不可删除");
        }
        categoryMapper.delete(id);
    }
}
