package com.frigora.frigoraplatform.assetsManagement.application.internal;

import com.frigora.frigoraplatform.assetsManagement.domain.model.aggregates.Site;
import com.frigora.frigoraplatform.assetsManagement.domain.model.queries.GetAllSitesQuery;
import com.frigora.frigoraplatform.assetsManagement.domain.model.queries.GetSiteByIdQuery;
import com.frigora.frigoraplatform.assetsManagement.domain.model.queries.GetSitesByOwnerQuery;
import com.frigora.frigoraplatform.assetsManagement.domain.repositories.ISiteRepository;
import com.frigora.frigoraplatform.assetsManagement.domain.services.ISiteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Site Query Service
 * <p>
 * This class handles queries related to Site entities.
 * It interacts with the ISiteRepository to retrieve data.
 * </p>
 */

@Service
@RequiredArgsConstructor
public class SiteQueryServiceImlp implements ISiteQueryService {
    private final ISiteRepository siteRepository;

    @Override
    public Optional<Site> handle(GetSiteByIdQuery query) {
        return siteRepository.findById(query.Id());
    }

    @Override
    public List<Site> handle(GetAllSitesQuery query) {
        return siteRepository.findAll();
    }

    @Override
    public List<Site> handle(GetSitesByOwnerQuery query) {
        return siteRepository.findByOwnerId(query.ownerId());  // ← AÑADIDO
    }

}
