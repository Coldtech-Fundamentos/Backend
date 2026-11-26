package com.frigora.frigoraplatform.monitoring.infrastructure.persistence.jpa.repositories;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Alert;
import com.frigora.frigoraplatform.monitoring.domain.repositories.AlertRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaAlertRepository extends JpaRepository<Alert, Long>, AlertRepository {
    @Override
    List<Alert> findByEquipmentId(Long equipmentId);

    @Override
    List<Alert> findByTenantId(Long tenantId);

    @Override
    Optional<Alert> findByIdAndTenantId(Long id, Long tenantId);

    @Override
    List<Alert> findByEquipmentIdAndTenantId(Long equipmentId, Long tenantId);

    @Override
    long deleteByIdAndTenantId(Long id, Long tenantId);
}
