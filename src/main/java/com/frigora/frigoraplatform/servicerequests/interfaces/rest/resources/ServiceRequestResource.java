package com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources;

import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestPriority;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestStatus;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestType;

import java.time.LocalDateTime;

public record ServiceRequestResource(
        long id,
        int requesterId,
        int siteId,
        int equipmentId,
        int assignedTo,
        String origin,
        ServiceRequestType.EServiceRequestType type,
        ServiceRequestPriority.EServiceRequestPriority priority,
        String description,
        ServiceRequestStatus.EServiceRequestStatus status,
        LocalDateTime completedAt,
        LocalDateTime canceledAt,
        Integer technicianId) {}
