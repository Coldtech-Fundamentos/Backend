package com.frigora.frigoraplatform.servicerequests.domain.model.queries;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestStatus;

public record GetServiceRequestsByProviderIdQuery(
        int providerId,
        ServiceRequestStatus.EServiceRequestStatus status) {}