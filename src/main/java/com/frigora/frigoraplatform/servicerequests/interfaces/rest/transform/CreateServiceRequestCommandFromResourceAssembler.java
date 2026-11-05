package com.frigora.frigoraplatform.servicerequests.interfaces.rest.transform;

import com.frigora.frigoraplatform.servicerequests.domain.model.commands.CreateServiceRequestCommand;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestPriority;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestType;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources.CreateServiceRequestResource;

public class CreateServiceRequestCommandFromResourceAssembler {

    public static CreateServiceRequestCommand toCommandFromResource(CreateServiceRequestResource resource) {
        return new CreateServiceRequestCommand(
                resource.requesterId(),
                resource.siteId(),
                resource.equipmentId(),
                resource.assignedTo(),
                resource.origin(),
                ServiceRequestType.EServiceRequestType.valueOf(resource.type().toUpperCase()),
                ServiceRequestPriority.EServiceRequestPriority.valueOf(resource.priority().toUpperCase()),
                resource.description()
        );
    }
}