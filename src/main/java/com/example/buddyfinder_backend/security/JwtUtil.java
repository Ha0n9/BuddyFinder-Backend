package com.example.buddyfinder_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generate token with userId and isAdmin
     */
    public String generateToken(String email, Long userId, Boolean isAdmin) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("isAdmin", isAdmin != null ? isAdmin : false)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Backward compatibility: Generate token without isAdmin (defaults to false)
     */
    public String generateToken(String email, Long userId) {
        return generateToken(email, userId, false);
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    /**
     * ✅ NEW: Extract isAdmin from token
     */
    public Boolean extractIsAdmin(String token) {
        try {
            Boolean isAdmin = extractClaims(token).get("isAdmin", Boolean.class);
            return isAdmin != null ? isAdmin : false;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}