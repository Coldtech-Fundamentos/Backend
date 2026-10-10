package com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class TechnicianId {

    @Column(name = "technician_id")
    private int value;

    public TechnicianId() {
        this.value = 0;
    }

    public TechnicianId(int value) {
        this.value = value;
    }
}
