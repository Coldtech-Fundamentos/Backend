package com.frigora.frigoraplatform.feedback.application.internal.queryservices;

import com.frigora.frigoraplatform.feedback.domain.model.aggregates.Review;
import com.frigora.frigoraplatform.feedback.domain.model.queries.GetAllReviewsQuery;
import com.frigora.frigoraplatform.feedback.domain.model.queries.GetReviewByIdQuery;
import com.frigora.frigoraplatform.feedback.domain.model.queries.GetReviewsByServiceRequestIdQuery;
import com.frigora.frigoraplatform.feedback.domain.services.ReviewQueryService;
import com.frigora.frigoraplatform.feedback.infrastructure.persistence.jpa.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewQueryServiceImpl implements ReviewQueryService {

    private final ReviewRepository reviewRepository;

    @Override
    public Optional<Review> handle(GetReviewByIdQuery query) {
        return reviewRepository.findById((long) query.id());
    }

    @Override
    public List<Review> handle(GetReviewsByServiceRequestIdQuery query) {
        return reviewRepository.findByServiceRequestIdValue(
                query.serviceRequestId()
        );
    }

    @Override
    public List<Review> handle(GetAllReviewsQuery query) {
        return reviewRepository.findAll();
    }
}
