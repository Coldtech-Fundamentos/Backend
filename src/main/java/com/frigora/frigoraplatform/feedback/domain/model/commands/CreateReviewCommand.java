package com.frigora.frigoraplatform.feedback.domain.model.commands;

public record CreateReviewCommand(Integer serviceRequestId, Integer technicianId, Integer rating, String comment) {}
