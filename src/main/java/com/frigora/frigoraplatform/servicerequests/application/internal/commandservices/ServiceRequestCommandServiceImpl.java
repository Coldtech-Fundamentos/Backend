package com.frigora.frigoraplatform.servicerequests.application.internal.commandservices;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.ServiceRequest;
import com.frigora.frigoraplatform.servicerequests.domain.model.commands.*;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestStatus;
import com.frigora.frigoraplatform.servicerequests.domain.services.ServiceRequestCommandService;
import com.frigora.frigoraplatform.servicerequests.infrastructure.persistence.jpa.repositories.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceRequestCommandServiceImpl implements ServiceRequestCommandService {

    private final ServiceRequestRepository serviceRequestRepository;

    @Override
    @Transactional
    public Optional<ServiceRequest> handle(CreateServiceRequestCommand command) {

        var serviceRequest = new ServiceRequest(
                command.requesterId(),
                command.siteId(),
                command.equipmentId(),
                command.assignedTo(),
                command.origin(),
                command.type(),
                command.priority(),
                command.description()
        );

        serviceRequestRepository.save(serviceRequest);
        return Optional.of(serviceRequest);
    }

    @Override
    @Transactional
    public Optional<ServiceRequest> handle(AcceptServiceRequestCommand command) {

        return serviceRequestRepository.findById((long) command.serviceRequestId())
                .map(serviceRequest -> {
                    serviceRequest.accept();
                    return serviceRequest;
                });
    }

    @Override
    @Transactional
    public Optional<ServiceRequest> handle(RejectServiceRequestCommand command) {

        return serviceRequestRepository.findById((long) command.ServiceRequestId())
                .map(serviceRequest -> {
                    serviceRequest.reject();
                    return serviceRequest;
                });
    }

    @Override
    @Transactional
    public Optional<ServiceRequest> handle(CancelServiceRequestCommand command) {

        return serviceRequestRepository.findById((long) command.ServiceRequestId())
                .map(serviceRequest -> {
                    serviceRequest.cancel();
                    return serviceRequest;
                });
    }

    @Override
    @Transactional
    public Optional<ServiceRequest> handle(AssignTechnicianToServiceRequestCommand command) {

        return serviceRequestRepository.findById((long) command.serviceRequestId())
                .map(serviceRequest -> {
                    serviceRequest.assignTechnician(command.technicianId());
                    return serviceRequest;
                });
    }

    @Override
    @Transactional
    public Optional<ServiceRequest> handle(CompleteServiceRequestCommand command) {

        return serviceRequestRepository.findById((long) command.ServiceRequestId())
                .map(serviceRequest -> {
                    serviceRequest.complete();
                    return serviceRequest;
                });
    }

    @Override
    @Transactional
    public boolean handle(DeleteServiceRequestCommand command) {

        var request = serviceRequestRepository
                .findById((long) command.ServiceRequestId());

        if (request.isEmpty())
            return false;
        var serviceRequest = request.get();
        if (serviceRequest.getStatus().getStatus() != ServiceRequestStatus.EServiceRequestStatus.COMPLETED &&
                serviceRequest.getStatus().getStatus() != ServiceRequestStatus.EServiceRequestStatus.CANCELED &&
                serviceRequest.getStatus().getStatus() != ServiceRequestStatus.EServiceRequestStatus.REJECTED) {

            throw new IllegalStateException(
                    "Only completed, canceled or rejected requests can be deleted"
            );
        }
        serviceRequestRepository.delete(serviceRequest);

        return true;
    }
}