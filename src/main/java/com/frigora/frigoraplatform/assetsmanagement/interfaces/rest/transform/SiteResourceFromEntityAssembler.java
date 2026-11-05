package com.frigora.frigoraplatform.assetsmanagement.interfaces.rest.transform;

import com.frigora.frigoraplatform.assetsmanagement.domain.model.aggregates.Site;
import com.frigora.frigoraplatform.assetsmanagement.interfaces.rest.resources.SiteResource;

public class SiteResourceFromEntityAssembler {
    public static SiteResource toResourceFromEntity(Site entity) {
        return new SiteResource(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getContactName(),
                entity.getPhone(),
                entity.getOwnerId());
    }
}
