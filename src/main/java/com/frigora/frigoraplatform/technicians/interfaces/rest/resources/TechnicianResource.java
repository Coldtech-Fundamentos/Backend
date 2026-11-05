package com.frigora.frigoraplatform.technicians.interfaces.rest.resources;

public record TechnicianResource(
        Integer id,
        String name,
        String specialty,
        String phone,
        Integer providerId
) {
}