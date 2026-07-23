package com.campus.audit.service;

import com.campus.audit.entity.AuditLog;
import com.campus.audit.mapper.AuditLogMapper;
import com.campus.audit.vo.AuditLogVo;
import com.campus.common.PageResult;
import com.campus.common.PageUtils;
import com.campus.config.AppProperties;
import com.campus.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogMapper auditLogMapper;
    private final AppProperties appProperties;

    public AuditService(AuditLogMapper auditLogMapper, AppProperties appProperties) {
        this.auditLogMapper = auditLogMapper;
        this.appProperties = appProperties;
    }

    public void record(UserPrincipal actor, String action, String resourceType, Long resourceId, String detail) {
        try {
            AuditLog row = new AuditLog();
            if (actor != null) {
                row.setActorId(actor.getId());
                row.setActorUsername(actor.getUsername());
            }
            row.setAction(action);
            row.setResourceType(resourceType);
            row.setResourceId(resourceId);
            if (detail != null && detail.length() > 500) {
                detail = detail.substring(0, 500);
            }
            row.setDetail(detail);
            row.setCreatedAt(LocalDateTime.now());
            auditLogMapper.insert(row);
        } catch (Exception e) {
            // Audit must not break business flow
            log.warn("Failed to write audit log action={} resource={}:{}", action, resourceType, resourceId, e);
        }
    }

    public PageResult<AuditLogVo> list(HttpServletRequest request) {
        return PageUtils.paginate(request, appProperties.getPageSize(), () ->
                auditLogMapper.findRecent().stream().map(this::toVo).collect(Collectors.toList()));
    }

    private AuditLogVo toVo(AuditLog a) {
        AuditLogVo vo = new AuditLogVo();
        vo.setId(a.getId());
        vo.setActorId(a.getActorId());
        vo.setActorUsername(a.getActorUsername());
        vo.setAction(a.getAction());
        vo.setResourceType(a.getResourceType());
        vo.setResourceId(a.getResourceId());
        vo.setDetail(a.getDetail());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }
}
