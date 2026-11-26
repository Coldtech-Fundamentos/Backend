package com.frigora.frigoraplatform.assets.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EquipmentStatus {
    ACTIVE,
    OFF,
    MAINTENANCE;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static EquipmentStatus fromJson(String value) {
        return EquipmentStatus.valueOf(value.toUpperCase());
    }
}
