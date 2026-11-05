-- Initial schema for the independent frigora_reporting_db database only.
-- No IF NOT EXISTS: an unexpected existing table must fail rather than be silently adopted.
CREATE TABLE reports (
    id INT NOT NULL AUTO_INCREMENT,
    tenant_id INT NULL,
    equipment_id INT NULL,
    type ENUM('AUDIT', 'INCIDENT', 'INSPECTION', 'MAINTENANCE') NULL,
    title VARCHAR(255) NULL,
    status ENUM('CANCELED', 'COMPLETED', 'IN_PROGRESS') NULL,
    summary VARCHAR(255) NULL,
    content TEXT NULL,
    url VARCHAR(255) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_reports_tenant_equipment (tenant_id, equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
