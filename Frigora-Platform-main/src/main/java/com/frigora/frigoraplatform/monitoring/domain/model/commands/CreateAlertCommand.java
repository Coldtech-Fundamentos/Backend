package com.frigora.frigoraplatform.monitoring.domain.model.commands;

import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.EAlertSeverity;
import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.EAlertStatus;

public record CreateAlertCommand(
        Long tenantId,
        Long equipmentId,
        Long siteId,
        String type,
        EAlertSeverity severity,
        String description,
        EAlertStatus status
) {
}
