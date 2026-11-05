package com.frigora.frigoraplatform.technicians.interfaces.rest.resources;

public record UpdateTechnicianResource(
        String name,
        String specialty,
        String phone
) {
}