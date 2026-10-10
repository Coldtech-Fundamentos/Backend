package com.frigora.frigoraplatform.technicians.interfaces.rest.transform;

import com.frigora.frigoraplatform.technicians.domain.model.aggregates.Technician;
import com.frigora.frigoraplatform.technicians.interfaces.rest.resources.TechnicianResource;

public class TechnicianResourceFromEntityAssembler {
    public static TechnicianResource toResourceFromEntity(Technician entity) {
        return new TechnicianResource(
                entity.getId(),
                entity.getName(),
                entity.getSpecialty(),
                entity.getPhone(),
                entity.getProviderId()
        );
    }
}