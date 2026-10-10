package com.frigora.frigoraplatform.monitoring.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusEquipment {

    ACTIVE,
    OFF,
    MAINTENANCE;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static StatusEquipment fromJson(String value) {
        return StatusEquipment.valueOf(value.toUpperCase());
    }
}