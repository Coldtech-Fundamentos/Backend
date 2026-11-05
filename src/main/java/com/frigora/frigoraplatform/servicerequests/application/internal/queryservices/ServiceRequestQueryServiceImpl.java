package com.frigora.frigoraplatform.servicerequests.application.internal.queryservices;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.ServiceRequest;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetServiceRequestByIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetServiceRequestsByProviderIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetServiceRequestsByRequesterIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.services.ServiceRequestQueryService;
import com.frigora.frigoraplatform.servicerequests.infrastructure.persistence.jpa.repositories.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceRequestQueryServiceImpl implements ServiceRequestQueryService {

    private final ServiceRequestRepository serviceRequestRepository;

    @Override
    public Optional<ServiceRequest> handle(GetServiceRequestByIdQuery query) {
        return serviceRequestRepository.findById((long) query.ServiceRequestId()); // camelCase
    }

    @Override
    public List<ServiceRequest> handle(GetServiceRequestsByRequesterIdQuery query) {
        return serviceRequestRepository.findByRequesterIdValue(query.RequesterId()); // camelCase
    }

    @Override
    public List<ServiceRequest> handle(GetServiceRequestsByProviderIdQuery query) {
        if (query.status() != null) {
            return serviceRequestRepository.findByAssignedToValueAndStatusStatus(
                    query.providerId(),
                    query.status()
            );
        }
        return serviceRequestRepository.findByAssignedToValue(query.providerId());
    }
}
