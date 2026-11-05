package com.frigora.frigoraplatform.assetsManagement.interfaces;

import com.frigora.frigoraplatform.assetsManagement.domain.model.commands.CreateSiteCommand;
import com.frigora.frigoraplatform.assetsManagement.domain.model.commands.DeleteSiteCommand;
import com.frigora.frigoraplatform.assetsManagement.domain.model.queries.GetSiteByIdQuery;
import com.frigora.frigoraplatform.assetsManagement.domain.model.queries.GetSitesByOwnerQuery;
import com.frigora.frigoraplatform.assetsManagement.domain.services.ISiteCommandService;
import com.frigora.frigoraplatform.assetsManagement.domain.services.ISiteQueryService;
import com.frigora.frigoraplatform.assetsManagement.interfaces.rest.resources.CreateSiteResource;
import com.frigora.frigoraplatform.assetsManagement.interfaces.rest.resources.SiteResource;
import com.frigora.frigoraplatform.assetsManagement.interfaces.rest.transform.CreateSiteCommandFromResourceAssembler;
import com.frigora.frigoraplatform.assetsManagement.interfaces.rest.transform.SiteResourceFromEntityAssembler;
import com.frigora.frigoraplatform.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.frigora.frigoraplatform.iam.domain.model.aggregates.User;
import com.frigora.frigoraplatform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sites")
@Tag(name = "Site", description = "Endpoints for managing physical sites or locations where refrigeration assets are deployed")
@RequiredArgsConstructor
public class SiteController {

	private final ISiteCommandService siteCommandService;
	private final ISiteQueryService siteQueryService;
	private final BearerTokenService tokenService;
	private final UserRepository userRepository;

	private Long extractUserId(HttpServletRequest request) {
		String token = tokenService.getBearerTokenFrom(request);
		String username = tokenService.getUsernameFromToken(token);
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found: " + username));
		return (long) user.getId();
	}

	@PostMapping
	@Operation(summary = "Create a new Site", operationId = "CreateSite")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Site created successfully"),
			@ApiResponse(responseCode = "400", description = "Bad request due to invalid data format"),
			@ApiResponse(responseCode = "409", description = "Conflict: A site with the same Contact Name already exists")
	})
	public ResponseEntity<?> createSite(
			@Valid @RequestBody CreateSiteResource resource,
			HttpServletRequest request) {

		Long ownerId = extractUserId(request);
		CreateSiteCommand command = CreateSiteCommandFromResourceAssembler.fromResource(resource, ownerId);

		try {
			var result = siteCommandService.handle(command);

			if (result.isEmpty()) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body("A site with the same Contact Name already exists.");
			}

			SiteResource response = SiteResourceFromEntityAssembler.toResourceFromEntity(result.get());
			URI location = ServletUriComponentsBuilder.fromCurrentRequest()
					.path("/{id}")
					.buildAndExpand(response.id())
					.toUri();

			return ResponseEntity.created(location).body(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/{id}")
	@Operation(summary = "Gets a Site by Id", operationId = "GetSiteById")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Site found"),
			@ApiResponse(responseCode = "404", description = "Site not found")
	})
	public ResponseEntity<SiteResource> getSiteById(
			@Parameter(description = "The unique identifier of the site", required = true)
			@PathVariable int id) {
		var result = siteQueryService.handle(new GetSiteByIdQuery(id));
		return result
				.map(site -> ResponseEntity.ok(SiteResourceFromEntityAssembler.toResourceFromEntity(site)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping
	@Operation(summary = "Get all sites for the current user", operationId = "GetAllSites")
	@ApiResponse(responseCode = "200", description = "List of sites retrieved successfully")
	public ResponseEntity<List<SiteResource>> getAllSites(HttpServletRequest request) {
		Long ownerId = extractUserId(request);
		var results = siteQueryService.handle(new GetSitesByOwnerQuery(ownerId));
		var resources = results.stream()
				.map(SiteResourceFromEntityAssembler::toResourceFromEntity)
				.toList();
		return ResponseEntity.ok(resources);
	}
	
	@GetMapping("/by-owner/{ownerId}")
	@Operation(summary = "Get all sites by owner ID", operationId = "GetSitesByOwner")
	@ApiResponse(responseCode = "200", description = "List of sites retrieved successfully")
	public ResponseEntity<List<SiteResource>> getSitesByOwner(
			@Parameter(description = "The owner ID", required = true)
			@PathVariable Long ownerId) {
		var results = siteQueryService.handle(new GetSitesByOwnerQuery(ownerId));
		var resources = results.stream()
				.map(SiteResourceFromEntityAssembler::toResourceFromEntity)
				.toList();
		return ResponseEntity.ok(resources);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete Site", operationId = "DeleteSite")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Site deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Site not found")
	})
	public ResponseEntity<String> deleteSite(
			@Parameter(description = "The unique identifier of the site to be deleted", required = true)
			@PathVariable int id) {
		boolean deleted = siteCommandService.handle(new DeleteSiteCommand(id));
		if (!deleted) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("Site with ID " + id + " not found.");
		}
		return ResponseEntity.noContent().build();
	}
}