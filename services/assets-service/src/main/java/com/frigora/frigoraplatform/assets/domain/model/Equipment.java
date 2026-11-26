package com.frigora.frigoraplatform.assets.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "equipment_id", nullable = false, unique = true, updatable = false, length = 36)
    private UUID equipmentId;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, unique = true)
    private String serial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status;

    private LocalDateTime installed;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "set_point")
    private Float setPoint;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private Boolean online;

    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @Column(nullable = false)
    private LocalDateTime updated;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private Long ownerId;

    protected Equipment() {}

    public Equipment(
            String model,
            String type,
            String serial,
            EquipmentStatus status,
            LocalDateTime installed,
            LocalDateTime lastSeen,
            Float setPoint,
            String name,
            String manufacturer,
            Boolean online,
            Long ownerId) {
        this.model = model;
        this.type = type;
        this.serial = serial;
        this.status = status;
        this.installed = installed;
        this.lastSeen = lastSeen;
        this.setPoint = setPoint;
        this.name = name;
        this.manufacturer = manufacturer;
        this.online = online;
        this.ownerId = ownerId;
    }

    @PrePersist
    protected void onCreate() {
        if (equipmentId == null) {
            equipmentId = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        created = now;
        updated = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getEquipmentId() {
        return equipmentId;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
    }

    public String getSerial() {
        return serial;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getInstalled() {
        return installed;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public Float getSetPoint() {
        return setPoint;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public Boolean getOnline() {
        return online;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public Long getOwnerId() {
        return ownerId;
    }
}
