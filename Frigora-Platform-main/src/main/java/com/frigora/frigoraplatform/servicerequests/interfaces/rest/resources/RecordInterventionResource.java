package com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.List;

public record RecordInterventionResource(
        int serviceRequestId,
        int technicianId,
        String summary,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<String> photoUrls) {}
