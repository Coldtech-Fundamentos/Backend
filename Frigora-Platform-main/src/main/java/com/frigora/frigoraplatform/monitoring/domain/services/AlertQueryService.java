package com.frigora.frigoraplatform.monitoring.domain.services;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Alert;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAlertByIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByEquipmentIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByTenantIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsQuery;

import java.util.List;
import java.util.Optional;

public interface AlertQueryService {
    List<Alert> getAll(GetAllAlertsQuery query);

    Optional<Alert> getById(GetAlertByIdQuery query);

    List<Alert> getAllByEquipmentId(GetAllAlertsByEquipmentIdQuery query);

    List<Alert> getAllByTenantId(GetAllAlertsByTenantIdQuery query);
}
