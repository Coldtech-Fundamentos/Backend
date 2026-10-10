package com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class SiteId {

    @Column(name = "site_id")
    private int value;

    public SiteId() {
        this.value = 0;
    }

    public SiteId(int value) {
        this.value = value;
    }
}
