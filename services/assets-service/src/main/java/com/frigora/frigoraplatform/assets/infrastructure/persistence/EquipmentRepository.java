package com.frigora.frigoraplatform.assets.infrastructure.persistence;

import com.frigora.frigoraplatform.assets.domain.model.Equipment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    Optional<Equipment> findByIdAndOwnerId(Long id, Long ownerId);
}
