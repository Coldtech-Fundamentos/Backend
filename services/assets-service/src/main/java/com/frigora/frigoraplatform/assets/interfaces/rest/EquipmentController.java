package com.frigora.frigoraplatform.assets.interfaces.rest;

import com.frigora.frigoraplatform.assets.application.EquipmentService;
import com.frigora.frigoraplatform.assets.interfaces.rest.resources.CreateEquipmentResource;
import com.frigora.frigoraplatform.assets.interfaces.rest.resources.EquipmentResource;
import com.frigora.frigoraplatform.assets.interfaces.rest.transform.AssetsResourceAssembler;
import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedUser;

import jakarta.validation.Valid;

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
@RequestMapping("/api/v1/equipment")
public class EquipmentController {
    private final EquipmentService equipment;

    public EquipmentController(EquipmentService equipment) {
        this.equipment = equipment;
    }

    @GetMapping
    public ResponseEntity<List<EquipmentResource>> getAll(Authentication authentication) {
        long ownerId = AuthenticatedUser.requireUserId(authentication);
        return ResponseEntity.ok(
                AssetsResourceAssembler.toEquipmentResources(equipment.findAllForOwner(ownerId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResource> getById(
            @PathVariable long id, Authentication authentication) {
        long ownerId = AuthenticatedUser.requireUserId(authentication);
        return equipment
                .findByIdForOwner(id, ownerId)
                .map(AssetsResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-owner/{ownerId}")
    public ResponseEntity<List<EquipmentResource>> getByOwner(
            @PathVariable long ownerId, Authentication authentication) {
        if (ownerId != AuthenticatedUser.requireUserId(authentication)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(
                AssetsResourceAssembler.toEquipmentResources(equipment.findAllForOwner(ownerId)));
    }

    @PostMapping
    public ResponseEntity<EquipmentResource> create(
            @Valid @RequestBody CreateEquipmentResource resource, Authentication authentication) {
        long ownerId = AuthenticatedUser.requireUserId(authentication);
        var item =
                equipment.create(
                        resource.model(),
                        resource.type(),
                        resource.serial(),
                        resource.status(),
                        resource.installed(),
                        resource.lastSeen(),
                        resource.setPoint(),
                        resource.name(),
                        resource.manufacturer(),
                        resource.online(),
                        ownerId);
        var body = AssetsResourceAssembler.toResource(item);
        return ResponseEntity.created(URI.create("/api/v1/equipment/" + body.id())).body(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id, Authentication authentication) {
        long ownerId = AuthenticatedUser.requireUserId(authentication);
        return equipment.deleteForOwner(id, ownerId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
