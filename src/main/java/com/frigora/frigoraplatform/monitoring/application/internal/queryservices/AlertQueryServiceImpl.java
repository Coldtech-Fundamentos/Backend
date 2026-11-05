package com.frigora.frigoraplatform.monitoring.application.internal.queryservices;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Alert;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAlertByIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByEquipmentIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByTenantIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsQuery;
import com.frigora.frigoraplatform.monitoring.domain.repositories.AlertRepository;
import com.frigora.frigoraplatform.monitoring.domain.services.AlertQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alertRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getAll(GetAllAlertsQuery query) {
        return alertRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Alert> getById(GetAlertByIdQuery query) {
        return alertRepository.findById(query.id());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getAllByEquipmentId(GetAllAlertsByEquipmentIdQuery query) {
        return alertRepository.findByEquipmentId(query.equipmentId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getAllByTenantId(GetAllAlertsByTenantIdQuery query) {
        return alertRepository.findByTenantId(query.tenantId());
    }
}
