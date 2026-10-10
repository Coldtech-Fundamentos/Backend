package com.frigora.frigoraplatform.servicerequests.domain.model.commands;

import java.time.LocalDateTime;
import java.util.List;

public record RecordInterventionCommand(
        int serviceRequestId,
        int technicianId,
        String summary,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<String> photoUrls) {}
