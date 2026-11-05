package com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class EquipmentId {

    @Column(name = "equipment_id")
    private int value;

    public EquipmentId() {
        this.value = 0;
    }

    public EquipmentId(int value) {
        this.value = value;
    }
}