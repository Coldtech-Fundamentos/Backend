package com.frigora.frigoraplatform.monitoring.domain.services;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Equipment;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllEquipmentQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetEquipmentByIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetEquipmentsByOwnerQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EquipmentQueryService {
    List<Equipment> getAllByOwner(GetEquipmentsByOwnerQuery query);

    @Transactional(readOnly = true)
    List<Equipment> getAll(GetAllEquipmentQuery query);

    Optional<Equipment> getById(GetEquipmentByIdQuery query);
}
