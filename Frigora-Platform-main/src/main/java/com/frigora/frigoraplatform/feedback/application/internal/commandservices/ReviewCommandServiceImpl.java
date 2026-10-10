package com.frigora.frigoraplatform.feedback.application.internal.commandservices;

import com.frigora.frigoraplatform.feedback.domain.model.aggregates.Review;
import com.frigora.frigoraplatform.feedback.domain.model.commands.CreateReviewCommand;
import com.frigora.frigoraplatform.feedback.domain.services.ReviewCommandService;
import com.frigora.frigoraplatform.feedback.infrastructure.persistence.jpa.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewCommandServiceImpl implements ReviewCommandService {

    private final ReviewRepository reviewRepository;

    @Override
    public Optional<Review> handle(CreateReviewCommand command) {

        var review = new Review(
                command.serviceRequestId(),
                command.technicianId(),
                command.rating(),
                command.comment()
        );

        reviewRepository.save(review);

        return Optional.of(review);
    }
}
