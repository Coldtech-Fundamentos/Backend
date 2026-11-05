package com.frigora.frigoraplatform.monitoring.domain.model.aggregates;

import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.EAlertSeverity;
import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.EAlertStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Long equipmentId;

    @Column(nullable = false)
    private Long siteId;

    @Column(nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EAlertSeverity severity;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EAlertStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void acknowledge() {
        this.status = EAlertStatus.ACKNOWLEDGED;
    }

    public void resolve() {
        this.status = EAlertStatus.RESOLVED;
    }

    public void close() {
        this.status = EAlertStatus.CLOSED;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}