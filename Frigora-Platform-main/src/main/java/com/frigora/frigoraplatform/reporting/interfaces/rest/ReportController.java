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
import com.frigora.frigoraplatform.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.frigora.frigoraplatform.iam.domain.model.aggregates.User;
import com.frigora.frigoraplatform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reporting", description = "Available reporting endpoints / Endpoints para la gestión de reportes")
public class ReportController {

    private final ReportCommandService reportCommandService;
    private final ReportQueryService reportQueryService;
    private final BearerTokenService tokenService;
    private final UserRepository userRepository;

    public ReportController(ReportCommandService reportCommandService,
                            ReportQueryService reportQueryService,
                            BearerTokenService tokenService,
                            UserRepository userRepository) {
        this.reportCommandService = reportCommandService;
        this.reportQueryService = reportQueryService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    private Integer extractUserId(HttpServletRequest request) {
        String token = tokenService.getBearerTokenFrom(request);
        String username = tokenService.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return user.getId();
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo reporte")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reporte creado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Error de validación o datos incompletos.")
    })
    public ResponseEntity<ReportResource> createReport(
            @RequestBody CreateReportResource resource,
            HttpServletRequest request) {

        Integer ownerId = extractUserId(request);

        var command = CreateReportCommandFromResourceAssembler.toCommandFromResource(resource, ownerId);

        var report = reportCommandService.handle(command);
        if (report.isEmpty()) return ResponseEntity.badRequest().build();

        var reportResource = ReportResourceFromEntityAssembler.toResourceFromEntity(report.get());
        return new ResponseEntity<>(reportResource, HttpStatus.CREATED);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Obtener un reporte por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte encontrado."),
            @ApiResponse(responseCode = "404", description = "Reporte no encontrado.")
    })
    public ResponseEntity<ReportResource> getReportById(
            @Parameter(description = "Identificador único del reporte", required = true)
            @PathVariable Integer reportId) {
        var report = reportQueryService.handle(new GetReportByIdQuery(reportId));
        if (report.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ReportResourceFromEntityAssembler.toResourceFromEntity(report.get()));
    }

    @GetMapping
    @Operation(summary = "Obtener todos los reportes del usuario actual")
    @ApiResponse(responseCode = "200", description = "Lista de reportes recuperada exitosamente.")
    public ResponseEntity<List<ReportResource>> getAllReports(
            @Parameter(description = "Filtro opcional por equipment ID")
            @RequestParam(required = false) Integer equipmentId,
            HttpServletRequest request) {  // ← MODIFICADO

        Integer ownerId = extractUserId(request);

        List<Report> reports =
                equipmentId != null
                        ? reportQueryService.handle(new GetAllReportsByEquipmentIdQuery(equipmentId))
                        : reportQueryService.handle(new GetAllReportsByTenantIdQuery(ownerId));

        var reportResources = reports.stream()
                .map(ReportResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(reportResources);
    }

    @PutMapping("/{reportId}")
    @Operation(summary = "Actualizar un reporte")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte actualizado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Error al actualizar.")
    })
    public ResponseEntity<ReportResource> updateReport(
            @Parameter(description = "Identificador único del reporte", required = true)
            @PathVariable Integer reportId,
            @RequestBody UpdateReportResource resource,
            HttpServletRequest request) {
        Integer ownerId = extractUserId(request);
        var command = UpdateReportCommandFromResourceAssembler.toCommandFromResource(reportId, resource, ownerId);
        var report = reportCommandService.handle(command);
        if (report.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(ReportResourceFromEntityAssembler.toResourceFromEntity(report.get()));
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Eliminar un reporte")
    @ApiResponse(responseCode = "200", description = "Reporte eliminado exitosamente.")
    public ResponseEntity<?> deleteReport(
            @Parameter(description = "Identificador único del reporte", required = true)
            @PathVariable Integer reportId) {
        reportCommandService.handle(new DeleteReportCommand(reportId));
        return ResponseEntity.ok().build();
    }
}