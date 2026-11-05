package com.frigora.frigoraplatform.reporting.interfaces.rest.resources;

import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateReportResource(
        @NotNull @Positive Integer equipmentId,
        @NotBlank @Size(max = 255) String title,
        @NotNull EReportStatus status,
        @Size(max = 255) String summary,
        String content,
        @Size(max = 255) String url) {}
