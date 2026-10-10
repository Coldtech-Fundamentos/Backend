package com.frigora.frigoraplatform.assetsManagement.interfaces.rest.resources;

public record CreateSiteResource(
        String name,
        String address,
        String contactName,
        String phone
) {
}
