package com.bookwise.backend.security;

import com.bookwise.backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Securely generates a key
    private static final long JWT_EXPIRATION_MS = 3600000; // 1 hour

    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getId())
            .claim("roles", user.getRoles())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
            .signWith(jwtSecretKey, SignatureAlgorithm.HS256) // Use Key and algorithm
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            System.out.println("Validating token: " + token);
            Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey) // Ensure this matches the signing key
                .build()
                .parseClaimsJws(token); // Parse and validate the token
            System.out.println("Token is valid!");
            return true;
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(jwtSecretKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(jwtSecretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

}
