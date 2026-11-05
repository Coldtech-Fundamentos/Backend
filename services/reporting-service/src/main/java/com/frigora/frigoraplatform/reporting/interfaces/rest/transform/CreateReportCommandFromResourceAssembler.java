package com.frigora.frigoraplatform.reporting.interfaces.rest.transform;


import com.frigora.frigoraplatform.reporting.domain.model.commands.CreateReportCommand;
import com.frigora.frigoraplatform.reporting.interfaces.rest.resources.CreateReportResource;

public class CreateReportCommandFromResourceAssembler {
    public static CreateReportCommand toCommandFromResource(CreateReportResource resource, Integer tenantId) {
        return new CreateReportCommand(
                tenantId,             
                resource.type(),
                resource.equipmentId(),
                resource.title(),
                resource.summary(),
                resource.content(),
                resource.url()
        );
    }
}