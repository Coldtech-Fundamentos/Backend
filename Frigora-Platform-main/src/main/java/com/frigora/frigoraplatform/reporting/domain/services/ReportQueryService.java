package com.frigora.frigoraplatform.reporting.domain.services;


import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetAllReportsByEquipmentIdQuery;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetAllReportsByTenantIdQuery;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetAllReportsQuery;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetReportByIdQuery;

import java.util.List;
import java.util.Optional;

public interface ReportQueryService {
    Optional<Report> handle(GetReportByIdQuery query);
    List<Report> handle(GetAllReportsQuery query);
    List<Report> handle(GetAllReportsByTenantIdQuery query);
    List<Report> handle(GetAllReportsByEquipmentIdQuery query);
}