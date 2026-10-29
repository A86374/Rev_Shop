package com.revShop.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    // anyone can call these
    private static final List<String> PUBLIC_ANY_METHOD = List.of("/api/auth/**");
    private static final List<String> PUBLIC_GET = List.of("/api/products/**", "/api/categories/**", "/api/reviews/**");

    private final SecretKey key;

    // same secret as user-service, so tokens it signs can be checked here
    public JwtAuthFilter(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        HttpMethod method = request.getMethod();

        Claims claims = readToken(request);
        String userId = claims == null ? null : claims.getSubject();
        String role = claims == null ? null : claims.get("role", String.class);

        boolean isPublic = HttpMethod.OPTIONS.equals(method)
                || matches(PUBLIC_ANY_METHOD, path)
                || (HttpMethod.GET.equals(method) && matches(PUBLIC_GET, path));

        if (!isPublic && userId == null) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Please log in: missing or invalid token");
        }
        if (MATCHER.match("/api/admin/**", path) && !"ADMIN".equals(role)) {
            return reject(exchange, HttpStatus.FORBIDDEN, "Admins only");
        }
        if (MATCHER.match("/api/seller/**", path) && !"SELLER".equals(role)) {
            return reject(exchange, HttpStatus.FORBIDDEN, "Sellers only");
        }
        if (MATCHER.match("/api/categories/**", path) && !HttpMethod.GET.equals(method) && !"ADMIN".equals(role)) {
            return reject(exchange, HttpStatus.FORBIDDEN, "Only admins can change categories");
        }

        // never trust these headers from outside: only this filter sets them
        ServerHttpRequest forwarded = request.mutate().headers(h -> {
            h.remove("X-User-Id");
            h.remove("X-User-Role");
            if (userId != null) {
                h.set("X-User-Id", userId);
                h.set("X-User-Role", role);
            }
        }).build();

        return chain.filter(exchange.mutate().request(forwarded).build());
    }

    // runs before the request is sent on to a service
    @Override
    public int getOrder() {
        return -1;
    }

    // null when there is no token, or it is expired / tampered
    private Claims readToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(header.substring(7).trim())
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private boolean matches(List<String> patterns, String path) {
        return patterns.stream().anyMatch(p -> MATCHER.match(p, path));
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":" + status.value() + ",\"error\":\"" + status.getReasonPhrase()
                + "\",\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}