package com.frigora.frigoraplatform.monitoring.domain.model.aggregates;

import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.StatusEquipment;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID equipmentId;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, unique = true)
    private String serial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEquipment status;

    private LocalDateTime installed;
    private LocalDateTime lastSeen;
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

    @Column(nullable = false)
    private Long ownerId; 

    @PrePersist
    protected void onCreate() {
        if (this.equipmentId == null) {
            this.equipmentId = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        this.created = now;
        this.updated = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated = LocalDateTime.now();
    }
}