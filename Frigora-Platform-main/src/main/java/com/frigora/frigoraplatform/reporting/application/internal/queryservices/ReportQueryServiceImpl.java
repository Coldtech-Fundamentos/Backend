package com.frigora.frigoraplatform.reporting.application.internal.queryservices;

import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetAllReportsByEquipmentIdQuery;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetAllReportsByTenantIdQuery;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetAllReportsQuery;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetReportByIdQuery;
import com.frigora.frigoraplatform.reporting.domain.repositories.ReportRepository;
import com.frigora.frigoraplatform.reporting.domain.services.ReportQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReportQueryServiceImpl implements ReportQueryService {

    private final ReportRepository reportRepository;

    public ReportQueryServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Report> handle(GetReportByIdQuery query) {
        return reportRepository.findById(query.reportId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> handle(GetAllReportsQuery query) {
        return reportRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> handle(GetAllReportsByTenantIdQuery query) {
        return reportRepository.findAllByTenantId(query.tenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> handle(GetAllReportsByEquipmentIdQuery query) {
        return reportRepository.findAllByEquipmentId(query.equipmentId());
    }
}