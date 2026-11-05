package com.frigora.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import io.netty.channel.ConnectTimeoutException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class UpstreamErrorHandlerTests {
    private final UpstreamErrorHandler handler = new UpstreamErrorHandler();

    @Test
    void nestedConnectionTimeoutReturns504() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/reports"));
        handler.handle(
                        exchange,
                        new IllegalStateException(new ConnectTimeoutException("private host")))
                .block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(exchange.getResponse().getBodyAsString().block())
                .isEqualTo("{\"status\":504,\"error\":\"Gateway Timeout\"}");
    }

    @Test
    void unrelatedErrorsRemainAvailableToOtherHandlers() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/reports"));
        var failure = new IllegalArgumentException("unrelated");
        assertThat(handler.handle(exchange, failure).materialize().block().getThrowable())
                .isSameAs(failure);
    }

    @Test
    void committedResponsesAreNotRewritten() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/reports"));
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().setComplete().block();
        var failure = new ConnectTimeoutException("private host");
        assertThat(handler.handle(exchange, failure).materialize().block().getThrowable())
                .isSameAs(failure);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
