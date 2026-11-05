package com.frigora.frigoraplatform.feedback.domain.services;

import com.frigora.frigoraplatform.feedback.domain.model.aggregates.Review;
import com.frigora.frigoraplatform.feedback.domain.model.commands.CreateReviewCommand;

import java.util.Optional;

public interface ReviewCommandService {

    Optional<Review> handle(CreateReviewCommand command);
}