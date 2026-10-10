package com.frigora.frigoraplatform.assetsManagement.domain.services;

import com.frigora.frigoraplatform.assetsManagement.domain.model.aggregates.Site;
import com.frigora.frigoraplatform.assetsManagement.domain.model.queries.GetAllSitesQuery;
import com.frigora.frigoraplatform.assetsManagement.domain.model.queries.GetSiteByIdQuery;
import com.frigora.frigoraplatform.assetsManagement.domain.model.queries.GetSitesByOwnerQuery;

import java.util.List;
import java.util.Optional;

public interface ISiteQueryService {
    /**
     * Handle the GetSiteByIdQuery
     * <p>
     * This method handles the GetSiteByIdQuery to retrieve a
     * Site entity by its unique identifier.
     * </p>
     *
     * @param query The GetSiteByIdQuery query
     * @return An Optional containing the Site if found; otherwise, empty
     */
    Optional<Site> handle(GetSiteByIdQuery query);

    /**
     * Handle the GetAllSitesQuery
     *
     * @param query The GetAllSitesQuery query
     * @return A list of all Site entities
     */
    List<Site> handle(GetAllSitesQuery query);

    List<Site> handle(GetSitesByOwnerQuery query);
}
