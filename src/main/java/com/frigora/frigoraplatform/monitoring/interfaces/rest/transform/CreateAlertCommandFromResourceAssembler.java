package com.frigora.frigoraplatform.monitoring.interfaces.rest.transform;

import com.frigora.frigoraplatform.monitoring.domain.model.commands.CreateAlertCommand;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.resources.CreateAlertResource;
import org.springframework.stereotype.Component;

@Component
public class CreateAlertCommandFromResourceAssembler {

    public CreateAlertCommand toCommand(CreateAlertResource resource, Long tenantId) {
        return new CreateAlertCommand(
                tenantId,                  
                resource.getEquipmentId(),
                resource.getSiteId(),
                resource.getType(),
                resource.getSeverity(),
                resource.getDescription(),
                resource.getStatus()
        );
    }
}
