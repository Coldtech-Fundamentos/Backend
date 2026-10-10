package com.frigora.frigoraplatform.reporting.infrastructure.persistence.jpa.repositories;

import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaReportRepository extends JpaRepository<Report, Integer> {

    // Spring Data JPA crea la query "SELECT * FROM reports WHERE tenant_id = ?" automaticamente
    List<Report> findAllByTenantId(Integer tenantId);

    // Spring Data JPA crea la query "SELECT * FROM reports WHERE equipment_id = ?" automaticamente
    List<Report> findAllByEquipmentId(Integer equipmentId);
}