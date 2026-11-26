package com.frigora.frigoraplatform.assets.application;

import com.frigora.frigoraplatform.assets.domain.model.Equipment;
import com.frigora.frigoraplatform.assets.domain.model.EquipmentStatus;
import com.frigora.frigoraplatform.assets.infrastructure.persistence.EquipmentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EquipmentService {
    private final EquipmentRepository equipment;

    public EquipmentService(EquipmentRepository equipment) {
        this.equipment = equipment;
    }

    @Transactional(readOnly = true)
    public List<Equipment> findAllForOwner(long ownerId) {
        return equipment.findAllByOwnerIdOrderByIdAsc(ownerId);
    }

    @Transactional(readOnly = true)
    public Optional<Equipment> findByIdForOwner(long id, long ownerId) {
        return equipment.findByIdAndOwnerId(id, ownerId);
    }

    @Transactional
    public Equipment create(
            String model,
            String type,
            String serial,
            EquipmentStatus status,
            LocalDateTime installed,
            LocalDateTime lastSeen,
            Float setPoint,
            String name,
            String manufacturer,
            Boolean online,
            long ownerId) {
        return equipment.save(
                new Equipment(
                        model,
                        type,
                        serial,
                        status,
                        installed,
                        lastSeen,
                        setPoint,
                        name,
                        manufacturer,
                        online,
                        ownerId));
    }

    @Transactional
    public boolean deleteForOwner(long id, long ownerId) {
        return equipment
                .findByIdAndOwnerId(id, ownerId)
                .map(
                        item -> {
                            equipment.delete(item);
                            return true;
                        })
                .orElse(false);
    }
}
