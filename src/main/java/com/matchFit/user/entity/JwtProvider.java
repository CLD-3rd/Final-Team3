package com.matchFit.user.entity;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
@Component
public class JwtProvider {

    private final String secretKey = "test-secret-key-matchfit-app-long-long-long"; // 반드시 외부 파일로 분리 (현업은 env 또는 config)
    private final long tokenValidity = 1000 * 60 * 60 * 24; // 24시간

    public String createToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidity);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }

    public Claims validateToken(String token) throws JwtException {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }
}
