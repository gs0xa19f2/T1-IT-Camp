package com.example.auth_service.service;

import com.example.auth_service.repository.TokenBlacklistRepository;
import com.example.auth_service.entity.TokenBlacklist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public void blacklistToken(String token) {
        TokenBlacklist blacklisted = new TokenBlacklist(null, token);
        tokenBlacklistRepository.save(blacklisted);
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.findByToken(token).isPresent();
    }
}