package com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Embeddable
@Getter
public class InterventionStatus {

    public enum EInterventionStatus {
        PENDING,
        COMPLETED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EInterventionStatus status = EInterventionStatus.PENDING;

    public InterventionStatus() {
        this.status = EInterventionStatus.PENDING;
    }

    public InterventionStatus(EInterventionStatus status) {
        this.status = status;
    }
}