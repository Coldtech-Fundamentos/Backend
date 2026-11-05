package com.frigora.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

// Real gateway on a random port and disposable HTTP upstreams; no business services or DB needed.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayTests {
    private static final ExecutorService UPSTREAM_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();
    private static final BlockingQueue<RecordedRequest> REPORTS = new LinkedBlockingQueue<>();
    private static final BlockingQueue<RecordedRequest> MONOLITH = new LinkedBlockingQueue<>();
    private static HttpServer REPORTING_SERVER = start("reporting", REPORTS);
    private static final HttpServer MONOLITH_SERVER = start("monolith", MONOLITH);

    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newHttpClient();

    private record RecordedRequest(
            String method, String path, String query, String authorization, String body) {}

    private static HttpServer start(String name, BlockingQueue<RecordedRequest> requests) {
        return start(name, requests, 0);
    }

    private static HttpServer start(
            String name, BlockingQueue<RecordedRequest> requests, int port) {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
            server.setExecutor(UPSTREAM_EXECUTOR);
            server.createContext("/", exchange -> respond(exchange, name, requests));
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void respond(
            HttpExchange exchange, String name, BlockingQueue<RecordedRequest> requests)
            throws IOException {
        byte[] body = exchange.getRequestBody().readAllBytes();
        requests.add(
                new RecordedRequest(
                        exchange.getRequestMethod(),
                        exchange.getRequestURI().getPath(),
                        exchange.getRequestURI().getRawQuery(),
                        exchange.getRequestHeaders().getFirst("Authorization"),
                        new String(body, StandardCharsets.UTF_8)));
        exchange.getResponseHeaders().set("X-Upstream", name);
        if (exchange.getRequestURI().getPath().endsWith("/disconnect")) {
            exchange.close();
            return;
        }
        if (exchange.getRequestURI().getPath().endsWith("/slow")) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                exchange.close();
                return;
            }
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        // Model the monolith's existing CORS so deduplication is exercised.
        if (exchange.getRequestHeaders().getFirst("Origin") != null) {
            exchange.getResponseHeaders()
                    .set("Access-Control-Allow-Origin", "http://localhost:4200");
            exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        }
        int status = exchange.getRequestURI().getPath().endsWith("/missing") ? 404 : 200;
        String query = exchange.getRequestURI().getRawQuery();
        for (int candidate : List.of(401, 403, 404)) {
            if (("responseStatus=" + candidate).equals(query)) {
                status = candidate;
            }
        }
        if (status == 401) {
            exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
        }
        byte[] response = body.length == 0 ? "{}".getBytes(StandardCharsets.UTF_8) : body;
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    @DynamicPropertySource
    static void destinations(DynamicPropertyRegistry properties) {
        properties.add(
                "spring.cloud.gateway.server.webflux.httpclient.response-timeout", () -> "1s");
        properties.add(
                "REPORTING_SERVICE_URL",
                () -> "http://127.0.0.1:" + REPORTING_SERVER.getAddress().getPort());
        properties.add(
                "MONOLITH_URL", () -> "http://127.0.0.1:" + MONOLITH_SERVER.getAddress().getPort());
    }

    @BeforeEach
    void clearRequests() {
        REPORTS.clear();
        MONOLITH.clear();
    }

    @AfterAll
    static void closeServers() {
        REPORTING_SERVER.stop(0);
        MONOLITH_SERVER.stop(0);
        UPSTREAM_EXECUTOR.shutdownNow();
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15));
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void reportingHasPriorityAndPreservesPrefixQueryAuthorizationMethodAndBody() throws Exception {
        for (String method : List.of("GET", "POST", "PUT", "DELETE")) {
            for (String path : List.of("/api/v1/reports", "/api/v1/reports/123")) {
                String body =
                        method.equals("POST") || method.equals("PUT")
                                ? "{\"title\":\"Example\"}"
                                : "";
                var response =
                        send(
                                request(path + "?equipmentId=50")
                                        .header("Authorization", "Bearer test-token")
                                        .header("Content-Type", "application/json")
                                        .method(method, HttpRequest.BodyPublishers.ofString(body)));
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.headers().firstValue("X-Upstream")).contains("reporting");
                assertThat(response.body()).isEqualTo(body.isEmpty() ? "{}" : body);
                assertThat(REPORTS.poll(2, TimeUnit.SECONDS))
                        .isEqualTo(
                                new RecordedRequest(
                                        method, path, "equipmentId=50", "Bearer test-token", body));
                assertThat(MONOLITH).isEmpty();
            }
        }
    }

    @Test
    void otherModulesAndAuthenticationGoToMonolithWithOriginalMethodsAndHeaders() throws Exception {
        for (String method : List.of("GET", "POST", "PUT", "DELETE")) {
            String path = "/api/v1/sites/123";
            String body = "{\"name\":\"Site\"}";
            var response =
                    send(
                            request(path)
                                    .header("Authorization", "Bearer forwarded")
                                    .header("Content-Type", "application/json")
                                    .method(method, HttpRequest.BodyPublishers.ofString(body)));
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("X-Upstream")).contains("monolith");
            assertThat(MONOLITH.poll(2, TimeUnit.SECONDS))
                    .isEqualTo(new RecordedRequest(method, path, null, "Bearer forwarded", body));
        }
        assertThat(
                        send(request("/api/v1/authentication/sign-in")
                                        .POST(HttpRequest.BodyPublishers.ofString("{}")))
                                .headers()
                                .firstValue("X-Upstream"))
                .contains("monolith");
        assertThat(REPORTS).isEmpty();
    }

    @Test
    void allowedPreflightIsHandledWithoutCallingUpstreams() throws Exception {
        for (String path : List.of("/api/v1/reports", "/api/v1/sites")) {
            var response =
                    send(
                            request(path)
                                    .header("Origin", "http://localhost:4200")
                                    .header("Access-Control-Request-Method", "PUT")
                                    .header(
                                            "Access-Control-Request-Headers",
                                            "authorization,content-type")
                                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody()));
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
                    .contains("http://localhost:4200");
            assertThat(response.headers().firstValue("Access-Control-Allow-Methods").orElseThrow())
                    .contains("PUT");
        }
        assertThat(REPORTS).isEmpty();
        assertThat(MONOLITH).isEmpty();
    }

    @Test
    void disallowedOriginAndHeadersAreRejected() throws Exception {
        assertThat(
                        send(request("/api/v1/reports")
                                        .header("Origin", "https://untrusted.test")
                                        .GET())
                                .statusCode())
                .isEqualTo(403);
        assertThat(
                        send(request("/api/v1/sites")
                                        .header("Origin", "http://localhost:4200")
                                        .header("Access-Control-Request-Method", "POST")
                                        .header("Access-Control-Request-Headers", "X-Unapproved")
                                        .method("OPTIONS", HttpRequest.BodyPublishers.noBody()))
                                .statusCode())
                .isEqualTo(403);
        assertThat(REPORTS).isEmpty();
        assertThat(MONOLITH).isEmpty();
    }

    @Test
    void actualCorsResponseDoesNotDuplicateDownstreamHeaders() throws Exception {
        var response =
                send(request("/api/v1/sites").header("Origin", "http://localhost:4200").GET());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().allValues("Access-Control-Allow-Origin"))
                .containsExactly("http://localhost:4200");
        assertThat(response.headers().allValues("Access-Control-Allow-Credentials"))
                .containsExactly("true");
    }

    @Test
    void unavailableUpstreamReturns502WithoutInternalDetails() throws Exception {
        int upstreamPort = REPORTING_SERVER.getAddress().getPort();
        REPORTING_SERVER.stop(0);
        try {
            var response = send(request("/api/v1/reports").GET());
            assertThat(response.statusCode()).isEqualTo(502);
            assertThat(response.body()).isEqualTo("{\"status\":502,\"error\":\"Bad Gateway\"}");
            assertThat(MONOLITH).isEmpty();
        } finally {
            REPORTING_SERVER = start("reporting", REPORTS, upstreamPort);
        }
    }

    @Test
    void slowUpstreamReturns504WithoutFallback() throws Exception {
        var response = send(request("/api/v1/reports/slow").GET());
        assertThat(response.statusCode()).isEqualTo(504);
        assertThat(response.body()).isEqualTo("{\"status\":504,\"error\":\"Gateway Timeout\"}");
        assertThat(MONOLITH).isEmpty();
    }

    @Test
    void prematureDisconnectReturns502WithoutFallback() throws Exception {
        var response = send(request("/api/v1/reports/disconnect").GET());
        assertThat(response.statusCode()).isEqualTo(502);
        assertThat(response.body()).isEqualTo("{\"status\":502,\"error\":\"Bad Gateway\"}");
        assertThat(MONOLITH).isEmpty();
    }

    @Test
    void downstream404IsPreservedAndUnknownGatewayPathsAreNotForwarded() throws Exception {
        assertThat(send(request("/api/v1/reports/missing").GET()).statusCode()).isEqualTo(404);
        assertThat(REPORTS.poll(2, TimeUnit.SECONDS)).isNotNull();
        assertThat(send(request("/outside-api").GET()).statusCode()).isEqualTo(404);
        assertThat(MONOLITH).isEmpty();
    }

    @Test
    void routesEveryActualMonolithContextAndItsQueryParametersWithoutRewriting() throws Exception {
        // Paths taken from real controllers. Upstream responses remain simulated in this test.
        for (String path :
                List.of(
                        "/api/v1/users/123",
                        "/api/v1/sites/by-owner/123",
                        "/api/v1/equipment/by-owner/123",
                        "/api/v1/alert?equipmentId=50",
                        "/api/v1/service-requests/provider/123?status=Pending",
                        "/api/v1/interventions/serviceRequest/123",
                        "/api/v1/technicians?providerId=123",
                        "/api/v1/reviews/service-request/123")) {
            var response =
                    send(request(path).header("Authorization", "Bearer context-token").GET());
            assertThat(response.statusCode()).isEqualTo(200);
            var uri = URI.create(path);
            assertThat(MONOLITH.poll(2, TimeUnit.SECONDS))
                    .isEqualTo(
                            new RecordedRequest(
                                    "GET",
                                    uri.getPath(),
                                    uri.getRawQuery(),
                                    "Bearer context-token",
                                    ""));
        }
        var patch =
                send(
                        request("/api/v1/service-requests/123/assign-technician")
                                .header("Authorization", "Bearer patch-token")
                                .header("Content-Type", "application/json")
                                .method(
                                        "PATCH",
                                        HttpRequest.BodyPublishers.ofString(
                                                "{\"technicianId\":456}")));
        assertThat(patch.statusCode()).isEqualTo(200);
        assertThat(MONOLITH.poll(2, TimeUnit.SECONDS))
                .isEqualTo(
                        new RecordedRequest(
                                "PATCH",
                                "/api/v1/service-requests/123/assign-technician",
                                null,
                                "Bearer patch-token",
                                "{\"technicianId\":456}"));
        assertThat(REPORTS).isEmpty();
    }

    @Test
    void preserves401403404ResponseStatusBodyAndAuthenticationChallengeFromBothDestinations()
            throws Exception {
        for (String path : List.of("/api/v1/users/123", "/api/v1/reports/123")) {
            for (int status : List.of(401, 403, 404)) {
                var response =
                        send(
                                request(path + "?responseStatus=" + status)
                                        .header("Authorization", "Bearer rejected-token")
                                        .GET());
                assertThat(response.statusCode()).isEqualTo(status);
                assertThat(response.body()).isEqualTo("{}");
                if (status == 401) {
                    assertThat(response.headers().firstValue("WWW-Authenticate"))
                            .contains("Bearer");
                }
            }
        }
        assertThat(MONOLITH).hasSize(3);
        assertThat(REPORTS).hasSize(3);
    }
}
