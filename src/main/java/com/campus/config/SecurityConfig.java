package com.campus.config;

import com.campus.security.AuthRateLimitFilter;
import com.campus.security.TokenAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final AuthRateLimitFilter authRateLimitFilter;
    private final AppProperties appProperties;

    public SecurityConfig(TokenAuthenticationFilter tokenAuthenticationFilter,
                          AuthRateLimitFilter authRateLimitFilter,
                          AppProperties appProperties) {
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
        this.authRateLimitFilter = authRateLimitFilter;
        this.appProperties = appProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/media/**", "/error", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/register", "/api/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/", "/api/products/", "/api/products/**",
                                "/api/products/*/comments/").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll()
                )
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> writeJson(res, 401, "未认证"))
                        .accessDeniedHandler((req, res, e) -> writeJson(res, 403, "无权操作"))
                )
                .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = appProperties.corsOriginList();
        if (origins.isEmpty()) {
            origins = List.of("http://localhost:5173");
        }
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void writeJson(HttpServletResponse res, int code, String message) throws java.io.IOException {
        res.setStatus(code);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("data", null);
        new ObjectMapper().writeValue(res.getWriter(), body);
    }
}
