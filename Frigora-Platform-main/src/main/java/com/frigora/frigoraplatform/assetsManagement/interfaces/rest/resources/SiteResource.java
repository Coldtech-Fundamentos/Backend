package com.frigora.frigoraplatform.assetsManagement.interfaces.rest.resources;

import java.time.OffsetDateTime;

public record SiteResource (
        int id,
        String name,
        String address,
        String contactName,
        String phone,
        Long ownerId
){

}
