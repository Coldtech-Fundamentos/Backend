package com.frigora.frigoraplatform.technicians.domain.services;

import com.frigora.frigoraplatform.technicians.domain.model.aggregates.Technician;
import com.frigora.frigoraplatform.technicians.domain.model.commands.CreateTechnicianCommand;
import com.frigora.frigoraplatform.technicians.domain.model.commands.DeleteTechnicianCommand;
import com.frigora.frigoraplatform.technicians.domain.model.commands.UpdateTechnicianCommand;

import java.util.Optional;

public interface TechnicianCommandService {
    Optional<Technician> handle(CreateTechnicianCommand command);
    Optional<Technician> handle(UpdateTechnicianCommand command);
    void handle(DeleteTechnicianCommand command);
}