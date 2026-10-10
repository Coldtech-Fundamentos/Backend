package com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.List;

public record InterventionResource(
        long id,
        int serviceRequestId,
        int technicianId,
        String status,
        String summary,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<String> photoUrls) {}
