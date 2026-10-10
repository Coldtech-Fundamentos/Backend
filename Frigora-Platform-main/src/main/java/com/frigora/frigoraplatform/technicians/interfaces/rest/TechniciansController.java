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
@Tag(name = "Technicians", description = "Endpoints para la gestión y administración de técnicos")
public class TechniciansController {

    private final TechnicianCommandService technicianCommandService;
    private final TechnicianQueryService technicianQueryService;

    public TechniciansController(TechnicianCommandService technicianCommandService, TechnicianQueryService technicianQueryService) {
        this.technicianCommandService = technicianCommandService;
        this.technicianQueryService = technicianQueryService;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo técnico", description = "Registra un nuevo técnico en la base de datos.")
    public ResponseEntity<TechnicianResource> createTechnician(@RequestBody CreateTechnicianResource resource) {
        var command = CreateTechnicianCommandFromResourceAssembler.toCommandFromResource(resource);
        var technician = technicianCommandService.handle(command);
        if (technician.isEmpty()) return ResponseEntity.badRequest().build();

        var technicianResource = TechnicianResourceFromEntityAssembler.toResourceFromEntity(technician.get());
        return new ResponseEntity<>(technicianResource, HttpStatus.CREATED);
    }

    @GetMapping("/{technicianId}")
    @Operation(summary = "Obtener un técnico por ID", description = "Devuelve los detalles de un técnico específico utilizando su ID único.")
    public ResponseEntity<TechnicianResource> getTechnicianById(@PathVariable Integer technicianId) {
        var query = new GetTechnicianByIdQuery(technicianId);
        var technician = technicianQueryService.handle(query);
        if (technician.isEmpty()) return ResponseEntity.notFound().build();

        var technicianResource = TechnicianResourceFromEntityAssembler.toResourceFromEntity(technician.get());
        return ResponseEntity.ok(technicianResource);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los técnicos", description = "Devuelve una lista completa de técnicos registrados. Permite filtrar opcionalmente por providerId.")
    public ResponseEntity<List<TechnicianResource>> getAllTechnicians(@RequestParam(required = false) Integer providerId) {
        var technicians = providerId != null
                ? technicianQueryService.handle(new GetTechniciansByProviderIdQuery(providerId))
                : technicianQueryService.handle(new GetAllTechniciansQuery());

        var technicianResources = technicians.stream()
                .map(TechnicianResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(technicianResources);
    }

    @PutMapping("/{technicianId}")
    @Operation(summary = "Actualizar un técnico", description = "Actualiza la información de un técnico existente mediante su ID.")
    public ResponseEntity<TechnicianResource> updateTechnician(@PathVariable Integer technicianId, @RequestBody UpdateTechnicianResource resource) {
        var command = UpdateTechnicianCommandFromResourceAssembler.toCommandFromResource(technicianId, resource);
        var technician = technicianCommandService.handle(command);
        if (technician.isEmpty()) return ResponseEntity.badRequest().build();

        var technicianResource = TechnicianResourceFromEntityAssembler.toResourceFromEntity(technician.get());
        return ResponseEntity.ok(technicianResource);
    }

    @DeleteMapping("/{technicianId}")
    @Operation(summary = "Eliminar un técnico", description = "Elimina de forma permanente un técnico del sistema utilizando su ID.")
    public ResponseEntity<?> deleteTechnician(@PathVariable Integer technicianId) {
        var command = new DeleteTechnicianCommand(technicianId);
        technicianCommandService.handle(command);
        return ResponseEntity.ok().build();
    }
}