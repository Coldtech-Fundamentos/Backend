package com.frigora.frigoraplatform.assetsmanagement.domain.model.commands;

public record CreateSiteCommand(
        String name, String address, String contactName, String phone, Long ownerId) {}
