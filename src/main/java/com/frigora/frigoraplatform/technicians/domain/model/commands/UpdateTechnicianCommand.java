package com.frigora.frigoraplatform.technicians.domain.model.commands;

public record UpdateTechnicianCommand(Integer id, String name, String specialty, String phone) {
}