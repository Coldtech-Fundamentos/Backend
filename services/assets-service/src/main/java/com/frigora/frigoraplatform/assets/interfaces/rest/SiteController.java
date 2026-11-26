package com.frigora.frigoraplatform.assets.interfaces.rest;

import com.frigora.frigoraplatform.assets.application.SiteService;
import com.frigora.frigoraplatform.assets.interfaces.rest.resources.CreateSiteResource;
import com.frigora.frigoraplatform.assets.interfaces.rest.resources.SiteResource;
import com.frigora.frigoraplatform.assets.interfaces.rest.transform.AssetsResourceAssembler;
import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedUser;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sites")
public class SiteController {
    private final SiteService sites;

    public SiteController(SiteService sites) {
        this.sites = sites;
    }

    @PostMapping
    public ResponseEntity<SiteResource> create(
            @Valid @RequestBody CreateSiteResource resource, Authentication authentication) {
        long ownerId = AuthenticatedUser.requireUserId(authentication);
        var site =
                sites.create(
                        resource.name(),
                        resource.address(),
                        resource.contactName(),
                        resource.phone(),
                        ownerId);
        var body = AssetsResourceAssembler.toResource(site);
        return ResponseEntity.created(URI.create("/api/v1/sites/" + body.id())).body(body);
    }

    @GetMapping
    public ResponseEntity<List<SiteResource>> getAll(Authentication authentication) {
        long ownerId = AuthenticatedUser.requireUserId(authentication);
        return ResponseEntity.ok(
                AssetsResourceAssembler.toSiteResources(sites.findAllForOwner(ownerId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteResource> getById(
            @PathVariable int id, Authentication authentication) {
        long ownerId = AuthenticatedUser.requireUserId(authentication);
        return sites.findByIdForOwner(id, ownerId)
                .map(AssetsResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-owner/{ownerId}")
    public ResponseEntity<List<SiteResource>> getByOwner(
            @PathVariable long ownerId, Authentication authentication) {
        if (ownerId != AuthenticatedUser.requireUserId(authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(
                AssetsResourceAssembler.toSiteResources(sites.findAllForOwner(ownerId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable int id, Authentication authentication) {
        long ownerId = AuthenticatedUser.requireUserId(authentication);
        if (!sites.deleteForOwner(id, ownerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Site with ID " + id + " not found.");
        }
        return ResponseEntity.noContent().build();
    }
}
