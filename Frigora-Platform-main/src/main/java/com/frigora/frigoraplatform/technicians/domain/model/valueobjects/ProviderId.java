package com.frigora.frigoraplatform.technicians.domain.model.valueobjects;

public record ProviderId(Integer value) {
    public ProviderId() {
        this(0);
    }
}