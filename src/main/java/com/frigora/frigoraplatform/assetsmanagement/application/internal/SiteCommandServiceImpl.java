package com.frigora.frigoraplatform.assetsmanagement.application.internal;

import com.frigora.frigoraplatform.assetsmanagement.domain.model.aggregates.Site;
import com.frigora.frigoraplatform.assetsmanagement.domain.model.commands.CreateSiteCommand;
import com.frigora.frigoraplatform.assetsmanagement.domain.model.commands.DeleteSiteCommand;
import com.frigora.frigoraplatform.assetsmanagement.domain.repositories.SiteRepository;
import com.frigora.frigoraplatform.assetsmanagement.domain.services.SiteCommandService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteCommandServiceImpl implements SiteCommandService {
    private final SiteRepository siteRepository;

    @Override
    @Transactional
    public Optional<Site> handle(CreateSiteCommand command) {
        Site site = new Site(command);
        try {
            Site savedSite = siteRepository.save(site);
            return Optional.of(savedSite);
        } catch (Exception ex) {
            log.error("Error creating Site: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public boolean handle(DeleteSiteCommand command) {
        Optional<Site> siteOptional = siteRepository.findById(command.siteId());

        if (siteOptional.isEmpty()) {
            return false;
        }

        try {
            siteRepository.delete(siteOptional.get());
            return true;
        } catch (Exception e) {
            log.error("Error deleting Site: {}", e.getMessage());
            return false;
        }
    }
}
