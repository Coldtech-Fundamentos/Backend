package com.frigora.frigoraplatform.feedback.domain.model.aggregates;


import com.frigora.frigoraplatform.feedback.domain.model.valueobjects.ReviewRating;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestId;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.TechnicianId;
import com.frigora.frigoraplatform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
public class Review extends AuditableAbstractAggregateRoot<Review> {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "service_request_id"))
    private ServiceRequestId serviceRequestId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "technician_id"))
    private TechnicianId technicianId;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "rating")
    )
    private ReviewRating rating;

    @Column(name = "comment", nullable = false)
    private String comment;

    public Review(
            Integer serviceRequestId,
            Integer technicianId,
            Integer rating,
            String comment
    ) {
        this.serviceRequestId = new ServiceRequestId(serviceRequestId);
        this.technicianId = new TechnicianId(technicianId);
        this.rating = new ReviewRating(rating);
        this.comment = comment;
    }
}