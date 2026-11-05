package com.frigora.frigoraplatform.reporting.infrastructure.persistence.jpa.repositories;

import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaReportRepository extends JpaRepository<Report, Integer> {

    List<Report> findAllByTenantId(Integer tenantId);

    List<Report> findAllByEquipmentIdAndTenantId(Integer equipmentId, Integer tenantId);

    Optional<Report> findByIdAndTenantId(Integer id, Integer tenantId);
}
