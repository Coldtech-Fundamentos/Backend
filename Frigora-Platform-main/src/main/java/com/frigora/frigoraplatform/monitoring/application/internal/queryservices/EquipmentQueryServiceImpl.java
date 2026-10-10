package com.frigora.frigoraplatform.monitoring.application.internal.queryservices;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Equipment;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllEquipmentQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetEquipmentByIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetEquipmentsByOwnerQuery;
import com.frigora.frigoraplatform.monitoring.domain.repositories.EquipmentRepository;
import com.frigora.frigoraplatform.monitoring.domain.services.EquipmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipmentQueryServiceImpl implements EquipmentQueryService {

    private final EquipmentRepository equipmentRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Equipment> getAll(GetAllEquipmentQuery query) {
        return equipmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Equipment> getById(GetEquipmentByIdQuery query) {
        return equipmentRepository.findById(query.id());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> getAllByOwner(GetEquipmentsByOwnerQuery query) {
        return equipmentRepository.findByOwnerId(query.ownerId()); // ← AÑADIDO
    }
}
