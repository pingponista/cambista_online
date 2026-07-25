package com.cambistaonline.auth.infrastructure.security;

import com.cambistaonline.auth.domain.model.User;
import com.cambistaonline.auth.domain.ports.JwtTokenPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProviderAdapter implements JwtTokenPort {

    private final SecretKey key;
    private final long expirationMs;

    public JwtProviderAdapter(
            @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secretHex,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretHex);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId().toString());
        claims.put("email", user.getEmail().getValue());
        claims.put("tipoUsuario", "N".equalsIgnoreCase(user.getRole()) ? "PERSONA_NATURAL" : "PERSONA_JURIDICA");
        claims.put("roles", user.getRole());
        claims.put("status", user.getStatus().name());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail().getValue())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
