package com.frigora.frigoraplatform.assets.interfaces.rest.resources;

import com.frigora.frigoraplatform.assets.domain.model.EquipmentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateEquipmentResource(
        @NotBlank String model,
        @NotBlank String type,
        @NotBlank String serial,
        @NotNull EquipmentStatus status,
        LocalDateTime installed,
        LocalDateTime lastSeen,
        Float setPoint,
        @NotBlank String name,
        @NotBlank String manufacturer,
        @NotNull Boolean online) {}
