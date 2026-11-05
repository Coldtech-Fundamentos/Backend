package com.frigora.frigoraplatform.assetsmanagement.interfaces.rest.resources;

public record SiteResource(
        int id, String name, String address, String contactName, String phone, Long ownerId) {}
