package com.frigora.frigoraplatform.iam.interfaces.rest;

import com.frigora.frigoraplatform.iam.domain.model.queries.GetAllUsersQuery;
import com.frigora.frigoraplatform.iam.domain.model.queries.GetUserByIdQuery;
import com.frigora.frigoraplatform.iam.domain.model.queries.GetUserByRoleQuery;
import com.frigora.frigoraplatform.iam.domain.model.valueobjects.Roles;
import com.frigora.frigoraplatform.iam.domain.services.UserQueryService;
import com.frigora.frigoraplatform.iam.interfaces.rest.resources.UserResource;
import com.frigora.frigoraplatform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Available Users Endpoints")
public class UsersController {

    private final UserQueryService userQueryService;

    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by its id", description = "Get a user by its id", operationId = "GetUserById")
    @ApiResponse(responseCode = "200", description = "The user was found")
    public ResponseEntity<UserResource> getUserById(@PathVariable Long id) {
        var query = new GetUserByIdQuery(id);
        var user = userQueryService.handle(query);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Get all users", operationId = "GetAllUsers")
    @ApiResponse(responseCode = "200", description = "The users were found")
    public ResponseEntity<?> getAllUsers() {
        var query = new GetAllUsersQuery();
        var users = userQueryService.handle(query);
        var userResources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(userResources);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Get users by role", operationId = "GetUsersByRole")
    @ApiResponse(responseCode = "200", description = "The users were found")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        Roles userRole;
        try {
            userRole = Arrays.stream(Roles.values())
                    .filter(r -> r.name().equalsIgnoreCase(role))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid role"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role: " + role);
        }
        var query = new GetUserByRoleQuery(userRole);
        var users = userQueryService.handle(query);
        var userResources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(userResources);
    }
}
