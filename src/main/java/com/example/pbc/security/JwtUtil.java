package com.example.pbc.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;


public class JwtUtil {

    private final String secret = "very-long-secret-key-for-jwt-signing-which-is-over-64-bytes-in-length";
    private final byte[] SIGNING_KEY = secret.getBytes(StandardCharsets.UTF_8);

    private final long EXPIRATION = 864_000_000; // 10 дней

    public String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY)
                .compact();
    }

    public String extractUuid(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token= token.substring(7);
        }
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}