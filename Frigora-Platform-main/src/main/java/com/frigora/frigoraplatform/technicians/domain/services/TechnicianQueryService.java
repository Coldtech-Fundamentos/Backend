package com.frigora.frigoraplatform.technicians.domain.services;

import com.frigora.frigoraplatform.technicians.domain.model.aggregates.Technician;
import com.frigora.frigoraplatform.technicians.domain.model.queries.GetAllTechniciansQuery;
import com.frigora.frigoraplatform.technicians.domain.model.queries.GetTechnicianByIdQuery;
import com.frigora.frigoraplatform.technicians.domain.model.queries.GetTechniciansByProviderIdQuery;

import java.util.List;
import java.util.Optional;

public interface TechnicianQueryService {
    List<Technician> handle(GetAllTechniciansQuery query);
    Optional<Technician> handle(GetTechnicianByIdQuery query);
    List<Technician> handle(GetTechniciansByProviderIdQuery query);
}