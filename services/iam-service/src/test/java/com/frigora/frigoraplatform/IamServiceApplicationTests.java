package com.frigora.frigoraplatform;

import static org.assertj.core.api.Assertions.assertThat;

import com.frigora.frigoraplatform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:iam;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "authorization.jwt.secret=test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz"
})
public class IamServiceApplicationTests {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    @BeforeEach
    void clearUsers() {
        users.deleteAll();
    }

    private HttpResponse<String> post(String operation, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port
                        + "/api/v1/authentication/" + operation))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void registrationLoginRolesAndUserRoutesPreserveContracts() throws Exception {
        for (String role : new String[] {"Owner", "Provider"}) {
            var registration = post("sign-up", """
                    {"username":"test-%s","password":"synthetic-password","roles":"%s"}
                    """.formatted(role, role));
            assertThat(registration.statusCode()).isEqualTo(200);
            assertThat(json.readTree(registration.body()).path("message").asText())
                    .isEqualTo("User created successfully");
            var stored = users.findByUsername("test-" + role).orElseThrow();
            assertThat(stored.getPassword()).startsWith("$2").isNotEqualTo("synthetic-password");
            assertThat(stored.getRole().name()).isEqualTo(role);
            var login = post("sign-in", """
                    {"username":"test-%s","password":"synthetic-password"}
                    """.formatted(role));
            assertThat(login.statusCode()).isEqualTo(200);
            var authenticated = json.readTree(login.body());
            assertThat(authenticated.path("id").asInt()).isEqualTo(stored.getId());
            assertThat(authenticated.path("username").asText()).isEqualTo(stored.getUsername());
            assertThat(authenticated.path("role").asText()).isEqualTo(role);
            String token = authenticated.path("token").asText();
            var byId = get("/api/v1/users/" + stored.getId(), token);
            assertThat(byId.statusCode()).isEqualTo(200);
            assertThat(json.readTree(byId.body()).size()).isEqualTo(2);
            assertThat(get("/api/v1/users", token).statusCode()).isEqualTo(200);
            var filtered = get("/api/v1/users/role/" + role.toLowerCase(), token);
            assertThat(filtered.statusCode()).isEqualTo(200);
            assertThat(json.readTree(filtered.body()).size()).isEqualTo(1);
            assertThat(get("/api/v1/users/role/invalid", token).statusCode()).isEqualTo(400);
            assertThat(get("/api/v1/users/2147483647", token).statusCode()).isEqualTo(404);
            com.frigora.frigoraplatform.shared.infrastructure.security.IamJwtContractAssertions
                    .verify(token, stored.getId());
            MonolithCompatibilityAssertions.verifyNewIamUserIsRejected(token);
        }
        assertThat(get("/api/v1/users", null).statusCode()).isEqualTo(401);
    }

    @Test
    void invalidCredentialsAndDuplicateRegistrationDoNotSucceed() throws Exception {
        String credentials = """
                {"username":"test-owner","password":"synthetic-password","roles":"Owner"}
                """;
        assertThat(post("sign-up", credentials).statusCode()).isEqualTo(200);
        assertThat(post("sign-in", """
                {"username":"test-owner","password":"wrong"}
                """).statusCode()).isEqualTo(401);
        assertThat(post("sign-in", """
                {"username":"missing","password":"wrong"}
                """).statusCode()).isEqualTo(401);
        assertThat(post("sign-up", credentials).statusCode()).isEqualTo(401);
    }
}
