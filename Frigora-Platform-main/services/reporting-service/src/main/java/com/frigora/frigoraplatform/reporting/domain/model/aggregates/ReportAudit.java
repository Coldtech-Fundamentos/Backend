package com.frigora.frigoraplatform.reporting.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Embeddable
@Getter
@Setter
public class ReportAudit {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedDate;
}