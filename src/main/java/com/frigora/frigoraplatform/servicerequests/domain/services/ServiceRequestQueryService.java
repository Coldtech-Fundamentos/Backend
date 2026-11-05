package com.frigora.frigoraplatform.servicerequests.domain.services;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.ServiceRequest;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetServiceRequestByIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetServiceRequestsByProviderIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetServiceRequestsByRequesterIdQuery;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestQueryService {
    Optional<ServiceRequest> handle(GetServiceRequestByIdQuery query);
    List<ServiceRequest> handle(GetServiceRequestsByRequesterIdQuery query);
    List<ServiceRequest> handle(GetServiceRequestsByProviderIdQuery query);
}
