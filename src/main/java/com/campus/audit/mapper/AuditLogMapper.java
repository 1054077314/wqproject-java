package com.campus.audit.mapper;

import com.campus.audit.entity.AuditLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuditLogMapper {

    @Insert("""
            INSERT INTO audit_logs (actor_id, actor_username, action, resource_type, resource_id, detail, created_at)
            VALUES (#{actorId}, #{actorUsername}, #{action}, #{resourceType}, #{resourceId}, #{detail}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLog log);

    @Select("""
            SELECT id, actor_id, actor_username, action, resource_type, resource_id, detail, created_at
            FROM audit_logs
            ORDER BY created_at DESC, id DESC
            """)
    List<AuditLog> findRecent();
}
