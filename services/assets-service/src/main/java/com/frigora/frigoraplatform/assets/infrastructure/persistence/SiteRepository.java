package com.frigora.frigoraplatform.assets.infrastructure.persistence;

import com.frigora.frigoraplatform.assets.domain.model.Site;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Integer> {
    List<Site> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    Optional<Site> findByIdAndOwnerId(Integer id, Long ownerId);
}
