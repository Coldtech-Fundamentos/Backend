package com.frigora.frigoraplatform.servicerequests.interfaces.rest;

import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetInterventionByIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetInterventionsByServiceRequestIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.services.InterventionCommandService;
import com.frigora.frigoraplatform.servicerequests.domain.services.InterventionQueryService;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources.InterventionResource;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources.RecordInterventionResource;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.transform.InterventionResourceFromEntityAssembler;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.transform.RecordInterventionCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/interventions")
@Tag(name = "Interventions", description = "Available Interventions Endpoints")
public class InterventionsController {

    private final InterventionCommandService interventionCommandService;
    private final InterventionQueryService interventionQueryService;

    public InterventionsController(InterventionCommandService interventionCommandService,
                                   InterventionQueryService interventionQueryService) {
        this.interventionCommandService = interventionCommandService;
        this.interventionQueryService = interventionQueryService;
    }

    @PostMapping
    @Operation(summary = "Record an Intervention",
            description = "Records a new Intervention for a Service Request",
            operationId = "RecordIntervention")
    @ApiResponse(responseCode = "201", description = "The intervention was recorded")
    public ResponseEntity<InterventionResource> recordIntervention(@RequestBody RecordInterventionResource resource) {
        var command = RecordInterventionCommandFromResourceAssembler.toCommandFromResource(resource);
        var intervention = interventionCommandService.handle(command);
        if (intervention.isEmpty()) return ResponseEntity.badRequest().build();
        var interventionResource = InterventionResourceFromEntityAssembler.toResourceFromEntity(intervention.get());
        return ResponseEntity.created(URI.create("/api/v1/interventions/" + interventionResource.id()))
                .body(interventionResource);
    }

    @GetMapping("/{interventionId}")
    @Operation(summary = "Get Intervention by Id",
            description = "Gets an Intervention by its Id",
            operationId = "GetInterventionById")
    @ApiResponse(responseCode = "200", description = "The intervention was found")
    @ApiResponse(responseCode = "404", description = "The intervention was not found")
    public ResponseEntity<InterventionResource> getInterventionById(@PathVariable int interventionId) {
        var query = new GetInterventionByIdQuery(interventionId);
        var intervention = interventionQueryService.handle(query);
        if (intervention.isEmpty()) return ResponseEntity.notFound().build();
        var interventionResource = InterventionResourceFromEntityAssembler.toResourceFromEntity(intervention.get());
        return ResponseEntity.ok(interventionResource);
    }

    @GetMapping("/serviceRequest/{serviceRequestId}")
    @Operation(summary = "Get Interventions by Service Request Id",
            description = "Gets all Interventions for a given Service Request Id",
            operationId = "GetInterventionsByServiceRequestId")
    @ApiResponse(responseCode = "200", description = "The interventions were found")
    public ResponseEntity<?> getInterventionsByServiceRequestId(@PathVariable int serviceRequestId) {
        var query = new GetInterventionsByServiceRequestIdQuery(serviceRequestId);
        var interventions = interventionQueryService.handle(query);
        var interventionResources = interventions.stream()
                .map(InterventionResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(interventionResources);
    }
}