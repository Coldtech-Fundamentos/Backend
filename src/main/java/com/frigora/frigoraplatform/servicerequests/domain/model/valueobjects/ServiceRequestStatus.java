package com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Embeddable
@Getter
public class ServiceRequestStatus {

    public enum EServiceRequestStatus {
        PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELED, REJECTED;

        @JsonValue
        public String toJson() {
            return switch (this) {
                case IN_PROGRESS -> "inProgress";
                default -> this.name().toLowerCase();
            };
        }

        @JsonCreator
        public static EServiceRequestStatus fromJson(String value) {
            return switch (value) {
                case "inProgress", "in_progress", "IN_PROGRESS" -> IN_PROGRESS;
                default -> valueOf(value.toUpperCase());
            };
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EServiceRequestStatus status = EServiceRequestStatus.PENDING;

    public ServiceRequestStatus() {
        this.status = EServiceRequestStatus.PENDING;
    }

    public ServiceRequestStatus(EServiceRequestStatus status) {
        this.status = status;
    }
}
