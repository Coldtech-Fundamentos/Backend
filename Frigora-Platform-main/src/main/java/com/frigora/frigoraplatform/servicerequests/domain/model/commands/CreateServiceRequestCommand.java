package com.frigora.frigoraplatform.servicerequests.domain.model.commands;

import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestPriority;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestType;

public record CreateServiceRequestCommand(
        int requesterId,
        int siteId,
        int equipmentId,
        int assignedTo,
        String origin,
        ServiceRequestType.EServiceRequestType type,
        ServiceRequestPriority.EServiceRequestPriority priority,
        String description) {}