package com.frigora.frigoraplatform.servicerequests.application.internal.queryservices;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.Intervention;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetInterventionByIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetInterventionsByServiceRequestIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.services.InterventionQueryService;
import com.frigora.frigoraplatform.servicerequests.infrastructure.persistence.jpa.repositories.InterventionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InterventionQueryServiceImpl implements InterventionQueryService {

    private final InterventionRepository interventionRepository;

    @Override
    public Optional<Intervention> handle(GetInterventionByIdQuery query) {
        return interventionRepository.findById((long) query.InterventionId());
    }

    @Override
    public List<Intervention> handle(GetInterventionsByServiceRequestIdQuery query) {
        return interventionRepository.findByServiceRequestIdValue(query.ServiceRequestId());
    }
}