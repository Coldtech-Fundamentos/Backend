package com.frigora.frigoraplatform.feedback.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public class ReviewRating {

    private int value;

    protected ReviewRating() {
        this(0);
    }

    public ReviewRating(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
