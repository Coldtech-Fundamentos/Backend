package com.frigora.frigoraplatform.servicerequests.application.internal.commandservices;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.Intervention;
import com.frigora.frigoraplatform.servicerequests.domain.model.commands.RecordInterventionCommand;
import com.frigora.frigoraplatform.servicerequests.domain.services.InterventionCommandService;
import com.frigora.frigoraplatform.servicerequests.infrastructure.persistence.jpa.repositories.InterventionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InterventionCommandServiceImpl implements InterventionCommandService {

    private final InterventionRepository interventionRepository;

    public Optional<Intervention> handle(RecordInterventionCommand command) {
        var intervention = new Intervention(
                command.serviceRequestId(),
                command.technicianId(),
                command.summary(),
                command.startTime(),
                command.endTime(),
                command.photoUrls()
        );

        interventionRepository.save(intervention);
        return Optional.of(intervention);
    }
}
