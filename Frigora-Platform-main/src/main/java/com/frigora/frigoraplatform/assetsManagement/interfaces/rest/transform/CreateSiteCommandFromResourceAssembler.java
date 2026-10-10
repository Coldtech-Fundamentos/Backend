package com.frigora.frigoraplatform.assetsManagement.interfaces.rest.transform;

import com.frigora.frigoraplatform.assetsManagement.domain.model.commands.CreateSiteCommand;
import com.frigora.frigoraplatform.assetsManagement.interfaces.rest.resources.CreateSiteResource;

public class CreateSiteCommandFromResourceAssembler {
    public static CreateSiteCommand fromResource(CreateSiteResource resource, Long ownerId) {
        return new CreateSiteCommand(
                resource.name(),
                resource.address(),
                resource.contactName(),
                resource.phone(),
                ownerId
        );
    }
}