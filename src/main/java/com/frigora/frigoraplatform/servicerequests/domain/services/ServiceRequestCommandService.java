package com.frigora.frigoraplatform.servicerequests.domain.services;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.ServiceRequest;
import com.frigora.frigoraplatform.servicerequests.domain.model.commands.*;

import java.util.Optional;

public interface ServiceRequestCommandService {
    Optional<ServiceRequest> handle(CreateServiceRequestCommand command);
    Optional<ServiceRequest> handle(AcceptServiceRequestCommand command);
    Optional<ServiceRequest> handle(RejectServiceRequestCommand command);
    Optional<ServiceRequest> handle(CancelServiceRequestCommand command);
    Optional<ServiceRequest> handle(AssignTechnicianToServiceRequestCommand command);
    Optional<ServiceRequest> handle(CompleteServiceRequestCommand command);
    boolean handle(DeleteServiceRequestCommand command);
}