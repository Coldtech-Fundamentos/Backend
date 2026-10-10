package com.frigora.frigoraplatform.monitoring.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EAlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED,
    CLOSED;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static EAlertStatus fromJson(String value) {
        return EAlertStatus.valueOf(value.toUpperCase());
    }
}