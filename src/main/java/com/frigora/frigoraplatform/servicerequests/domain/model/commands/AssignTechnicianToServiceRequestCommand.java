package com.frigora.frigoraplatform.servicerequests.domain.model.commands;

public record AssignTechnicianToServiceRequestCommand(int serviceRequestId, int technicianId) {}