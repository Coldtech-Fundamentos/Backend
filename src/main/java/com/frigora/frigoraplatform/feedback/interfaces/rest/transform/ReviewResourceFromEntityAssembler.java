package com.frigora.frigoraplatform.feedback.interfaces.rest.transform;

import com.frigora.frigoraplatform.feedback.domain.model.aggregates.Review;
import com.frigora.frigoraplatform.feedback.interfaces.rest.resources.ReviewResource;

public class ReviewResourceFromEntityAssembler {

    public static ReviewResource toResourceFromEntity(Review entity) {
        return new ReviewResource(
                entity.getId(),
                entity.getServiceRequestId().getValue(),
                entity.getTechnicianId().getValue(),
                entity.getRating().getValue(),
                entity.getComment()
        );
    }
}
