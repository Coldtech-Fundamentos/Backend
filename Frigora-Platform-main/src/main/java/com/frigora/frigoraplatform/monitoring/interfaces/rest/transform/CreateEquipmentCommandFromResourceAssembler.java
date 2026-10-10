package com.frigora.frigoraplatform.monitoring.interfaces.rest.transform;

import com.frigora.frigoraplatform.monitoring.domain.model.commands.CreateEquipmentCommand;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.resources.CreateEquipmentResource;
import org.springframework.stereotype.Component;

@Component
public class CreateEquipmentCommandFromResourceAssembler {

    public CreateEquipmentCommand toCommand(CreateEquipmentResource resource, Long ownerId) {
        return new CreateEquipmentCommand(
                resource.getModel(),
                resource.getType(),
                resource.getSerial(),
                resource.getStatus(),
                resource.getInstalled(),
                resource.getLastSeen(),
                resource.getSetPoint(),
                resource.getName(),
                resource.getManufacturer(),
                resource.getOnline(),
                ownerId
        );
    }
}
