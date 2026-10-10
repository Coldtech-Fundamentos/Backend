package com.frigora.frigoraplatform.assetsManagement.domain.model.commands;

public record CreateSiteCommand (String name, String address, String contactName, String phone, Long ownerId){}
