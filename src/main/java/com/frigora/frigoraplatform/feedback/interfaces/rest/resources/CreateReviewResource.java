package com.frigora.frigoraplatform.feedback.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateReviewResource(
        @Schema(description = "Associated service request identifier", example = "101")
                int serviceRequestId,
        @Schema(
                        description = "Identifier of the technician who performed the service",
                        example = "15")
                int technicianId,
        @Schema(description = "Service rating, typically from 1 to 5", example = "5") int rating,
        @Schema(
                        description = "Additional feedback about the refrigeration service",
                        example = "The equipment is cooling correctly. Excellent service.")
                String comment) {}
