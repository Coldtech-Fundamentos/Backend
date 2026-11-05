package com.frigora.frigoraplatform.monitoring.domain.services;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Equipment;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.CreateEquipmentCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.DeleteEquipmentCommand;

public interface EquipmentCommandService {
    Equipment create(CreateEquipmentCommand command);

    boolean delete(DeleteEquipmentCommand command);
}
