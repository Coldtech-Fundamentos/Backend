package com.frigora.frigoraplatform.reporting.interfaces.rest.resources;


import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportStatus;

public record UpdateReportResource(
        Integer equipmentId,
        String title,
        EReportStatus status,
        String summary,
        String content,
        String url
) {}