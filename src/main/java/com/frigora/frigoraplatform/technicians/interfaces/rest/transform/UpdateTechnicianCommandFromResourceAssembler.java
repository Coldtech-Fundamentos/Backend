package com.frigora.frigoraplatform.technicians.interfaces.rest.transform;

import com.frigora.frigoraplatform.technicians.domain.model.commands.UpdateTechnicianCommand;
import com.frigora.frigoraplatform.technicians.interfaces.rest.resources.UpdateTechnicianResource;

public class UpdateTechnicianCommandFromResourceAssembler {
    public static UpdateTechnicianCommand toCommandFromResource(Integer id, UpdateTechnicianResource resource) {
        return new UpdateTechnicianCommand(
                id,
                resource.name(),
                resource.specialty(),
                resource.phone()
        );
    }
}