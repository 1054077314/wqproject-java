-- Audit trail for sensitive operations (review, sell, disable user, etc.)

CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id        BIGINT       NULL,
    actor_username  VARCHAR(64)  NULL,
    action          VARCHAR(64)  NOT NULL,
    resource_type   VARCHAR(32)  NOT NULL,
    resource_id     BIGINT       NULL,
    detail          VARCHAR(512) NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_actor_id ON audit_logs (actor_id);
CREATE INDEX idx_audit_logs_resource ON audit_logs (resource_type, resource_id);
