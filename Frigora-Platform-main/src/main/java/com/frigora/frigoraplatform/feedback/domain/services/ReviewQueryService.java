package com.frigora.frigoraplatform.feedback.domain.services;

import com.frigora.frigoraplatform.feedback.domain.model.aggregates.Review;
import com.frigora.frigoraplatform.feedback.domain.model.queries.GetAllReviewsQuery;
import com.frigora.frigoraplatform.feedback.domain.model.queries.GetReviewByIdQuery;
import com.frigora.frigoraplatform.feedback.domain.model.queries.GetReviewsByServiceRequestIdQuery;

import java.util.List;
import java.util.Optional;

public interface ReviewQueryService {

    Optional<Review> handle(GetReviewByIdQuery query);

    List<Review> handle(GetReviewsByServiceRequestIdQuery query);

    List<Review> handle(GetAllReviewsQuery query);
}
