package com.frigora.frigoraplatform.reporting.interfaces.rest.resources;

import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportType;

public record CreateReportResource(
        EReportType type,
        Integer equipmentId,
        String title,
        String summary,
        String content,
        String url
) {}