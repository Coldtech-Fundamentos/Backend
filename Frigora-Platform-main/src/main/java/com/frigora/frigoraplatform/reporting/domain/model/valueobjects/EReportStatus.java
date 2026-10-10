package com.frigora.frigoraplatform.reporting.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EReportStatus {
    @JsonProperty("InProgress")
    IN_PROGRESS,

    @JsonProperty("Completed")
    COMPLETED,

    @JsonProperty("Canceled")
    CANCELED
}