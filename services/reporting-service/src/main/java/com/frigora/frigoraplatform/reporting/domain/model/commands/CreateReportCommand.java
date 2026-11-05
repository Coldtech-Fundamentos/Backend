package com.frigora.frigoraplatform.reporting.domain.model.commands;


import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportType;

public record CreateReportCommand(
        Integer tenantId,
        EReportType type,
        Integer equipmentId,
        String title,
        String summary,
        String content,
        String url
) {}