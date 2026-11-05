package com.frigora.frigoraplatform.reporting.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EReportType {
    @JsonProperty("Inspection")
    INSPECTION,

    @JsonProperty("Maintenance")
    MAINTENANCE,

    @JsonProperty("Incident")
    INCIDENT,

    @JsonProperty("Audit")
    AUDIT
}