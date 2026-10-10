package com.frigora.frigoraplatform.monitoring.application.internal.commandservices;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Equipment;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.CreateEquipmentCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.DeleteEquipmentCommand;
import com.frigora.frigoraplatform.monitoring.domain.repositories.EquipmentRepository;
import com.frigora.frigoraplatform.monitoring.domain.services.EquipmentCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipmentCommandServiceImpl implements EquipmentCommandService {

    private final EquipmentRepository equipmentRepository;

    @Override
    @Transactional
    public Equipment create(CreateEquipmentCommand command) {
        Equipment equipment = Equipment.builder()
                .model(command.model())
                .type(command.type())
                .serial(command.serial())
                .status(command.status())
                .installed(command.installed())
                .lastSeen(command.lastSeen())
                .setPoint(command.setPoint())
                .name(command.name())
                .manufacturer(command.manufacturer())
                .online(command.online())
                .ownerId(command.ownerId())
                .build();

        return equipmentRepository.save(equipment);
    }

    @Override
    @Transactional
    public boolean delete(DeleteEquipmentCommand command) {
        if (!equipmentRepository.existsById(command.id())) {
            return false;
        }
        equipmentRepository.deleteById(command.id());
        return true;
    }
}
