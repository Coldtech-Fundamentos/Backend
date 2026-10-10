package com.frigora.frigoraplatform.monitoring.domain.repositories;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Equipment;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository {
    List<Equipment> findAll();
    Optional<Equipment> findById(Long id);
    Equipment save(Equipment equipment);
    void deleteById(Long id);
    boolean existsById(Long id);
    List<Equipment> findByOwnerId(Long ownerId);
}