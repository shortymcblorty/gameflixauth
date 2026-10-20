package com.example.gameflixauth.service;

import com.example.gameflixauth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService
{
    private final String jwtSecret;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String jwtSecret,
                      @Value("${jwt.expiration-ms}") long expirationMs)
    {
        this.jwtSecret = jwtSecret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user)
    {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token)
    {
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token)
    {
        Number userId = extractClaims(token).get("userId", Number.class);

        return userId.longValue();
    }

    public boolean isTokenValid(String token)
    {
        try
        {
            Claims claims = extractClaims(token);

            return claims.getExpiration().after(new Date());
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    private Claims extractClaims(String token)
    {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey()
    {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}