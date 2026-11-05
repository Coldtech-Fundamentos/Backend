package com.frigora.frigoraplatform.reporting.domain.model.commands;


import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportStatus;

public record UpdateReportCommand(
        Integer id,
        Integer tenantId,
        Integer equipmentId,
        String title,
        EReportStatus status,
        String summary,
        String content,
        String url
) {}