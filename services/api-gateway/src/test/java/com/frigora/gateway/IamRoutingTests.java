package com.frigora.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IamRoutingTests {
    private static final HttpServer IAM = upstream("iam");
    private static final HttpServer MONOLITH = upstream("monolith");
    private static final HttpServer REPORTING = upstream("reporting");
    @LocalServerPort int port;

    private static HttpServer upstream(String name) {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                byte[] body = exchange.getRequestBody().readAllBytes();
                exchange.getResponseHeaders().set("X-Upstream", name);
                exchange.getResponseHeaders().set("X-Path", exchange.getRequestURI().toString());
                exchange.getResponseHeaders().set("X-Method", exchange.getRequestMethod());
                exchange.getResponseHeaders().set("X-Authorization",
                        exchange.getRequestHeaders().getFirst("Authorization"));
                exchange.sendResponseHeaders(200, body.length == 0 ? -1 : body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    @DynamicPropertySource
    static void destinations(DynamicPropertyRegistry properties) {
        properties.add("IAM_SERVICE_URL", () -> url(IAM));
        properties.add("MONOLITH_URL", () -> url(MONOLITH));
        properties.add("REPORTING_SERVICE_URL", () -> url(REPORTING));
    }

    private static String url(HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    static void close() {
        IAM.stop(0);
        MONOLITH.stop(0);
        REPORTING.stop(0);
    }

    @Test
    void explicitIamDestinationPreservesContractsAndOtherRoutes() throws Exception {
        var client = HttpClient.newHttpClient();
        for (String path : List.of("/api/v1/authentication/sign-in",
                "/api/v1/authentication/sign-up", "/api/v1/users",
                "/api/v1/users/42", "/api/v1/users/role/Owner",
                "/api/v1/reports", "/api/v1/reports/42", "/api/v1/sites/42")) {
            String method = path.contains("authentication") ? "POST" : "GET";
            String body = method.equals("POST") ? "{\"username\":\"synthetic\"}" : "";
            var response = client.send(HttpRequest.newBuilder(
                            URI.create("http://localhost:" + port + path + "?test=1"))
                    .header("Authorization", "Bearer synthetic")
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body)).build(),
                    HttpResponse.BodyHandlers.ofString());
            String destination = path.contains("/reports") ? "reporting"
                    : path.contains("/sites") ? "monolith" : "iam";
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("X-Upstream")).contains(destination);
            assertThat(response.headers().firstValue("X-Path")).contains(path + "?test=1");
            assertThat(response.headers().firstValue("X-Method")).contains(method);
            assertThat(response.headers().firstValue("X-Authorization")).contains("Bearer synthetic");
            assertThat(response.body()).isEqualTo(body);
        }
    }
}
