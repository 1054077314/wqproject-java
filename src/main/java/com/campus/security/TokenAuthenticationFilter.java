package com.campus.security;

import com.campus.user.entity.Token;
import com.campus.user.entity.User;
import com.campus.user.mapper.TokenMapper;
import com.campus.user.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenMapper tokenMapper;
    private final UserMapper userMapper;

    public TokenAuthenticationFilter(TokenMapper tokenMapper, UserMapper userMapper) {
        this.tokenMapper = tokenMapper;
        this.userMapper = userMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = extractToken(request);
        if (key != null && !key.isEmpty()) {
            Token token = tokenMapper.findByKey(key);
            if (token == null) {
                if (isApi(request)) {
                    writeUnauthorized(response, "token 无效");
                    return;
                }
            } else if (token.isExpired()) {
                tokenMapper.deleteByKey(key);
                if (isApi(request)) {
                    writeUnauthorized(response, "token 已过期");
                    return;
                }
            } else {
                User user = userMapper.findById(token.getUserId());
                if (user == null || !user.isActive()) {
                    if (isApi(request)) {
                        writeUnauthorized(response, "token 无效");
                        return;
                    }
                } else {
                    UserPrincipal principal = new UserPrincipal(user, key);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isApi(HttpServletRequest request) {
        return request.getRequestURI() != null && request.getRequestURI().startsWith("/api/");
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
