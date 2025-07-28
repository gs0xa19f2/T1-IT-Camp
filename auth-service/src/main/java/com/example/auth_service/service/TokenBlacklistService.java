package com.example.auth_service.service;

import com.example.auth_service.repository.TokenBlacklistRepository;
import com.example.auth_service.entity.TokenBlacklist;
import com.example.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Теперь blacklist работает по jti
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtService jwtService;

    public void blacklistToken(String token) {
        String jti = jwtService.extractJti(token);
        if (jti != null && !jti.isEmpty()) {
            TokenBlacklist blacklisted = new TokenBlacklist(null, jti);
            tokenBlacklistRepository.save(blacklisted);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String jti = jwtService.extractJti(token);
        return jti != null && tokenBlacklistRepository.findByJti(jti).isPresent();
    }
}