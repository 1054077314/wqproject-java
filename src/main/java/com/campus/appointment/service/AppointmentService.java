package com.campus.appointment.service;

import com.campus.appointment.entity.Appointment;
import com.campus.appointment.mapper.AppointmentMapper;
import com.campus.appointment.vo.AppointmentVo;
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
    public AppointmentVo create(Long productId, UserPrincipal principal) {
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

        Appointment existing = appointmentMapper.findByBuyerAndProduct(principal.getId(), productId);
        if (existing != null) {
            String status = existing.getStatus();
            if ("pending".equals(status) || "confirmed".equals(status)) {
                throw new BusinessException(409, "已预约");
            }
            LocalDateTime now = LocalDateTime.now();
            if (appointmentMapper.reopenIfClosed(existing.getId(), now) == 0) {
                throw new BusinessException(409, "已预约");
            }
            return toVo(appointmentMapper.findById(existing.getId()));
        }

        Appointment appointment = new Appointment();
        appointment.setBuyerId(principal.getId());
        appointment.setProductId(productId);
        appointment.setStatus("pending");
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        try {
            appointmentMapper.insert(appointment);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new BusinessException(409, "已预约");
        }
        return toVo(appointmentMapper.findById(appointment.getId()));
    }

    public PageResult<AppointmentVo> asBuyer(HttpServletRequest request, UserPrincipal principal) {
        return PageUtils.paginate(request, appProperties.getPageSize(), () ->
                appointmentMapper.findByBuyer(principal.getId()).stream().map(this::toVo).collect(Collectors.toList()));
    }

    public PageResult<AppointmentVo> asSeller(HttpServletRequest request, UserPrincipal principal) {
        return PageUtils.paginate(request, appProperties.getPageSize(), () ->
                appointmentMapper.findBySeller(principal.getId()).stream().map(this::toVo).collect(Collectors.toList()));
    }

    @Transactional
    public AppointmentVo updateStatus(Long id, String action, UserPrincipal principal) {
        Appointment appointment = appointmentMapper.findById(id);
        if (appointment == null) {
            throw new BusinessException(404, "预约不存在");
        }

        if ("cancel".equals(action)) {
            return cancelByBuyer(appointment, principal);
        }

        Product product = productMapper.findById(appointment.getProductId());
        if (product == null || !product.getSellerId().equals(principal.getId())) {
            throw new BusinessException(403, "无权操作");
        }
        if (!"confirm".equals(action) && !"reject".equals(action)) {
            throw new BusinessException(400, "无效操作");
        }

        LocalDateTime now = LocalDateTime.now();
        String newStatus = "confirm".equals(action) ? "confirmed" : "rejected";
        if (appointmentMapper.casStatus(id, "pending", newStatus, now) == 0) {
            throw new BusinessException(409, "该预约已被处理");
        }

        if ("confirm".equals(action)) {
            // Confirm = deal locked: product active→sold, competing pending→rejected (same TX).
            if (productMapper.casStatus(product.getId(), "active", "sold", "") == 0) {
                throw new BusinessException(409, "商品状态已变更，无法确认成交");
            }
            appointmentMapper.rejectOtherPending(product.getId(), id, now);
        }

        return toVo(appointmentMapper.findById(id));
    }

    private AppointmentVo cancelByBuyer(Appointment appointment, UserPrincipal principal) {
        if (!appointment.getBuyerId().equals(principal.getId())) {
            throw new BusinessException(403, "只能取消自己的预约");
        }
        // Confirmed means deal locked — cancel only allowed while pending.
        if (appointmentMapper.casStatus(appointment.getId(), "pending", "cancelled", LocalDateTime.now()) == 0) {
            throw new BusinessException(400, "当前状态不可取消");
        }
        return toVo(appointmentMapper.findById(appointment.getId()));
    }

    private AppointmentVo toVo(Appointment a) {
        AppointmentVo vo = new AppointmentVo();
        vo.setId(a.getId());
        vo.setProductId(a.getProductId());
        vo.setProductTitle(a.getProductTitle());
        vo.setProductPrice(a.getProductPrice());
        vo.setBuyerUsername(a.getBuyerUsername());
        vo.setStatus(a.getStatus());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }
}
