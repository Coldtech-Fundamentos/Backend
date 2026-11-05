package com.frigora.frigoraplatform.servicerequests.infrastructure.persistence.jpa.repositories;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    /**
     * Finds all interventions by service request id.
     * @param serviceRequestId The service request id.
     * @return A list of interventions.
     */
    List<Intervention> findByServiceRequestIdValue(int serviceRequestId);
}
