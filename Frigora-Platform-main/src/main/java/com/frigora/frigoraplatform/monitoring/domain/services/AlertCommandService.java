package com.frigora.frigoraplatform.monitoring.domain.services;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Alert;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.AcknowledgeAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.CreateAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.DeleteAlertCommand;

import java.util.Optional;

public interface AlertCommandService {
    Alert create(CreateAlertCommand command);

    Optional<Alert> acknowledge(AcknowledgeAlertCommand command);

    Optional<Alert> resolve(AcknowledgeAlertCommand command);

    Optional<Alert> close(AcknowledgeAlertCommand command);

    boolean delete(DeleteAlertCommand command);
}
