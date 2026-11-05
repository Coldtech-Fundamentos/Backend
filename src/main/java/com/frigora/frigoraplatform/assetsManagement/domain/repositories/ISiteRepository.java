package com.frigora.frigoraplatform.assetsManagement.domain.repositories;

import com.frigora.frigoraplatform.assetsManagement.domain.model.aggregates.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISiteRepository extends JpaRepository<Site, Integer> {


    List<Site> findByOwnerId(Long ownerId);

}
