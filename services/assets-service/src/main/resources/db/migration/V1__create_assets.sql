-- Initial schema for the exclusive frigora_assets_db database.
-- No IF NOT EXISTS: unexpected existing tables must fail rather than be adopted.
CREATE TABLE sites (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_sites_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE equipment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    equipment_id VARCHAR(36) NOT NULL,
    model VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    serial VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'OFF', 'MAINTENANCE') NOT NULL,
    installed DATETIME(6) NULL,
    last_seen DATETIME(6) NULL,
    set_point FLOAT NULL,
    name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    online BOOLEAN NOT NULL,
    created DATETIME(6) NOT NULL,
    updated DATETIME(6) NOT NULL,
    owner_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_equipment_equipment_id (equipment_id),
    UNIQUE KEY uk_equipment_serial (serial),
    INDEX idx_equipment_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
