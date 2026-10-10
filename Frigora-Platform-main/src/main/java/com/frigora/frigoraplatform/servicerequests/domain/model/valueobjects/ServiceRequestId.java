package com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class ServiceRequestId {

    @Column(name = "service_request_id")
    private int value;

    public ServiceRequestId() {
        this.value = 0;
    }

    public ServiceRequestId(int value) {
        this.value = value;
    }
}
