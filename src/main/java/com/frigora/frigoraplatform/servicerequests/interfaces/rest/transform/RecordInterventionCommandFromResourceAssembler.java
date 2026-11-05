package com.frigora.frigoraplatform.servicerequests.interfaces.rest.transform;

import com.frigora.frigoraplatform.servicerequests.domain.model.commands.RecordInterventionCommand;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources.RecordInterventionResource;

public class RecordInterventionCommandFromResourceAssembler {

    public static RecordInterventionCommand toCommandFromResource(RecordInterventionResource resource) {
        return new RecordInterventionCommand(
                resource.serviceRequestId(),
                resource.technicianId(),
                resource.summary(),
                resource.startTime(),
                resource.endTime(),
                resource.photoUrls()
        );
    }
}
