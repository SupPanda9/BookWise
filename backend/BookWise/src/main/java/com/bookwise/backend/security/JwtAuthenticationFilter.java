package com.bookwise.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final int BEARER_PREFIX_LENGTH = 7;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, jakarta.servlet.ServletException {
        String token = getTokenFromRequest(request);

        System.out.println("Authorization Header: " + request.getHeader("Authorization"));
        if (token != null && jwtTokenProvider.validateToken(token)) {
            System.out.println("Valid token found!");
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);
            String userId = claims.getSubject();

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            System.out.println("Invalid or missing token.");
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(BEARER_PREFIX_LENGTH); // Remove "Bearer " prefix
        }
        return null;
    }
}
