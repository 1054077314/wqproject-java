package com.campus.appointment.service;

import com.campus.appointment.entity.Appointment;
import com.campus.appointment.mapper.AppointmentMapper;
import com.campus.appointment.vo.AppointmentVo;
import com.campus.audit.service.AuditService;
import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.PageUtils;
import com.campus.config.AppProperties;
import com.campus.config.CacheConfig;
import com.campus.lock.DistributedLock;
import com.campus.product.entity.Product;
import com.campus.product.mapper.ProductMapper;
import com.campus.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final ProductMapper productMapper;
    private final AppProperties appProperties;
    private final DistributedLock distributedLock;
    private final TransactionTemplate transactionTemplate;
    private final AuditService auditService;

    public AppointmentService(AppointmentMapper appointmentMapper, ProductMapper productMapper,
                              AppProperties appProperties, DistributedLock distributedLock,
                              TransactionTemplate transactionTemplate, AuditService auditService) {
        this.appointmentMapper = appointmentMapper;
        this.productMapper = productMapper;
        this.appProperties = appProperties;
        this.distributedLock = distributedLock;
        this.transactionTemplate = transactionTemplate;
        this.auditService = auditService;
    }

    public AppointmentVo create(Long productId, UserPrincipal principal) {
        return transactionTemplate.execute(status -> createInTx(productId, principal));
    }

    private AppointmentVo createInTx(Long productId, UserPrincipal principal) {
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
            String st = existing.getStatus();
            if ("pending".equals(st) || "confirmed".equals(st)) {
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

    @CacheEvict(cacheNames = CacheConfig.STATISTICS, allEntries = true)
    public AppointmentVo updateStatus(Long id, String action, UserPrincipal principal) {
        Appointment appointment = appointmentMapper.findById(id);
        if (appointment == null) {
            throw new BusinessException(404, "预约不存在");
        }

        if ("cancel".equals(action)) {
            AppointmentVo vo = transactionTemplate.execute(status -> cancelByBuyer(appointment, principal));
            auditService.record(principal, "appointment.cancel", "appointment", id, null);
            return vo;
        }

        Product product = productMapper.findById(appointment.getProductId());
        if (product == null || !product.getSellerId().equals(principal.getId())) {
            throw new BusinessException(403, "无权操作");
        }
        if (!"confirm".equals(action) && !"reject".equals(action)) {
            throw new BusinessException(400, "无效操作");
        }

        // Multi-instance: lock product deal key, then CAS inside one TX.
        if ("confirm".equals(action)) {
            AppointmentVo vo = distributedLock.execute(
                    "product:deal:" + product.getId(),
                    Duration.ofSeconds(15),
                    () -> transactionTemplate.execute(status -> confirmOrReject(id, action, principal)));
            auditService.record(principal, "appointment.confirm", "appointment", id,
                    "product_id=" + product.getId() + ",sold=true");
            return vo;
        }

        AppointmentVo vo = transactionTemplate.execute(status -> confirmOrReject(id, action, principal));
        auditService.record(principal, "appointment.reject", "appointment", id,
                "product_id=" + product.getId());
        return vo;
    }

    private AppointmentVo confirmOrReject(Long id, String action, UserPrincipal principal) {
        LocalDateTime now = LocalDateTime.now();
        String newStatus = "confirm".equals(action) ? "confirmed" : "rejected";
        if (appointmentMapper.casStatus(id, "pending", newStatus, now) == 0) {
            throw new BusinessException(409, "该预约已被处理");
        }
        if ("confirm".equals(action)) {
            Appointment latest = appointmentMapper.findById(id);
            if (productMapper.casStatus(latest.getProductId(), "active", "sold", "") == 0) {
                throw new BusinessException(409, "商品状态已变更，无法确认成交");
            }
            appointmentMapper.rejectOtherPending(latest.getProductId(), id, now);
        }
        return toVo(appointmentMapper.findById(id));
    }

    private AppointmentVo cancelByBuyer(Appointment appointment, UserPrincipal principal) {
        if (!appointment.getBuyerId().equals(principal.getId())) {
            throw new BusinessException(403, "只能取消自己的预约");
        }
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
