package com.example.auth_service.config;

import com.example.auth_service.security.JwtService;
import com.example.auth_service.service.TokenBlacklistService;
import com.example.auth_service.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        // Сначала ищем токен в заголовке Authorization
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println("Auth header: " + authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("Token from header: " + token);
        } else if (request.getCookies() != null) {
            // Если нет заголовка, ищем токен в cookies (accessToken)
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    System.out.println("Token from cookies: " + token);
                    break;
                }
            }
        }

        if (token == null || token.isEmpty()) {
            System.out.println("Token is null or empty, passing filter");
            filterChain.doFilter(request, response);
            return;
        }

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            System.out.println("Token is blacklisted, returning 403");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final String userLogin = jwtService.extractUserName(token);
        System.out.println("Extracted login: " + userLogin);

        if (userLogin != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userLogin);
            if (jwtService.isTokenValid(token, userDetails)) {
                System.out.println("Token is valid, authenticating");
                SecurityContextHolder.getContext().setAuthentication(
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        )
                );
            } else {
                System.out.println("Token is invalid");
            }
        }
        filterChain.doFilter(request, response);
    }
}