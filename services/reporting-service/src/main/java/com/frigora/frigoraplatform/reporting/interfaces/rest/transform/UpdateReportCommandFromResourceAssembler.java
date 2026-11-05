package com.frigora.frigoraplatform.reporting.interfaces.rest.transform;


import com.frigora.frigoraplatform.reporting.domain.model.commands.UpdateReportCommand;
import com.frigora.frigoraplatform.reporting.interfaces.rest.resources.UpdateReportResource;

public class UpdateReportCommandFromResourceAssembler {
    public static UpdateReportCommand toCommandFromResource(Integer reportId, UpdateReportResource resource, Integer tenantId) {
        return new UpdateReportCommand(
                reportId,
                tenantId,
                resource.equipmentId(),
                resource.title(),
                resource.status(),
                resource.summary(),
                resource.content(),
                resource.url()
        );
    }
}