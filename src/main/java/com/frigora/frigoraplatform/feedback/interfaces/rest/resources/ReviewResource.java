package com.frigora.frigoraplatform.feedback.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReviewResource(
        @Schema(description = "Automatically assigned review identifier", example = "1") int id,
        @Schema(description = "Associated service request identifier", example = "101")
                int serviceRequestId,
        @Schema(
                        description = "Identifier of the technician who performed the service",
                        example = "15")
                int technicianId,
        @Schema(description = "Final service rating, typically from 1 to 5", example = "5")
                int rating,
        @Schema(
                        description = "Detailed feedback about the refrigeration service",
                        example = "The equipment is cooling correctly. Excellent service.")
                String comment) {}
