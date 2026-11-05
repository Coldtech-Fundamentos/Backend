package com.frigora.frigoraplatform.reporting.interfaces.rest.resources;


import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportStatus;
import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportType;

public record ReportResource(
        Integer id,
        Integer tenantId,
        EReportType type,
        Integer equipmentId,
        String title,
        EReportStatus status,
        String summary,
        String content,
        String url
) {}