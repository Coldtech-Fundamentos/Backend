package com.frigora.frigoraplatform.assetsmanagement.domain.repositories;

import com.frigora.frigoraplatform.assetsmanagement.domain.model.aggregates.Site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    List<Site> findByOwnerId(Long ownerId);
}
