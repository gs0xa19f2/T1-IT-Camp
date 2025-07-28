package com.example.auth_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр, который блокирует любые HTTP-запросы и отвечает 403 Forbidden.
 * Работает во всех профилях, кроме "test".
 * Для production обязательно добавить HTTPS и SSL-сертификат.
 */
@Configuration
@Profile("!test")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebSecurityHttpsConfig extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.isSecure()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "HTTPS required");
            return;
        }
        filterChain.doFilter(request, response);
    }
}