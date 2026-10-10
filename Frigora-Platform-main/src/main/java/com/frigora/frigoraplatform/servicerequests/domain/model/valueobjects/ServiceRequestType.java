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
public class ServiceRequestType {

    public enum EServiceRequestType {
        CORRECTIVE, PREVENTIVE;

        @JsonValue
        public String toJson() {
            return this.name().toLowerCase();
        }

        @JsonCreator
        public static EServiceRequestType fromJson(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EServiceRequestType type = EServiceRequestType.CORRECTIVE;

    public ServiceRequestType() {
        this.type = EServiceRequestType.CORRECTIVE;
    }

    public ServiceRequestType(EServiceRequestType type) {
        this.type = type;
    }
}
