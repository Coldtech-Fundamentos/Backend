package com.frigora.frigoraplatform.servicerequests.interfaces.rest.transform;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.ServiceRequest;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources.ServiceRequestResource;

public class ServiceRequestResourceFromEntityAssembler {

    public static ServiceRequestResource toResourceFromEntity(ServiceRequest entity) {
        return new ServiceRequestResource(
                entity.getId(),
                entity.getRequesterId().getValue(),
                entity.getSiteId().getValue(),
                entity.getEquipmentId().getValue(),
                entity.getAssignedTo().getValue(),
                entity.getOrigin(),
                entity.getType().getType(),
                entity.getPriority().getPriority(),
                entity.getDescription(),
                entity.getStatus().getStatus(),
                entity.getCompletedAt(),
                entity.getCanceledAt(),
                entity.getTechnicianId() != null ? entity.getTechnicianId().getValue() : null
        );
    }
}
