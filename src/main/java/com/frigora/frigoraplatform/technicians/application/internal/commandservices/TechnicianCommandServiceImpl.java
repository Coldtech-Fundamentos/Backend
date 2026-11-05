package com.frigora.frigoraplatform.technicians.application.internal.commandservices;

import com.frigora.frigoraplatform.technicians.domain.model.aggregates.Technician;
import com.frigora.frigoraplatform.technicians.domain.model.commands.CreateTechnicianCommand;
import com.frigora.frigoraplatform.technicians.domain.model.commands.DeleteTechnicianCommand;
import com.frigora.frigoraplatform.technicians.domain.model.commands.UpdateTechnicianCommand;
import com.frigora.frigoraplatform.technicians.domain.repositories.TechnicianRepository;
import com.frigora.frigoraplatform.technicians.domain.services.TechnicianCommandService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TechnicianCommandServiceImpl implements TechnicianCommandService {

    private final TechnicianRepository technicianRepository;

    public TechnicianCommandServiceImpl(TechnicianRepository technicianRepository) {
        this.technicianRepository = technicianRepository;
    }

    @Override
    public Optional<Technician> handle(CreateTechnicianCommand command) {
        var technician = new Technician(command);
        var createdTechnician = technicianRepository.save(technician);
        return Optional.of(createdTechnician);
    }

    @Override
    public Optional<Technician> handle(UpdateTechnicianCommand command) {
        var result = technicianRepository.findById(command.id());
        if (result.isEmpty()) return Optional.empty();

        var technicianToUpdate = result.get();
        // Ejecutamos el método update propio de tu entidad aggregate
        technicianToUpdate.update(command.name(), command.specialty(), command.phone());
        var updatedTechnician = technicianRepository.save(technicianToUpdate);

        return Optional.of(updatedTechnician);
    }

    @Override
    public void handle(DeleteTechnicianCommand command) {
        var result = technicianRepository.findById(command.id());
        // Si existe el técnico, lo elimina a través del repositorio
        result.ifPresent(technicianRepository::delete);
    }
}