package com.frigora.frigoraplatform.assetsManagement.infrastructure.persistence.jpa.repositories;

import com.frigora.frigoraplatform.assetsManagement.domain.model.aggregates.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaSiteRepository extends JpaRepository<Site, Integer> {

    List<Site> findByName(String name);
    List<Site> findByOwnerId(Long ownerId);
}
