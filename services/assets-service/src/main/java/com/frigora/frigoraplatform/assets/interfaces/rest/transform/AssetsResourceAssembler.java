package com.frigora.frigoraplatform.assets.interfaces.rest.transform;

import com.frigora.frigoraplatform.assets.domain.model.Equipment;
import com.frigora.frigoraplatform.assets.domain.model.Site;
import com.frigora.frigoraplatform.assets.interfaces.rest.resources.EquipmentResource;
import com.frigora.frigoraplatform.assets.interfaces.rest.resources.SiteResource;

import java.util.List;

public final class AssetsResourceAssembler {
    private AssetsResourceAssembler() {}

    public static SiteResource toResource(Site site) {
        return new SiteResource(
                site.getId(),
                site.getName(),
                site.getAddress(),
                site.getContactName(),
                site.getPhone(),
                site.getOwnerId());
    }

    public static EquipmentResource toResource(Equipment item) {
        return new EquipmentResource(
                item.getId(),
                item.getEquipmentId(),
                item.getModel(),
                item.getType(),
                item.getSerial(),
                item.getStatus(),
                item.getInstalled(),
                item.getLastSeen(),
                item.getSetPoint(),
                item.getName(),
                item.getManufacturer(),
                item.getOnline(),
                item.getCreated(),
                item.getUpdated(),
                item.getOwnerId());
    }

    public static List<SiteResource> toSiteResources(List<Site> sites) {
        return sites.stream().map(AssetsResourceAssembler::toResource).toList();
    }

    public static List<EquipmentResource> toEquipmentResources(List<Equipment> items) {
        return items.stream().map(AssetsResourceAssembler::toResource).toList();
    }
}
