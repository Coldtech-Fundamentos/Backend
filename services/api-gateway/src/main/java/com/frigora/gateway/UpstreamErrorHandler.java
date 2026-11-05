package com.frigora.gateway;

import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

// Only transport failures are translated; downstream HTTP responses are passed through.
@Component
@Order(-2)
public class UpstreamErrorHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        HttpStatus status = null;
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof TimeoutException
                    || cause instanceof ReadTimeoutException
                    || cause instanceof ConnectTimeoutException
                    || (cause instanceof ResponseStatusException responseFailure
                            && responseFailure.getStatusCode().value() == 504)) {
                status = HttpStatus.GATEWAY_TIMEOUT;
                break;
            }
            if (cause instanceof ConnectException
                    || cause instanceof UnknownHostException
                    || cause instanceof SocketException
                    || cause instanceof PrematureCloseException) {
                status = HttpStatus.BAD_GATEWAY;
            }
        }
        if (status == null || exchange.getResponse().isCommitted()) {
            return Mono.error(exception);
        }
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body =
                ("{\"status\":"
                                + status.value()
                                + ",\"error\":\""
                                + status.getReasonPhrase()
                                + "\"}")
                        .getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }
}
