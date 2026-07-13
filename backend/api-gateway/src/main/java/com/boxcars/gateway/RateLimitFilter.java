package com.boxcars.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Simple in-memory rate limit — Phase 3 hardening (per IP, 120 req/min). */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int LIMIT = 120;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        } else {
            ip = ip.split(",")[0].trim();
        }

        Window window = windows.computeIfAbsent(ip, k -> new Window());
        long now = Instant.now().getEpochSecond();
        if (now - window.epochSecond >= 60) {
            window.epochSecond = now;
            window.count.set(0);
        }
        if (window.count.incrementAndGet() > LIMIT) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -50;
    }

    private static final class Window {
        volatile long epochSecond = Instant.now().getEpochSecond();
        final AtomicInteger count = new AtomicInteger(0);
    }
}
