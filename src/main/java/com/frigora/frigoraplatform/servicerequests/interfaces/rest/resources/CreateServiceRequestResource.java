package com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources;

public record CreateServiceRequestResource(
        int requesterId,
        int siteId,
        int equipmentId,
        int assignedTo,
        String origin,
        String type,
        String priority,
        String description) { }
