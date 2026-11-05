package com.frigora.frigoraplatform.technicians.interfaces.rest;

import com.frigora.frigoraplatform.technicians.domain.model.commands.DeleteTechnicianCommand;
import com.frigora.frigoraplatform.technicians.domain.model.queries.GetAllTechniciansQuery;
import com.frigora.frigoraplatform.technicians.domain.model.queries.GetTechnicianByIdQuery;
import com.frigora.frigoraplatform.technicians.domain.model.queries.GetTechniciansByProviderIdQuery;
import com.frigora.frigoraplatform.technicians.domain.services.TechnicianCommandService;
import com.frigora.frigoraplatform.technicians.domain.services.TechnicianQueryService;
import com.frigora.frigoraplatform.technicians.interfaces.rest.resources.CreateTechnicianResource;
import com.frigora.frigoraplatform.technicians.interfaces.rest.resources.TechnicianResource;
import com.frigora.frigoraplatform.technicians.interfaces.rest.resources.UpdateTechnicianResource;
import com.frigora.frigoraplatform.technicians.interfaces.rest.transform.CreateTechnicianCommandFromResourceAssembler;
import com.frigora.frigoraplatform.technicians.interfaces.rest.transform.TechnicianResourceFromEntityAssembler;
import com.frigora.frigoraplatform.technicians.interfaces.rest.transform.UpdateTechnicianCommandFromResourceAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/technicians")
@Tag(name = "Technicians", description = "Technician management endpoints")
public class TechniciansController {

    private final TechnicianCommandService technicianCommandService;
    private final TechnicianQueryService technicianQueryService;

    public TechniciansController(
            TechnicianCommandService technicianCommandService,
            TechnicianQueryService technicianQueryService) {
        this.technicianCommandService = technicianCommandService;
        this.technicianQueryService = technicianQueryService;
    }

    @PostMapping
    @Operation(summary = "Create a technician", description = "Register a technician.")
    public ResponseEntity<TechnicianResource> createTechnician(
            @RequestBody CreateTechnicianResource resource) {
        var command = CreateTechnicianCommandFromResourceAssembler.toCommandFromResource(resource);
        var technician = technicianCommandService.handle(command);
        if (technician.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var technicianResource =
                TechnicianResourceFromEntityAssembler.toResourceFromEntity(technician.get());
        return new ResponseEntity<>(technicianResource, HttpStatus.CREATED);
    }

    @GetMapping("/{technicianId}")
    @Operation(
            summary = "Get a technician by ID",
            description = "Retrieve a technician using its unique ID.")
    public ResponseEntity<TechnicianResource> getTechnicianById(
            @PathVariable Integer technicianId) {
        var query = new GetTechnicianByIdQuery(technicianId);
        var technician = technicianQueryService.handle(query);
        if (technician.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var technicianResource =
                TechnicianResourceFromEntityAssembler.toResourceFromEntity(technician.get());
        return ResponseEntity.ok(technicianResource);
    }

    @GetMapping
    @Operation(
            summary = "Get all technicians",
            description = "List technicians, optionally filtered by provider ID.")
    public ResponseEntity<List<TechnicianResource>> getAllTechnicians(
            @RequestParam(required = false) Integer providerId) {
        var technicians =
                providerId != null
                        ? technicianQueryService.handle(
                                new GetTechniciansByProviderIdQuery(providerId))
                        : technicianQueryService.handle(new GetAllTechniciansQuery());

        var technicianResources =
                technicians.stream()
                        .map(TechnicianResourceFromEntityAssembler::toResourceFromEntity)
                        .toList();

        return ResponseEntity.ok(technicianResources);
    }

    @PutMapping("/{technicianId}")
    @Operation(
            summary = "Update a technician",
            description = "Update an existing technician using its ID.")
    public ResponseEntity<TechnicianResource> updateTechnician(
            @PathVariable Integer technicianId, @RequestBody UpdateTechnicianResource resource) {
        var command =
                UpdateTechnicianCommandFromResourceAssembler.toCommandFromResource(
                        technicianId, resource);
        var technician = technicianCommandService.handle(command);
        if (technician.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var technicianResource =
                TechnicianResourceFromEntityAssembler.toResourceFromEntity(technician.get());
        return ResponseEntity.ok(technicianResource);
    }

    @DeleteMapping("/{technicianId}")
    @Operation(
            summary = "Delete a technician",
            description = "Permanently delete a technician using its ID.")
    public ResponseEntity<?> deleteTechnician(@PathVariable Integer technicianId) {
        var command = new DeleteTechnicianCommand(technicianId);
        technicianCommandService.handle(command);
        return ResponseEntity.ok().build();
    }
}
