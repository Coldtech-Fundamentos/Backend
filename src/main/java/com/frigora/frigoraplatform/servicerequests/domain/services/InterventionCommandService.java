package com.frigora.frigoraplatform.servicerequests.domain.services;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.Intervention;
import com.frigora.frigoraplatform.servicerequests.domain.model.commands.RecordInterventionCommand;

import java.util.Optional;

public interface InterventionCommandService {
    Optional<Intervention> handle(RecordInterventionCommand command);
}