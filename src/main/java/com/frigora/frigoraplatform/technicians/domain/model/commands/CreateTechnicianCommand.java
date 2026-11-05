package com.frigora.frigoraplatform.technicians.domain.model.commands;

public record CreateTechnicianCommand(
        String name,
        String specialty,
        String phone,
        Integer providerId
) {
}