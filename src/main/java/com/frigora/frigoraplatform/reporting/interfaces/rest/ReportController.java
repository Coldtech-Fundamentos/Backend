package com.frigora.frigoraplatform.reporting.interfaces.rest;

import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import com.frigora.frigoraplatform.reporting.domain.model.commands.DeleteReportCommand;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetAllReportsByEquipmentIdQuery;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetAllReportsByTenantIdQuery;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetReportByIdQuery;
import com.frigora.frigoraplatform.reporting.domain.services.ReportCommandService;
import com.frigora.frigoraplatform.reporting.domain.services.ReportQueryService;
import com.frigora.frigoraplatform.reporting.interfaces.rest.resources.CreateReportResource;
import com.frigora.frigoraplatform.reporting.interfaces.rest.resources.ReportResource;
import com.frigora.frigoraplatform.reporting.interfaces.rest.resources.UpdateReportResource;
import com.frigora.frigoraplatform.reporting.interfaces.rest.transform.CreateReportCommandFromResourceAssembler;
import com.frigora.frigoraplatform.reporting.interfaces.rest.transform.ReportResourceFromEntityAssembler;
import com.frigora.frigoraplatform.reporting.interfaces.rest.transform.UpdateReportCommandFromResourceAssembler;
import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reporting", description = "Available reporting endpoints")
public class ReportController {

    private final ReportCommandService reportCommandService;
    private final ReportQueryService reportQueryService;

    public ReportController(
            ReportCommandService reportCommandService, ReportQueryService reportQueryService) {
        this.reportCommandService = reportCommandService;
        this.reportQueryService = reportQueryService;
    }

    @PostMapping
    @Operation(summary = "Create a report")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Report created successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid or incomplete report data.")
    })
    public ResponseEntity<ReportResource> createReport(
            @RequestBody CreateReportResource resource, Authentication authentication) {

        Integer ownerId = AuthenticatedUser.requireUserId(authentication);

        var command =
                CreateReportCommandFromResourceAssembler.toCommandFromResource(resource, ownerId);

        var report = reportCommandService.handle(command);
        if (report.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var reportResource = ReportResourceFromEntityAssembler.toResourceFromEntity(report.get());
        return new ResponseEntity<>(reportResource, HttpStatus.CREATED);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get a report by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Report found."),
        @ApiResponse(responseCode = "404", description = "Report not found.")
    })
    public ResponseEntity<ReportResource> getReportById(
            @Parameter(description = "Unique report identifier", required = true) @PathVariable
                    Integer reportId,
            Authentication authentication) {
        int ownerId = AuthenticatedUser.requireUserId(authentication);
        var report = reportQueryService.handle(new GetReportByIdQuery(reportId, ownerId));
        if (report.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(
                ReportResourceFromEntityAssembler.toResourceFromEntity(report.get()));
    }

    @GetMapping
    @Operation(summary = "Get reports owned by the current user")
    @ApiResponse(responseCode = "200", description = "Reports retrieved successfully.")
    public ResponseEntity<List<ReportResource>> getAllReports(
            @Parameter(description = "Optional equipment ID filter") @RequestParam(required = false)
                    Integer equipmentId,
            Authentication authentication) {

        Integer ownerId = AuthenticatedUser.requireUserId(authentication);

        List<Report> reports =
                equipmentId != null
                        ? reportQueryService.handle(
                                new GetAllReportsByEquipmentIdQuery(equipmentId, ownerId))
                        : reportQueryService.handle(new GetAllReportsByTenantIdQuery(ownerId));

        var reportResources =
                reports.stream()
                        .map(ReportResourceFromEntityAssembler::toResourceFromEntity)
                        .toList();

        return ResponseEntity.ok(reportResources);
    }

    @PutMapping("/{reportId}")
    @Operation(summary = "Update a report")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Report updated successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid update data.")
    })
    public ResponseEntity<ReportResource> updateReport(
            @Parameter(description = "Unique report identifier", required = true) @PathVariable
                    Integer reportId,
            @RequestBody UpdateReportResource resource,
            Authentication authentication) {
        Integer ownerId = AuthenticatedUser.requireUserId(authentication);
        var command =
                UpdateReportCommandFromResourceAssembler.toCommandFromResource(
                        reportId, resource, ownerId);
        var report = reportCommandService.handle(command);
        if (report.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(
                ReportResourceFromEntityAssembler.toResourceFromEntity(report.get()));
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete a report")
    @ApiResponse(responseCode = "200", description = "Report deleted successfully.")
    public ResponseEntity<?> deleteReport(
            @Parameter(description = "Unique report identifier", required = true) @PathVariable
                    Integer reportId,
            Authentication authentication) {
        int ownerId = AuthenticatedUser.requireUserId(authentication);
        if (!reportCommandService.handle(new DeleteReportCommand(reportId, ownerId))) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
