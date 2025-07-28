package com.example.auth_service.controller;

import com.example.auth_service.dto.SignInRequest;
import com.example.auth_service.dto.SignUpRequest;
import com.example.auth_service.dto.JwtAuthenticationResponse;
import com.example.auth_service.dto.RefreshTokenRequest;
import com.example.auth_service.service.AuthenticationService;
import com.example.auth_service.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/signup")
    public JwtAuthenticationResponse signup(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/signin")
    public JwtAuthenticationResponse signin(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @PostMapping("/refresh")
    public JwtAuthenticationResponse refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return authenticationService.refreshToken(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, @RequestBody(required = false) RefreshTokenRequest refreshRequest) {
        // Отзываем access token (если он передан в заголовке)
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String accessToken = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(accessToken);
        }
        // Отзываем refresh token (если он передан в теле)
        if (refreshRequest != null && refreshRequest.getRefreshToken() != null) {
            authenticationService.logout(refreshRequest);
        }
        return ResponseEntity.ok("Successfully logged out");
    }
}