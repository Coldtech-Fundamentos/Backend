package com.frigora.frigoraplatform.monitoring.interfaces.rest.transform;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Alert;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.resources.AlertResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlertResourceFromEntityAssembler {

    public AlertResource toResource(Alert alert) {
        return new AlertResource(
                alert.getId(),
                alert.getTenantId(),
                alert.getEquipmentId(),
                alert.getSiteId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getDescription(),
                alert.getStatus(),
                alert.getCreatedAt()
        );
    }

    public List<AlertResource> toResources(List<Alert> alerts) {
        return alerts.stream().map(this::toResource).toList();
    }
}
