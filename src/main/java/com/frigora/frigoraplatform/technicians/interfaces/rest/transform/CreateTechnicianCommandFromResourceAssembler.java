package com.frigora.frigoraplatform.technicians.interfaces.rest.transform;

import com.frigora.frigoraplatform.technicians.domain.model.commands.CreateTechnicianCommand;
import com.frigora.frigoraplatform.technicians.interfaces.rest.resources.CreateTechnicianResource;

public class CreateTechnicianCommandFromResourceAssembler {
    public static CreateTechnicianCommand toCommandFromResource(CreateTechnicianResource resource) {
        return new CreateTechnicianCommand(
                resource.name(),
                resource.specialty(),
                resource.phone(),
                resource.providerId()
        );
    }
}