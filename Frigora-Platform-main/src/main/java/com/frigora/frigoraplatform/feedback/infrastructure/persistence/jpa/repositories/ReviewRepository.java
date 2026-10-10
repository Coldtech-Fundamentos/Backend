package com.frigora.frigoraplatform.feedback.infrastructure.persistence.jpa.repositories;

import com.frigora.frigoraplatform.feedback.domain.model.aggregates.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByServiceRequestIdValue(int serviceRequestId);

}
