package com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class AssignedTo {

    @Column(name = "assigned_to")
    private int value;

    public AssignedTo() {
        this.value = 0;
    }

    public AssignedTo(int value) {
        this.value = value;
    }
}