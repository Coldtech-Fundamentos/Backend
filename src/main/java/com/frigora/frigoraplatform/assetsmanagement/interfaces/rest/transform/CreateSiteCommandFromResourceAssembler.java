package com.frigora.frigoraplatform.assetsmanagement.interfaces.rest.transform;

import com.frigora.frigoraplatform.assetsmanagement.domain.model.commands.CreateSiteCommand;
import com.frigora.frigoraplatform.assetsmanagement.interfaces.rest.resources.CreateSiteResource;

public class CreateSiteCommandFromResourceAssembler {
    public static CreateSiteCommand fromResource(CreateSiteResource resource, Long ownerId) {
        return new CreateSiteCommand(
                resource.name(),
                resource.address(),
                resource.contactName(),
                resource.phone(),
                ownerId);
    }
}
