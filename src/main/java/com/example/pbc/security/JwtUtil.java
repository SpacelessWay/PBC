package com.example.pbc.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

//@Component
public class JwtUtil {
    String secret = "very-long-secret-key-for-jwt-signing-which-is-over-64-bytes-in-length";

    private final String SECRET_KEY = Base64.getEncoder().encodeToString(secret.getBytes());

    private final long EXPIRATION = 864_000_000; // 10 дней

    public String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public String extractUuid(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // здесь будет uuid
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}