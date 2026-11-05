package com.frigora.frigoraplatform.reporting.domain.repositories;

import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {

    Report save(Report report);

    void delete(Report report);

    Optional<Report> findByIdAndTenantId(Integer id, Integer tenantId);

    List<Report> findAll();

    List<Report> findAllByTenantId(Integer tenantId);

    List<Report> findAllByEquipmentIdAndTenantId(Integer equipmentId, Integer tenantId);

    boolean existsById(Integer id);
}
