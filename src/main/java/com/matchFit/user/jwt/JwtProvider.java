package com.matchFit.user.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long tokenValidity;

    public String createToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidity);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes())
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            throw new JwtException("토큰이 만료되었습니다");
        } catch (UnsupportedJwtException ex) {
            throw new JwtException("지원되지 않는 토큰입니다");
        } catch (MalformedJwtException ex) {
            throw new JwtException("잘못된 토큰 형식입니다");
        } catch (SignatureException ex) {
            throw new JwtException("토큰 서명이 유효하지 않습니다");
        } catch (IllegalArgumentException ex) {
            throw new JwtException("토큰이 null이거나 빈 문자열입니다");
        }
    }

    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return validateToken(token).get("email", String.class);
    }
}
