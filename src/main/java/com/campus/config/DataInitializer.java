package com.campus.config;

import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import com.campus.category.entity.Category;
import com.campus.category.mapper.CategoryMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, CategoryMapper categoryMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Skip when Flyway seed data (or prior bootstrap) already exists
        if (userMapper.countAll() > 0) {
            return;
        }
        if (userMapper.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin1234"));
            admin.setActive(true);
            admin.setStaff(true);
            admin.setSuperuser(true);
            admin.setCreatedAt(LocalDateTime.now());
            userMapper.insert(admin);
        }
        if (categoryMapper.findAll().isEmpty()) {
            seedCategory("数码电子", 1);
            seedCategory("图书教材", 2);
            seedCategory("生活用品", 3);
            seedCategory("服饰鞋包", 4);
            seedCategory("其它", 99);
        }
    }

    private void seedCategory(String name, int order) {
        Category c = new Category();
        c.setName(name);
        c.setSortOrder(order);
        c.setCreatedAt(LocalDateTime.now());
        categoryMapper.insert(c);
    }
}
