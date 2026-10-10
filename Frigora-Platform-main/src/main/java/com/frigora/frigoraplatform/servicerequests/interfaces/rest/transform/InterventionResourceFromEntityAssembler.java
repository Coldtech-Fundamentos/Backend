package com.frigora.frigoraplatform.servicerequests.interfaces.rest.transform;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.Intervention;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources.InterventionResource;

public class InterventionResourceFromEntityAssembler {

    public static InterventionResource toResourceFromEntity(Intervention entity) {
        return new InterventionResource(
                entity.getId(),
                entity.getServiceRequestId().getValue(),
                entity.getTechnicianId().getValue(),
                entity.getStatus().name(),
                entity.getSummary(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getPhotoUrls()
        );
    }
}
