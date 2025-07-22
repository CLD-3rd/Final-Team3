package com.matchFit.user.jwt;

import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;

@Component
public class JwtProvider {
    
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}") // 24시간 (밀리초)
    private long tokenValidity;
    
    public String createToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidity);
        
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes())
                .compact();
    }
    
    public Claims validateToken(String token) throws JwtException {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtException("토큰이 만료되었습니다");
        } catch (UnsupportedJwtException e) {
            throw new JwtException("지원되지 않는 토큰입니다");
        } catch (MalformedJwtException e) {
            throw new JwtException("잘못된 토큰 형식입니다");
        } catch (SignatureException e) {
            throw new JwtException("토큰 서명이 유효하지 않습니다");
        } catch (IllegalArgumentException e) {
            throw new JwtException("토큰이 null이거나 빈 문자열입니다");
        }
    }
    
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("email", String.class);
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("userId", Long.class);
    }
}