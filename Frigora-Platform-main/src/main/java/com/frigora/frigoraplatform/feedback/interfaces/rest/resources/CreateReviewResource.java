package com.frigora.frigoraplatform.feedback.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateReviewResource(

        @Schema(description = "Identificador único de la solicitud de servicio asociada", example = "101")
        int serviceRequestId,

        @Schema(description = "Identificador del técnico que ejecutó el servicio", example = "15")
        int technicianId,

        @Schema(description = "Calificación asignada al servicio, típicamente de 1 a 5", example = "5")
        int rating,

        @Schema(description = "Comentarios adicionales sobre el trabajo de refrigeración realizado", example = "El equipo quedó enfriando perfectamente, excelente servicio.")
        String comment
) {
}