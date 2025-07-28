package com.example.auth_service.security;

import com.example.auth_service.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Сервис для работы с JWT.
 * Поддерживает:
 * - подпись и верификацию токена
 * - отсутствие чувствительных данных внутри токена
 * - добавление jti (идентификатор токена) для blacklist/revoke
 * - возможность ротации секретов через версию (kid)
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // Извлекает логин пользователя из токена (subject claim)
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Извлекает идентификатор токена (jti)
    public String extractJti(String token) {
        return extractClaim(token, claims -> claims.getId());
    }

    // Извлекает версию ключа (kid, key id) для ротации
    public String extractKeyId(String token) {
        return extractClaim(token, claims -> claims.get("kid", String.class));
    }

    // Генерирует access-токен с подписью и jti
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("kid", jwtProperties.getKeyId()); // версия ключа для ротации
        String jti = UUID.randomUUID().toString();
        return generateToken(claims, userDetails, jwtProperties.getAccessTokenExpiration(), jti);
    }

    // Генерирует refresh-токен с подписью и jti
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("kid", jwtProperties.getKeyId());
        String jti = UUID.randomUUID().toString();
        return generateToken(claims, userDetails, jwtProperties.getRefreshTokenExpiration(), jti);
    }

    // Проверяет валидность токена
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration, String jti) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .id(jti)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Парсит токен и проверяет подпись
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}