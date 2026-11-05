package com.frigora.frigoraplatform.technicians.infrastructure.persistence.jpa.repositories;

import com.frigora.frigoraplatform.technicians.domain.model.aggregates.Technician;
import com.frigora.frigoraplatform.technicians.domain.repositories.TechnicianRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TechnicianRepositoryImpl implements TechnicianRepository {

    private final JpaTechnicianRepository jpaRepository;

    public TechnicianRepositoryImpl(JpaTechnicianRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Technician save(Technician technician) {
        return jpaRepository.save(technician);
    }

    @Override
    public void delete(Technician technician) {
        jpaRepository.delete(technician);
    }

    @Override
    public Optional<Technician> findById(Integer id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Technician> findAllByProviderId(Integer providerId) {
        return jpaRepository.findAllByProviderId(providerId);
    }

    @Override
    public List<Technician> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsById(Integer id) {
        return jpaRepository.existsById(id);
    }
}