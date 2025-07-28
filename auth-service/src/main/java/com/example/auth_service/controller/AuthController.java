package com.example.auth_service.controller;

import com.example.auth_service.dto.SignInRequest;
import com.example.auth_service.dto.SignUpRequest;
import com.example.auth_service.dto.JwtAuthenticationResponse;
import com.example.auth_service.dto.RefreshTokenRequest;
import com.example.auth_service.service.AuthenticationService;
import com.example.auth_service.service.TokenBlacklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody @Valid SignInRequest request, HttpServletResponse response) {
        JwtAuthenticationResponse tokens = authenticationService.signIn(request);
        setTokenCookies(response, tokens);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refresh(@RequestBody @Valid RefreshTokenRequest request, HttpServletResponse response) {
        JwtAuthenticationResponse tokens = authenticationService.refreshToken(request);
        setTokenCookies(response, tokens);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) RefreshTokenRequest refreshRequest) {
        // Remove tokens from cookies
        clearTokenCookies(response);

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

    /**
     * Устанавливает HttpOnly, Secure, SameSite=Strict cookies для access и refresh токенов.
     */
    private void setTokenCookies(HttpServletResponse response, JwtAuthenticationResponse tokens) {
        // Access token — короткое время жизни
        Cookie accessCookie = new Cookie("accessToken", tokens.getAccessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60); // 15 минут

        // Refresh token — длинное время жизни
        Cookie refreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 дней

        // Добавляем обычные cookie
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // Добавляем SameSite
        response.addHeader("Set-Cookie", String.format(
                "accessToken=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=Strict",
                tokens.getAccessToken(), 15 * 60)
        );
        response.addHeader("Set-Cookie", String.format(
                "refreshToken=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=Strict",
                tokens.getRefreshToken(), 7 * 24 * 60 * 60)
        );
    }

    // Очищает токеновые cookies (при logout)
    private void clearTokenCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", "");
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);

        Cookie refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        response.addHeader("Set-Cookie", "accessToken=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Strict");
        response.addHeader("Set-Cookie", "refreshToken=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Strict");
    }
}