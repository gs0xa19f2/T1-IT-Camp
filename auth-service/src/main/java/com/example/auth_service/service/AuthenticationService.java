package com.example.auth_service.service;

import com.example.auth_service.dto.JwtAuthenticationResponse;
import com.example.auth_service.dto.RefreshTokenRequest;
import com.example.auth_service.dto.SignInRequest;
import com.example.auth_service.dto.SignUpRequest;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.InvalidCredentialsException;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new IllegalArgumentException("Login is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        // Не позволяем регистрироваться сразу как админ через публичный эндпоинт
        if ("ROLE_ADMIN".equals(request.getRole())) {
            throw new IllegalArgumentException("Cannot register with admin role.");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new IllegalStateException(request.getRole() + " role not found."));

        var user = User.builder()
                .login(request.getLogin())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(role))
                .build();
        userRepository.save(user);

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));

        var user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new IllegalArgumentException("Invalid login or password."));

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String userLogin = jwtService.extractUserName(request.getRefreshToken());
        User user = userRepository.findByLogin(userLogin)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (jwtService.isTokenValid(request.getRefreshToken(), user) &&
                user.getRefreshToken() != null && user.getRefreshToken().equals(request.getRefreshToken())
                && !tokenBlacklistService.isTokenBlacklisted(request.getRefreshToken())) {
            var accessToken = jwtService.generateAccessToken(user);
            return JwtAuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(request.getRefreshToken())
                    .build();
        }
        throw new InvalidCredentialsException("Invalid or expired refresh token");
    }

    public void logout(RefreshTokenRequest request) {
        User user = userRepository.findByRefreshToken(request.getRefreshToken())
                .orElse(null);

        if (user != null) {
            tokenBlacklistService.blacklistToken(request.getRefreshToken());
            user.setRefreshToken(null);
            userRepository.save(user);
        }
    }
}