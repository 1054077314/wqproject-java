package com.campus.user.service;

import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.PageUtils;
import com.campus.config.AppProperties;
import com.campus.product.mapper.ProductMapper;
import com.campus.security.UserPrincipal;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.RegisterRequest;
import com.campus.user.dto.UserView;
import com.campus.user.entity.Token;
import com.campus.user.entity.User;
import com.campus.user.mapper.TokenMapper;
import com.campus.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final TokenMapper tokenMapper;
    private final ProductMapper productMapper;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public AuthService(UserMapper userMapper, TokenMapper tokenMapper, ProductMapper productMapper,
                       PasswordEncoder passwordEncoder, AppProperties appProperties) {
        this.userMapper = userMapper;
        this.tokenMapper = tokenMapper;
        this.productMapper = productMapper;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException(409, "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setStaff(false);
        user.setSuperuser(false);
        user.setCreatedAt(LocalDateTime.now());
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(409, "用户名已存在");
        }
        return Map.of("user_id", user.getId());
    }

    @Transactional
    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword()) || !user.isActive()) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        Token token = new Token();
        token.setKey(UUID.randomUUID().toString().replace("-", ""));
        token.setUserId(user.getId());
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(appProperties.getTokenExpireDays()));
        tokenMapper.insert(token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("is_staff", user.isStaff());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token.getKey());
        data.put("user", userInfo);
        return data;
    }

    @Transactional
    public void logout(UserPrincipal principal) {
        if (principal.getTokenKey() != null) {
            tokenMapper.deleteByKey(principal.getTokenKey());
        }
    }

    public UserView profile(UserPrincipal principal) {
        return toView(principal.getUser());
    }

    public PageResult<UserView> listUsers(HttpServletRequest request) {
        return PageUtils.paginate(request, 10, () ->
                userMapper.findAllOrderByCreatedAtDesc().stream().map(this::toView).collect(Collectors.toList()));
    }

    @Transactional
    public UserView toggleActive(Long id, boolean active, UserPrincipal current) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (user.getId().equals(current.getId())) {
            throw new BusinessException(400, "不可禁用自己");
        }
        userMapper.updateActive(id, active);
        user.setActive(active);
        return toView(user);
    }

    public Map<String, Object> statistics() {
        Map<String, Object> data = new HashMap<>();
        data.put("total_users", userMapper.countAll());
        data.put("total_products", productMapper.countAll());
        data.put("products_by_status", productMapper.countByStatus());
        data.put("today_new_products", productMapper.countToday());
        data.put("pending_products", productMapper.countByStatusValue("pending"));
        return data;
    }

    private UserView toView(User user) {
        return new UserView(user.getId(), user.getUsername(), user.isActive(), user.isStaff(), user.getCreatedAt());
    }
}
