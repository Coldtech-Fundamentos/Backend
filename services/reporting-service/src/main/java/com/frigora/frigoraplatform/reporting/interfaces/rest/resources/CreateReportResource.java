package com.frigora.frigoraplatform.reporting.interfaces.rest.resources;

import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateReportResource(
        @NotNull EReportType type,
        @NotNull @Positive Integer equipmentId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 255) String summary,
        String content,
        @Size(max = 255) String url) {}
