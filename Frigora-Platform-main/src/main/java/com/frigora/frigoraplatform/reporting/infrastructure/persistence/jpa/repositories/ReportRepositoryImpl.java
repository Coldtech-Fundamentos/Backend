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
    public Optional<Report> findById(Integer id) {
        return jpaRepository.findById(id);
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
    public List<Report> findAllByEquipmentId(Integer equipmentId) {
        return jpaRepository.findAllByEquipmentId(equipmentId);
    }

    @Override
    public boolean existsById(Integer id) {
        return jpaRepository.existsById(id);
    }
}