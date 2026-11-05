package com.frigora.frigoraplatform.technicians.application.internal.queryservices;

import com.frigora.frigoraplatform.technicians.domain.model.aggregates.Technician;
import com.frigora.frigoraplatform.technicians.domain.model.queries.GetAllTechniciansQuery;
import com.frigora.frigoraplatform.technicians.domain.model.queries.GetTechnicianByIdQuery;
import com.frigora.frigoraplatform.technicians.domain.model.queries.GetTechniciansByProviderIdQuery;
import com.frigora.frigoraplatform.technicians.domain.repositories.TechnicianRepository;
import com.frigora.frigoraplatform.technicians.domain.services.TechnicianQueryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TechnicianQueryServiceImpl implements TechnicianQueryService {

    private final TechnicianRepository technicianRepository;

    public TechnicianQueryServiceImpl(TechnicianRepository technicianRepository) {
        this.technicianRepository = technicianRepository;
    }

    @Override
    public List<Technician> handle(GetAllTechniciansQuery query) {
        return technicianRepository.findAll();
    }

    @Override
    public Optional<Technician> handle(GetTechnicianByIdQuery query) {
        return technicianRepository.findById(query.id());
    }

    @Override
    public List<Technician> handle(GetTechniciansByProviderIdQuery query) {
        return technicianRepository.findAllByProviderId(query.providerId());
    }
}