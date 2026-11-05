package com.frigora.frigoraplatform.reporting.infrastructure.persistence.jpa.repositories;

import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import com.frigora.frigoraplatform.reporting.domain.repositories.ReportRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReportRepositoryImpl implements ReportRepository {

    private final JpaReportRepository jpaRepository;

    // Inyectamos el repositorio de Spring Data
    public ReportRepositoryImpl(JpaReportRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Report save(Report report) {
        return jpaRepository.save(report);
    }

    @Override
    public void delete(Report report) {
        jpaRepository.delete(report);
    }

    @Override
    public Optional<Report> findByIdAndTenantId(Integer id, Integer tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId);
    }

    @Override
    public List<Report> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Report> findAllByTenantId(Integer tenantId) {
        return jpaRepository.findAllByTenantId(tenantId);
    }

    @Override
    public List<Report> findAllByEquipmentIdAndTenantId(Integer equipmentId, Integer tenantId) {
        return jpaRepository.findAllByEquipmentIdAndTenantId(equipmentId, tenantId);
    }

    @Override
    public boolean existsById(Integer id) {
        return jpaRepository.existsById(id);
    }
}
