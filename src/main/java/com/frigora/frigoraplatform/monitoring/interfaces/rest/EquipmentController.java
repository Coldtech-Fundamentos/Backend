package com.frigora.frigoraplatform.monitoring.interfaces.rest;

import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.DeleteEquipmentCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetEquipmentByIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetEquipmentsByOwnerQuery;
import com.frigora.frigoraplatform.monitoring.domain.services.EquipmentCommandService;
import com.frigora.frigoraplatform.monitoring.domain.services.EquipmentQueryService;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.resources.CreateEquipmentResource;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.resources.EquipmentResource;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.transform.CreateEquipmentCommandFromResourceAssembler;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.transform.EquipmentResourceFromEntityAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment")
@Tag(name = "Equipment", description = "Equipment management endpoints")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentCommandService equipmentCommandService;
    private final EquipmentQueryService equipmentQueryService;
    private final EquipmentResourceFromEntityAssembler equipmentResourceFromEntityAssembler;
    private final CreateEquipmentCommandFromResourceAssembler
            createEquipmentCommandFromResourceAssembler;

    @GetMapping
    @Operation(summary = "Get all equipments for the current user")
    public ResponseEntity<List<EquipmentResource>> getAll(Authentication authentication) {
        Long ownerId = (long) AuthenticatedUser.requireUserId(authentication);
        var equipments =
                equipmentQueryService.getAllByOwner(new GetEquipmentsByOwnerQuery(ownerId));
        return ResponseEntity.ok(equipmentResourceFromEntityAssembler.toResources(equipments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets a Equipment by Id")
    public ResponseEntity<EquipmentResource> getById(
            @PathVariable Long id, Authentication authentication) {
        Long ownerId = (long) AuthenticatedUser.requireUserId(authentication);
        return equipmentQueryService
                .getById(new GetEquipmentByIdQuery(id, ownerId))
                .map(
                        equipment ->
                                ResponseEntity.ok(
                                        equipmentResourceFromEntityAssembler.toResource(equipment)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-owner/{ownerId}")
    @Operation(summary = "Get all equipments by owner ID")
    public ResponseEntity<List<EquipmentResource>> getByOwner(
            @PathVariable Long ownerId, Authentication authentication) {
        Long authenticatedOwnerId = (long) AuthenticatedUser.requireUserId(authentication);
        if (!authenticatedOwnerId.equals(ownerId)) {
            return ResponseEntity.notFound().build();
        }
        var equipments =
                equipmentQueryService.getAllByOwner(new GetEquipmentsByOwnerQuery(ownerId));
        return ResponseEntity.ok(equipmentResourceFromEntityAssembler.toResources(equipments));
    }

    @PostMapping
    @Operation(summary = "Create a new Equipment")
    public ResponseEntity<EquipmentResource> create(
            @Valid @RequestBody CreateEquipmentResource resource, Authentication authentication) {

        Long ownerId = (long) AuthenticatedUser.requireUserId(authentication);
        var command = createEquipmentCommandFromResourceAssembler.toCommand(resource, ownerId);
        var created = equipmentCommandService.create(command);
        var response = equipmentResourceFromEntityAssembler.toResource(created);
        return ResponseEntity.created(URI.create("/api/v1/equipment/" + response.getId()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Equipment")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long ownerId = (long) AuthenticatedUser.requireUserId(authentication);
        var deleted = equipmentCommandService.delete(new DeleteEquipmentCommand(id, ownerId));
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
