package com.frigora.frigoraplatform.feedback.interfaces.rest.transform;

import com.frigora.frigoraplatform.feedback.domain.model.commands.CreateReviewCommand;
import com.frigora.frigoraplatform.feedback.interfaces.rest.resources.CreateReviewResource;

public class CreateReviewCommandFromResourceAssembler {

    public static CreateReviewCommand toCommandFromResource(
            CreateReviewResource resource) {

        return new CreateReviewCommand(
                resource.serviceRequestId(),
                resource.technicianId(),
                resource.rating(),
                resource.comment()
        );
    }
}
