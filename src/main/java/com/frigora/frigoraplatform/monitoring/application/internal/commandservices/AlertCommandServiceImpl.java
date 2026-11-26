package com.frigora.frigoraplatform.monitoring.application.internal.commandservices;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Alert;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.AcknowledgeAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.CreateAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.DeleteAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.EAlertStatus;
import com.frigora.frigoraplatform.monitoring.domain.repositories.AlertRepository;
import com.frigora.frigoraplatform.monitoring.domain.services.AlertCommandService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertCommandServiceImpl implements AlertCommandService {

    private final AlertRepository alertRepository;

    @Override
    @Transactional
    public Alert create(CreateAlertCommand command) {
        Alert alert =
                Alert.builder()
                        .tenantId(command.tenantId())
                        .equipmentId(command.equipmentId())
                        .siteId(command.siteId())
                        .type(command.type())
                        .severity(command.severity())
                        .description(command.description())
                        .status(EAlertStatus.ACTIVE)
                        .build();

        return alertRepository.save(alert);
    }

    @Override
    @Transactional
    public Optional<Alert> acknowledge(AcknowledgeAlertCommand command) {
        return alertRepository
                .findByIdAndTenantId(command.id(), command.tenantId())
                .map(
                        alert -> {
                            alert.acknowledge();
                            return alertRepository.save(alert);
                        });
    }

    @Override
    @Transactional
    public Optional<Alert> resolve(AcknowledgeAlertCommand command) {
        return alertRepository
                .findByIdAndTenantId(command.id(), command.tenantId())
                .map(
                        alert -> {
                            alert.resolve();
                            return alertRepository.save(alert);
                        });
    }

    @Override
    @Transactional
    public Optional<Alert> close(AcknowledgeAlertCommand command) {
        return alertRepository
                .findByIdAndTenantId(command.id(), command.tenantId())
                .map(
                        alert -> {
                            alert.close();
                            return alertRepository.save(alert);
                        });
    }

    @Override
    @Transactional
    public boolean delete(DeleteAlertCommand command) {
        return alertRepository.deleteByIdAndTenantId(command.id(), command.tenantId()) > 0;
    }
}
