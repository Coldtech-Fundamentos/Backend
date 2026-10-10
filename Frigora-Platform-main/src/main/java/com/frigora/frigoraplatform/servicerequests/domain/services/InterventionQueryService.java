package com.frigora.frigoraplatform.servicerequests.domain.services;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.Intervention;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetInterventionByIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetInterventionsByServiceRequestIdQuery;

import java.util.List;
import java.util.Optional;

public interface InterventionQueryService {
    Optional<Intervention> handle(GetInterventionByIdQuery query);
    List<Intervention> handle(GetInterventionsByServiceRequestIdQuery query);
}