package com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class RequesterId {

    @Column(name = "requester_id")
    private int value;

    public RequesterId() {
        this.value = 0;
    }

    public RequesterId(int value) {
        this.value = value;
    }
}
