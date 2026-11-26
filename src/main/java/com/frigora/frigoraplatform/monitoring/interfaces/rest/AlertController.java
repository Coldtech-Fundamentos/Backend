package com.frigora.frigoraplatform.monitoring.interfaces.rest;

import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.AcknowledgeAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.commands.DeleteAlertCommand;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAlertByIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByEquipmentIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.model.queries.GetAllAlertsByTenantIdQuery;
import com.frigora.frigoraplatform.monitoring.domain.services.AlertCommandService;
import com.frigora.frigoraplatform.monitoring.domain.services.AlertQueryService;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.resources.AlertResource;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.resources.CreateAlertResource;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.transform.AlertResourceFromEntityAssembler;
import com.frigora.frigoraplatform.monitoring.interfaces.rest.transform.CreateAlertCommandFromResourceAssembler;

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
@RequestMapping("/api/v1/alert")
@Tag(name = "Alert", description = "Alert management endpoints")
@RequiredArgsConstructor
public class AlertController {

    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;
    private final AlertResourceFromEntityAssembler alertResourceFromEntityAssembler;
    private final CreateAlertCommandFromResourceAssembler createAlertCommandFromResourceAssembler;

    @GetMapping
    @Operation(summary = "Gets all Alerts for the current user or filtered by equipmentId")
    public ResponseEntity<List<AlertResource>> getAll(
            @RequestParam(required = false) Long equipmentId, Authentication authentication) {
        Long ownerId = (long) AuthenticatedUser.requireUserId(authentication);
        var alerts =
                equipmentId != null
                        ? alertQueryService.getAllByEquipmentId(
                                new GetAllAlertsByEquipmentIdQuery(equipmentId)).stream()
                                .filter(alert -> ownerId.equals(alert.getTenantId()))
                                .toList()
                        : alertQueryService.getAllByTenantId(
                                new GetAllAlertsByTenantIdQuery(ownerId));

        return ResponseEntity.ok(alertResourceFromEntityAssembler.toResources(alerts));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets Alert by Id")
    public ResponseEntity<AlertResource> getById(@PathVariable Long id) {
        return alertQueryService
                .getById(new GetAlertByIdQuery(id))
                .map(alert -> ResponseEntity.ok(alertResourceFromEntityAssembler.toResource(alert)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new Alert")
    public ResponseEntity<AlertResource> create(
            @Valid @RequestBody CreateAlertResource resource, Authentication authentication) {
        Long ownerId = (long) AuthenticatedUser.requireUserId(authentication);
        var command = createAlertCommandFromResourceAssembler.toCommand(resource, ownerId);
        var created = alertCommandService.create(command);
        var response = alertResourceFromEntityAssembler.toResource(created);
        return ResponseEntity.created(URI.create("/api/v1/alert/" + response.getId()))
                .body(response);
    }

    @PatchMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledges an Alert")
    public ResponseEntity<AlertResource> acknowledge(@PathVariable Long id) {
        return alertCommandService
                .acknowledge(new AcknowledgeAlertCommand(id))
                .map(alert -> ResponseEntity.ok(alertResourceFromEntityAssembler.toResource(alert)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Alert")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        var deleted = alertCommandService.delete(new DeleteAlertCommand(id));
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
