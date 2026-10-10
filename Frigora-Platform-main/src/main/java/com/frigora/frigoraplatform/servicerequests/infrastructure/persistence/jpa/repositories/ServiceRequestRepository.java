package com.frigora.frigoraplatform.servicerequests.infrastructure.persistence.jpa.repositories;

import com.frigora.frigoraplatform.servicerequests.domain.model.aggregates.ServiceRequest;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    /**
     * Finds all service requests by requester id.
     * @param requesterId The requester id.
     * @return A list of service requests.
     */
    List<ServiceRequest> findByRequesterIdValue(int requesterId);

    /**
     * Finds all service requests by assigned to (provider) id.
     * @param assignedTo The provider id.
     * @return A list of service requests.
     */
    List<ServiceRequest> findByAssignedToValue(int assignedTo);

    /**
     * Finds all service requests by assigned to (provider) id and status.
     * @param assignedTo The provider id.
     * @param status The status filter.
     * @return A list of service requests.
     */
    List<ServiceRequest> findByAssignedToValueAndStatusStatus(
            int assignedTo,
            ServiceRequestStatus.EServiceRequestStatus status
    );
}