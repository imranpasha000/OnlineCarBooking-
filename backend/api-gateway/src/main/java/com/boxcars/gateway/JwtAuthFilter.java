package com.boxcars.gateway;

import com.boxcars.common.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final List<String> publicPrefixes = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/actuator",
            "/api/vehicles/search",
            "/api/vehicles/available"
    );

    public JwtAuthFilter(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration:86400000}") long expiration
    ) {
        this.jwtUtil = new JwtUtil(secret, expiration);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path) || "OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = auth.substring(7);
        if (!jwtUtil.isValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Long userId = jwtUtil.getUserId(token);
        List<String> roles = jwtUtil.getRoles(token);
        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Roles", String.join(",", roles)))
                .build();
        return chain.filter(mutated);
    }

    private boolean isPublic(String path) {
        return publicPrefixes.stream().anyMatch(path::startsWith)
                || (path.startsWith("/api/vehicles/") && path.matches(".*/\\d+$")
                    && "GET".equalsIgnoreCase("GET"));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
