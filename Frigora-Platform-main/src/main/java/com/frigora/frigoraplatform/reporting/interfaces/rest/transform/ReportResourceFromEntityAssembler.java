package com.frigora.frigoraplatform.reporting.interfaces.rest.transform;


import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import com.frigora.frigoraplatform.reporting.interfaces.rest.resources.ReportResource;

public class ReportResourceFromEntityAssembler {
    public static ReportResource toResourceFromEntity(Report entity) {
        return new ReportResource(
                entity.getId(),
                entity.getTenantId(),
                entity.getType(),
                entity.getEquipmentId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getSummary(),
                entity.getContent(),
                entity.getUrl()
        );
    }
}