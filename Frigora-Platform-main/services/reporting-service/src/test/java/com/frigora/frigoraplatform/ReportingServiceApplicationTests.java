package com.frigora.frigoraplatform;

import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import com.frigora.frigoraplatform.reporting.domain.model.commands.CreateReportCommand;
import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportType;
import com.frigora.frigoraplatform.reporting.infrastructure.persistence.jpa.repositories.JpaReportRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Full independent application and real HTTP/JPA, using H2 only; this does not validate MySQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:reporting;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "reporting.jwt.secret=test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz"
})
class ReportingServiceApplicationTests {
    private static final String SECRET = "test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz";
    private static final String CREATE = """
            {"type":"Inspection","equipmentId":50,"title":"Inspection","summary":"Summary",
             "content":"Content","url":"https://example.test/report"}
            """;
    private static final String UPDATE = """
            {"equipmentId":51,"title":"Updated","status":"Completed","summary":"Summary",
             "content":"Content","url":"https://example.test/report"}
            """;
    @LocalServerPort int port;
    @Autowired JpaReportRepository repository;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    @BeforeEach
    void clearTestDatabase() { repository.deleteAll(); }

    private String token(Object id, String key, Date expiration) {
        var builder = Jwts.builder().subject("owner").issuedAt(new Date());
        if (id != null) builder.claim("userId", id);
        if (expiration != null) builder.expiration(expiration);
        return builder.signWith(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8))).compact();
    }

    private String token(int id) { return token(id, SECRET, Date.from(Instant.now().plusSeconds(120))); }

    private HttpResponse<String> request(String method, String path, String token, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/reports" + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private Report fixture(int owner) {
        return repository.saveAndFlush(new Report(new CreateReportCommand(owner, EReportType.INSPECTION,
                50, "Inspection", "Summary", "Content", "https://example.test/report")));
    }

    @Test
    void fullApplicationStartsAndCreatesReadsUpdatesAndDeletesWithExistingContract() throws Exception {
        var jwt = token(101);
        var created = request("POST", "", jwt, CREATE);
        assertThat(created.statusCode()).isEqualTo(201);
        var resource = json.readTree(created.body());
        assertThat(resource.size()).isEqualTo(9);
        assertThat(resource.get("tenantId").asInt()).isEqualTo(101);
        assertThat(resource.get("type").asText()).isEqualTo("Inspection");
        assertThat(resource.get("status").asText()).isEqualTo("InProgress");
        String path = "/" + resource.get("id").asInt();
        assertThat(request("GET", path, jwt, null).body()).isEqualTo(created.body());
        var updated = request("PUT", path, jwt, UPDATE);
        assertThat(updated.statusCode()).isEqualTo(200);
        assertThat(json.readTree(updated.body()).get("status").asText()).isEqualTo("Completed");
        assertThat(json.readTree(updated.body()).get("tenantId").asInt()).isEqualTo(101);
        var deleted = request("DELETE", path, jwt, null);
        assertThat(deleted.statusCode()).isEqualTo(200);
        assertThat(deleted.body()).isEmpty();
        assertThat(request("GET", path, jwt, null).statusCode()).isEqualTo(404);
    }

    @Test
    void twoUsersCannotReadUpdateOrDeleteEachOthersReports() throws Exception {
        var first = fixture(101);
        var second = fixture(202);
        for (var target : List.of(first, second)) {
            String jwt = token(target.getTenantId() == 101 ? 202 : 101);
            String path = "/" + target.getId();
            assertThat(request("GET", path, jwt, null).statusCode()).isEqualTo(404);
            assertThat(request("PUT", path, jwt, UPDATE).statusCode()).isEqualTo(404);
            assertThat(request("DELETE", path, jwt, null).statusCode()).isEqualTo(404);
        }
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void listAndEquipmentFilterAreScopedToVerifiedClaim() throws Exception {
        fixture(101);
        fixture(202);
        for (int owner : List.of(101, 202)) {
            for (String path : List.of("", "?equipmentId=50")) {
                var response = request("GET", path, token(owner), null);
                assertThat(response.statusCode()).isEqualTo(200);
                var items = json.readTree(response.body());
                assertThat(items.size()).isEqualTo(1);
                assertThat(items.get(0).get("tenantId").asInt()).isEqualTo(owner);
            }
        }
    }

    @Test
    void invalidExpiredMissingAndMalformedClaimsReturn401() throws Exception {
        Date validExpiration = Date.from(Instant.now().plusSeconds(120));
        for (String jwt : List.of(
                token(101, SECRET + "wrong", validExpiration), token(101, SECRET, new Date(0)),
                token(null, SECRET, validExpiration), token("101", SECRET, validExpiration),
                token(-1, SECRET, validExpiration), token(1.5, SECRET, validExpiration),
                token(2147483648L, SECRET, validExpiration), token(101, SECRET, null), "not-a-jwt")) {
            assertThat(request("GET", "", jwt, null).statusCode()).isEqualTo(401);
        }
        assertThat(request("GET", "", null, null).statusCode()).isEqualTo(401);
    }

    @Test
    void auditListenerPopulatesDatesAndPreservesCreationDateOnUpdate() throws Exception {
        var saved = fixture(101);
        var before = repository.findById(saved.getId()).orElseThrow();
        assertThat(before.getAudit().getCreatedDate()).isNotNull();
        assertThat(before.getAudit().getUpdatedDate()).isNotNull();
        var created = before.getAudit().getCreatedDate().toInstant();
        var response = request("PUT", "/" + saved.getId(), token(101), UPDATE);
        assertThat(response.statusCode()).isEqualTo(200);
        var after = repository.findById(saved.getId()).orElseThrow();
        assertThat(after.getAudit().getCreatedDate().toInstant()).isEqualTo(created);
        assertThat(after.getAudit().getUpdatedDate().toInstant()).isAfterOrEqualTo(created);
    }
}
