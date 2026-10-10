package com.frigora.frigoraplatform.technicians.infrastructure.persistence.jpa.repositories;

import com.frigora.frigoraplatform.technicians.domain.model.aggregates.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaTechnicianRepository extends JpaRepository<Technician, Integer> {
    List<Technician> findAllByProviderId(Integer providerId);
}