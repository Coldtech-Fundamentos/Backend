package com.frigora.frigoraplatform.technicians.domain.repositories;

import com.frigora.frigoraplatform.technicians.domain.model.aggregates.Technician;

import java.util.List;
import java.util.Optional;

public interface TechnicianRepository {
    Technician save(Technician technician);
    void delete(Technician technician);
    Optional<Technician> findById(Integer id);
    List<Technician> findAllByProviderId(Integer providerId);
    List<Technician> findAll();
    boolean existsById(Integer id);
}