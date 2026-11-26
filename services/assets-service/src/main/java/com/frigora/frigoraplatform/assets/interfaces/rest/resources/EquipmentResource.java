package com.frigora.frigoraplatform.assets.interfaces.rest.resources;

import com.frigora.frigoraplatform.assets.domain.model.EquipmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EquipmentResource(
        Long id,
        UUID equipmentId,
        String model,
        String type,
        String serial,
        EquipmentStatus status,
        LocalDateTime installed,
        LocalDateTime lastSeen,
        Float setPoint,
        String name,
        String manufacturer,
        Boolean online,
        LocalDateTime created,
        LocalDateTime updated,
        Long ownerId) {}
