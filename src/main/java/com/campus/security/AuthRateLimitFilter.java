package com.campus.security;

import com.campus.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed-window rate limit for login/register to reduce credential stuffing.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri == null || !(uri.equals("/api/login") || uri.equals("/api/register"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        int limit = uri.equals("/api/register")
                ? appProperties.getRateLimitRegisterPerMinute()
                : appProperties.getRateLimitLoginPerMinute();
        String key = clientIp(request) + "|" + uri;
        long now = System.currentTimeMillis();
        long windowStart = now - 60_000L;

        Deque<Long> q = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst() < windowStart) {
                q.pollFirst();
            }
            if (q.size() >= limit) {
                writeTooMany(response);
                return;
            }
            q.addLast(now);
        }
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }

    private void writeTooMany(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        java.util.LinkedHashMap<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("code", 429);
        body.put("message", "请求过于频繁，请稍后再试");
        body.put("data", null);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
