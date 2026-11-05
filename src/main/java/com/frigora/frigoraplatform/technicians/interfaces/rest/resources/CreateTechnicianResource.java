package com.frigora.frigoraplatform.technicians.interfaces.rest.resources;

public record CreateTechnicianResource(
        String name,
        String specialty,
        String phone,
        Integer providerId
) {
}