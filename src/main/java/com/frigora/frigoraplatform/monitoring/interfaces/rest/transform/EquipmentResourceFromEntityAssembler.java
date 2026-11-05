package com.frigora.frigoraplatform.monitoring.interfaces.rest.transform;

import com.frigora.frigoraplatform.monitoring.domain.model.aggregates.Equipment;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.resources.EquipmentResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EquipmentResourceFromEntityAssembler {

    public EquipmentResource toResource(Equipment equipment) {
        return new EquipmentResource(
                equipment.getId(),
                equipment.getEquipmentId(),
                equipment.getModel(),
                equipment.getType(),
                equipment.getSerial(),
                equipment.getStatus(),
                equipment.getInstalled(),
                equipment.getLastSeen(),
                equipment.getSetPoint(),
                equipment.getName(),
                equipment.getManufacturer(),
                equipment.getOnline(),
                equipment.getCreated(),
                equipment.getUpdated(),
                equipment.getOwnerId()  
        );
    }

    public List<EquipmentResource> toResources(List<Equipment> equipments) {
        return equipments.stream().map(this::toResource).toList();
    }
}
