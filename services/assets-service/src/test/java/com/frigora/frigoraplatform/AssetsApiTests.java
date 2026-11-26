package com.frigora.frigoraplatform;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:assets;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=validate",
            "assets.jwt.secret=local-test-secret-that-is-at-least-32-bytes"
        })
@AutoConfigureMockMvc
class AssetsApiTests {
    private static final String SECRET = "local-test-secret-that-is-at-least-32-bytes";

    @Autowired private MockMvc mvc;

    @Test
    void bothAssetRoutesRequireAnIamJwt() throws Exception {
        mvc.perform(get("/api/v1/sites")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/equipment")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/sites").header("Authorization", "Basic dGVzdDp0ZXN0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void siteContractAndOwnerIsolationArePreserved() throws Exception {
        String site =
                mvc.perform(
                                post("/api/v1/sites")
                                        .header("Authorization", bearer(41))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
{"name":"North","address":"1 Main St","contactName":"Alex","phone":"555",
 "ownerId":999}
"""))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").isNumber())
                        .andExpect(jsonPath("$.ownerId").value(41))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        int id = new tools.jackson.databind.ObjectMapper().readTree(site).get("id").intValue();

        mvc.perform(get("/api/v1/sites").header("Authorization", bearer(41)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/v1/sites").header("Authorization", bearer(42)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mvc.perform(get("/api/v1/sites/by-owner/41").header("Authorization", bearer(41)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerId").value(41));
        mvc.perform(get("/api/v1/sites/by-owner/41").header("Authorization", bearer(42)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/sites/" + id).header("Authorization", bearer(42)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/sites/" + id).header("Authorization", bearer(42)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/sites/" + id).header("Authorization", bearer(41)))
                .andExpect(status().isNoContent());
    }

    @Test
    void equipmentContractAndOwnerIsolationArePreserved() throws Exception {
        String equipment =
                mvc.perform(
                                post("/api/v1/equipment")
                                        .header("Authorization", bearer(51))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
{"model":"M1","type":"freezer","serial":"S-51","status":"active",
 "name":"Freezer 1","manufacturer":"Frigora","online":true,
 "ownerId":999}
"""))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").isNumber())
                        .andExpect(jsonPath("$.equipmentId").isString())
                        .andExpect(jsonPath("$.ownerId").value(51))
                        .andExpect(jsonPath("$.status").value("active"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        long id =
                new tools.jackson.databind.ObjectMapper().readTree(equipment).get("id").longValue();

        mvc.perform(get("/api/v1/equipment").header("Authorization", bearer(51)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/v1/equipment").header("Authorization", bearer(52)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mvc.perform(get("/api/v1/equipment/by-owner/51").header("Authorization", bearer(51)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].equipmentId").isString());
        mvc.perform(get("/api/v1/equipment/by-owner/51").header("Authorization", bearer(52)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/equipment/" + id).header("Authorization", bearer(52)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/equipment/" + id).header("Authorization", bearer(52)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/equipment/" + id).header("Authorization", bearer(51)))
                .andExpect(status().isNoContent());
    }

    private static String bearer(int userId) {
        var key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token =
                Jwts.builder()
                        .subject("user-" + userId)
                        .claim("userId", userId)
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + 60_000))
                        .signWith(key)
                        .compact();
        return "Bearer " + token;
    }
}
