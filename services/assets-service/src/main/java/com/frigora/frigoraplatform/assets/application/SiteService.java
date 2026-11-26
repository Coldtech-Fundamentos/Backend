package com.frigora.frigoraplatform.assets.application;

import com.frigora.frigoraplatform.assets.domain.model.Site;
import com.frigora.frigoraplatform.assets.infrastructure.persistence.SiteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SiteService {
    private final SiteRepository sites;

    public SiteService(SiteRepository sites) {
        this.sites = sites;
    }

    @Transactional(readOnly = true)
    public List<Site> findAllForOwner(long ownerId) {
        return sites.findAllByOwnerIdOrderByIdAsc(ownerId);
    }

    @Transactional(readOnly = true)
    public Optional<Site> findByIdForOwner(int id, long ownerId) {
        return sites.findByIdAndOwnerId(id, ownerId);
    }

    @Transactional
    public Site create(
            String name, String address, String contactName, String phone, long ownerId) {
        return sites.save(new Site(name, address, contactName, phone, ownerId));
    }

    @Transactional
    public boolean deleteForOwner(int id, long ownerId) {
        return sites.findByIdAndOwnerId(id, ownerId)
                .map(
                        site -> {
                            sites.delete(site);
                            return true;
                        })
                .orElse(false);
    }
}
