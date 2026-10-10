package com.frigora.frigoraplatform.monitoring.domain.model.commands;

import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.StatusEquipment;

import java.time.LocalDateTime;

public record CreateEquipmentCommand(
        String model,
        String type,
        String serial,
        StatusEquipment status,
        LocalDateTime installed,
        LocalDateTime lastSeen,
        Float setPoint,
        String name,
        String manufacturer,
        Boolean online,
        Long ownerId
) {}
