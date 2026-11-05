package com.frigora.frigoraplatform.servicerequests.domain.model.aggregates;

import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.*;
import com.frigora.frigoraplatform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_requests")
@Getter
@NoArgsConstructor
public class ServiceRequest extends AuditableAbstractAggregateRoot<ServiceRequest> {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "requester_id"))
    private RequesterId requesterId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "site_id"))
    private SiteId siteId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "equipment_id"))
    private EquipmentId equipmentId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "assigned_to"))
    private AssignedTo assignedTo;

    @Column(name = "origin")
    private String origin;

    @Embedded
    private ServiceRequestType type;

    @Embedded
    private ServiceRequestPriority priority;

    @Column(name = "description")
    private String description;

    @Embedded
    private ServiceRequestStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "technician_id"))
    private TechnicianId technicianId;

    public ServiceRequest(int requesterId, int siteId, int equipmentId, int assignedTo,
                          String origin, ServiceRequestType.EServiceRequestType type, ServiceRequestPriority.EServiceRequestPriority priority,
                          String description) {
        this.requesterId = new RequesterId(requesterId);
        this.siteId = new SiteId(siteId);
        this.equipmentId = new EquipmentId(equipmentId);
        this.assignedTo = new AssignedTo(assignedTo);
        this.origin = origin;
        this.type = new ServiceRequestType(type);
        this.priority = new ServiceRequestPriority(priority);
        this.description = description;
        this.status = new ServiceRequestStatus(ServiceRequestStatus.EServiceRequestStatus.PENDING);
    }

    // Domain methods
    public void accept() {
        this.status = new ServiceRequestStatus(ServiceRequestStatus.EServiceRequestStatus.ACCEPTED);
    }

    public void reject() {
        this.status = new ServiceRequestStatus(ServiceRequestStatus.EServiceRequestStatus.REJECTED);
    }

    public void cancel() {
        this.status = new ServiceRequestStatus(ServiceRequestStatus.EServiceRequestStatus.CANCELED);
        this.canceledAt = LocalDateTime.now(Clock.systemUTC());
    }

    public void assignTechnician(int technicianId) {
        this.technicianId = new TechnicianId(technicianId);
        this.status = new ServiceRequestStatus(ServiceRequestStatus.EServiceRequestStatus.IN_PROGRESS);
    }

    public void complete() {
        this.status = new ServiceRequestStatus(ServiceRequestStatus.EServiceRequestStatus.COMPLETED);
        this.completedAt = LocalDateTime.now(Clock.systemUTC());
    }
}