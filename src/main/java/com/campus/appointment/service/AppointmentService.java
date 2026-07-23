package com.campus.appointment.service;

import com.campus.appointment.entity.Appointment;
import com.campus.appointment.mapper.AppointmentMapper;
import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.PageUtils;
import com.campus.config.AppProperties;
import com.campus.product.entity.Product;
import com.campus.product.mapper.ProductMapper;
import com.campus.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final ProductMapper productMapper;
    private final AppProperties appProperties;

    public AppointmentService(AppointmentMapper appointmentMapper, ProductMapper productMapper, AppProperties appProperties) {
        this.appointmentMapper = appointmentMapper;
        this.productMapper = productMapper;
        this.appProperties = appProperties;
    }

    @Transactional
    public Map<String, Object> create(Long productId, UserPrincipal principal) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (product.getSellerId().equals(principal.getId())) {
            throw new BusinessException(400, "不能预约自己的商品");
        }
        if (principal.isStaff()) {
            throw new BusinessException(403, "管理员只读，不能预约");
        }
        if (!"active".equals(product.getStatus())) {
            throw new BusinessException(400, "只能预约已上架商品");
        }
        if (appointmentMapper.exists(principal.getId(), productId)) {
            throw new BusinessException(409, "已预约");
        }
        Appointment appointment = new Appointment();
        appointment.setBuyerId(principal.getId());
        appointment.setProductId(productId);
        appointment.setStatus("pending");
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.insert(appointment);
        return toView(appointmentMapper.findById(appointment.getId()));
    }

    public PageResult<Map<String, Object>> asBuyer(HttpServletRequest request, UserPrincipal principal) {
        return PageUtils.paginate(request, appProperties.getPageSize(), () ->
                appointmentMapper.findByBuyer(principal.getId()).stream().map(this::toView).collect(Collectors.toList()));
    }

    public PageResult<Map<String, Object>> asSeller(HttpServletRequest request, UserPrincipal principal) {
        return PageUtils.paginate(request, appProperties.getPageSize(), () ->
                appointmentMapper.findBySeller(principal.getId()).stream().map(this::toView).collect(Collectors.toList()));
    }

    @Transactional
    public Map<String, Object> updateStatus(Long id, String action, UserPrincipal principal) {
        Appointment appointment = appointmentMapper.findById(id);
        if (appointment == null) {
            throw new BusinessException(404, "预约不存在");
        }
        Product product = productMapper.findById(appointment.getProductId());
        if (product == null || !product.getSellerId().equals(principal.getId())) {
            throw new BusinessException(403, "无权操作");
        }
        if (!"pending".equals(appointment.getStatus())) {
            throw new BusinessException(400, "该预约已被处理");
        }
        if (!"confirm".equals(action) && !"reject".equals(action)) {
            throw new BusinessException(400, "无效操作");
        }
        appointment.setStatus("confirm".equals(action) ? "confirmed" : "rejected");
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateStatus(appointment);
        return toView(appointmentMapper.findById(id));
    }

    private Map<String, Object> toView(Appointment a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("product_id", a.getProductId());
        m.put("product_title", a.getProductTitle());
        m.put("product_price", a.getProductPrice());
        m.put("buyer_username", a.getBuyerUsername());
        m.put("status", a.getStatus());
        m.put("created_at", a.getCreatedAt());
        return m;
    }
}
