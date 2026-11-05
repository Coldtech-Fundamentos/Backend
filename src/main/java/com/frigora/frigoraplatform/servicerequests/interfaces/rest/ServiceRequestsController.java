package com.frigora.frigoraplatform.servicerequests.interfaces.rest;

import com.frigora.frigoraplatform.servicerequests.domain.model.commands.*;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetServiceRequestByIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetServiceRequestsByProviderIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.model.queries.GetServiceRequestsByRequesterIdQuery;
import com.frigora.frigoraplatform.servicerequests.domain.model.valueobjects.ServiceRequestStatus;
import com.frigora.frigoraplatform.servicerequests.domain.services.ServiceRequestCommandService;
import com.frigora.frigoraplatform.servicerequests.domain.services.ServiceRequestQueryService;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources.AssignTechnicianToServiceRequestResource;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources.CreateServiceRequestResource;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.resources.ServiceRequestResource;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.transform.CreateServiceRequestCommandFromResourceAssembler;
import com.frigora.frigoraplatform.servicerequests.interfaces.rest.transform.ServiceRequestResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/service-requests")
@Tag(name = "Service Requests", description = "Available Service Requests Endpoints")
public class ServiceRequestsController {

    private final ServiceRequestCommandService serviceRequestCommandService;
    private final ServiceRequestQueryService serviceRequestQueryService;

    public ServiceRequestsController(ServiceRequestCommandService serviceRequestCommandService,
                                     ServiceRequestQueryService serviceRequestQueryService) {
        this.serviceRequestCommandService = serviceRequestCommandService;
        this.serviceRequestQueryService = serviceRequestQueryService;
    }

    @PostMapping
    @Operation(summary = "Create a Service Request", description = "Creates a new Service Request", operationId = "CreateServiceRequest")
    @ApiResponse(responseCode = "201", description = "The service request was created")
    public ResponseEntity<ServiceRequestResource> createServiceRequest(@RequestBody CreateServiceRequestResource resource) {
        var command = CreateServiceRequestCommandFromResourceAssembler.toCommandFromResource(resource);
        var serviceRequest = serviceRequestCommandService.handle(command);
        if (serviceRequest.isEmpty()) return ResponseEntity.badRequest().build();
        var serviceRequestResource = ServiceRequestResourceFromEntityAssembler.toResourceFromEntity(serviceRequest.get());
        return ResponseEntity.created(URI.create("/api/v1/service-requests/" + serviceRequestResource.id()))
                .body(serviceRequestResource);
    }

    @GetMapping("/{serviceRequestId}")
    @Operation(summary = "Get Service Request by Id", description = "Gets a Service Request by its Id", operationId = "GetServiceRequestById")
    @ApiResponse(responseCode = "200", description = "The service request was found")
    @ApiResponse(responseCode = "404", description = "The service request was not found")
    public ResponseEntity<ServiceRequestResource> getServiceRequestById(@PathVariable int serviceRequestId) {
        var query = new GetServiceRequestByIdQuery(serviceRequestId);
        var serviceRequest = serviceRequestQueryService.handle(query);
        if (serviceRequest.isEmpty()) return ResponseEntity.notFound().build();
        var serviceRequestResource = ServiceRequestResourceFromEntityAssembler.toResourceFromEntity(serviceRequest.get());
        return ResponseEntity.ok(serviceRequestResource);
    }

    @GetMapping("/requester/{requesterId}")
    @Operation(summary = "Get Service Requests by Requester Id", description = "Gets all Service Requests for a given Requester Id", operationId = "GetServiceRequestsByRequesterId")
    @ApiResponse(responseCode = "200", description = "The service requests were found")
    public ResponseEntity<?> getServiceRequestsByRequesterId(@PathVariable int requesterId) {
        var query = new GetServiceRequestsByRequesterIdQuery(requesterId);
        var serviceRequests = serviceRequestQueryService.handle(query);
        var serviceRequestResources = serviceRequests.stream()
                .map(ServiceRequestResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(serviceRequestResources);
    }

    @GetMapping("/provider/{providerId}")
    @Operation(
            summary = "Get Service Requests by Provider Id",
            description = "Gets all Service Requests for a given Provider Id, optionally filtered by status",
            operationId = "GetServiceRequestsByProviderId"
    )
    @ApiResponse(responseCode = "200", description = "The service requests were found")
    public ResponseEntity<?> getServiceRequestsByProviderId(
            @PathVariable int providerId,
            @RequestParam(required = false) String status
    ) {
        System.out.println("ENTRO AL CONTROLLER");

        ServiceRequestStatus.EServiceRequestStatus enumStatus = null;

        if (status != null && !status.isBlank()) {
            enumStatus = ServiceRequestStatus.EServiceRequestStatus.fromJson(status);
        }

        var query = new GetServiceRequestsByProviderIdQuery(providerId, enumStatus);

        var serviceRequests = serviceRequestQueryService.handle(query);

        var serviceRequestResources = serviceRequests.stream()
                .map(ServiceRequestResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(serviceRequestResources);
    }

    @PatchMapping("/{serviceRequestId}/accept")
    @Operation(summary = "Accept a Service Request", description = "Accepts a Service Request by its Id", operationId = "AcceptServiceRequest")
    @ApiResponse(responseCode = "200", description = "The service request was accepted")
    @ApiResponse(responseCode = "404", description = "The service request was not found")
    public ResponseEntity<ServiceRequestResource> acceptServiceRequest(@PathVariable int serviceRequestId) {
        var command = new AcceptServiceRequestCommand(serviceRequestId);
        var serviceRequest = serviceRequestCommandService.handle(command);
        if (serviceRequest.isEmpty()) return ResponseEntity.notFound().build();
        var serviceRequestResource = ServiceRequestResourceFromEntityAssembler.toResourceFromEntity(serviceRequest.get());
        return ResponseEntity.ok(serviceRequestResource);
    }

    @PatchMapping("/{serviceRequestId}/reject")
    @Operation(summary = "Reject a Service Request", description = "Rejects a Service Request by its Id", operationId = "RejectServiceRequest")
    @ApiResponse(responseCode = "200", description = "The service request was rejected")
    @ApiResponse(responseCode = "404", description = "The service request was not found")
    public ResponseEntity<ServiceRequestResource> rejectServiceRequest(@PathVariable int serviceRequestId) {
        var command = new RejectServiceRequestCommand(serviceRequestId);
        var serviceRequest = serviceRequestCommandService.handle(command);
        if (serviceRequest.isEmpty()) return ResponseEntity.notFound().build();
        var serviceRequestResource = ServiceRequestResourceFromEntityAssembler.toResourceFromEntity(serviceRequest.get());
        return ResponseEntity.ok(serviceRequestResource);
    }

    @PatchMapping("/{serviceRequestId}/cancel")
    @Operation(summary = "Cancel a Service Request", description = "Cancels a Service Request by its Id", operationId = "CancelServiceRequest")
    @ApiResponse(responseCode = "200", description = "The service request was canceled")
    @ApiResponse(responseCode = "404", description = "The service request was not found")
    public ResponseEntity<ServiceRequestResource> cancelServiceRequest(@PathVariable int serviceRequestId) {
        var command = new CancelServiceRequestCommand(serviceRequestId);
        var serviceRequest = serviceRequestCommandService.handle(command);
        if (serviceRequest.isEmpty()) return ResponseEntity.notFound().build();
        var serviceRequestResource = ServiceRequestResourceFromEntityAssembler.toResourceFromEntity(serviceRequest.get());
        return ResponseEntity.ok(serviceRequestResource);
    }

    @PatchMapping("/{serviceRequestId}/assign-technician")
    @Operation(summary = "Assign Technician to Service Request", description = "Assigns a technician to a Service Request by its Id", operationId = "AssignTechnicianToServiceRequest")
    @ApiResponse(responseCode = "200", description = "The technician was assigned")
    @ApiResponse(responseCode = "404", description = "The service request was not found")
    public ResponseEntity<ServiceRequestResource> assignTechnicianToServiceRequest(
            @PathVariable int serviceRequestId,
            @RequestBody AssignTechnicianToServiceRequestResource resource) {
        var command = new AssignTechnicianToServiceRequestCommand(serviceRequestId, resource.technicianId());
        var serviceRequest = serviceRequestCommandService.handle(command);
        if (serviceRequest.isEmpty()) return ResponseEntity.notFound().build();
        var serviceRequestResource = ServiceRequestResourceFromEntityAssembler.toResourceFromEntity(serviceRequest.get());
        return ResponseEntity.ok(serviceRequestResource);
    }

    @PatchMapping("/{serviceRequestId}/complete")
    @Operation(summary = "Complete a Service Request", description = "Completes a Service Request by its Id", operationId = "CompleteServiceRequest")
    @ApiResponse(responseCode = "200", description = "The service request was completed")
    @ApiResponse(responseCode = "404", description = "The service request was not found")
    public ResponseEntity<ServiceRequestResource> completeServiceRequest(@PathVariable int serviceRequestId) {
        var command = new CompleteServiceRequestCommand(serviceRequestId);
        var serviceRequest = serviceRequestCommandService.handle(command);
        if (serviceRequest.isEmpty()) return ResponseEntity.notFound().build();
        var serviceRequestResource = ServiceRequestResourceFromEntityAssembler.toResourceFromEntity(serviceRequest.get());
        return ResponseEntity.ok(serviceRequestResource);
    }

    @DeleteMapping("/{serviceRequestId}")
    @Operation(summary = "Delete a Service Request", description = "Deletes a Service Request by its Id", operationId = "DeleteServiceRequest")
    @ApiResponse(responseCode = "200", description = "The service request was deleted")
    @ApiResponse(responseCode = "404", description = "The service request was not found")
    public ResponseEntity<Void> deleteServiceRequest(@PathVariable int serviceRequestId) {

        var command = new DeleteServiceRequestCommand(serviceRequestId);

        boolean deleted = serviceRequestCommandService.handle(command);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}