package com.frigora.frigoraplatform.servicerequests.domain.model.aggregates;

import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.InterventionStatus;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestId;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.TechnicianId;
import com.frigora.frigoraplatform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interventions")
@Getter
@NoArgsConstructor
public class Intervention extends AuditableAbstractAggregateRoot<Intervention> {

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "service_request_id"))
    private ServiceRequestId serviceRequestId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "technician_id"))
    private TechnicianId technicianId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterventionStatus.EInterventionStatus status;

    @Column(name = "summary")
    private String summary;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @ElementCollection
    @CollectionTable(name = "intervention_photos", joinColumns = @JoinColumn(name = "intervention_id"))
    @Column(name = "photo_url")
    private List<String> photoUrls = new ArrayList<>();

    public Intervention(int serviceRequestId, int technicianId, String summary,
                        LocalDateTime startTime, LocalDateTime endTime, List<String> photoUrls) {
        this.serviceRequestId = new ServiceRequestId(serviceRequestId);
        this.technicianId = new TechnicianId(technicianId);
        this.summary = summary;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = (endTime != null) ? InterventionStatus.EInterventionStatus.COMPLETED : InterventionStatus.EInterventionStatus.PENDING;
        this.photoUrls = photoUrls != null ? photoUrls : new ArrayList<>();
    }
}
