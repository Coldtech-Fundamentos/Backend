package com.frigora.frigoraplatform.monitoring.domain.repositories;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Alert;

import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    List<Alert> findAll();

    Optional<Alert> findById(Long id);

    List<Alert> findByEquipmentId(Long equipmentId);

    List<Alert> findByTenantId(Long tenantId);

    Alert save(Alert alert);

    void deleteById(Long id);

    boolean existsById(Long id);
}
