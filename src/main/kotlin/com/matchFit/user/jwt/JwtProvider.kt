package com.matchFit.user.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date


@Component
class JwtProvider {
    @Value("\${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private lateinit var secretKey: String

    @Value("\${jwt.expiration:86400000}")
    private var tokenValidity: Long = 86400000

    fun createToken(userId: Long, email: String): String {
        val now = Date()
        val expiry = Date(now.time + tokenValidity)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .claim("userId", userId)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(SignatureAlgorithm.HS512, secretKey.toByteArray())
            .compact()
    }

    fun validateToken(token: String): Claims {
        return try {
            Jwts.parser()
                .setSigningKey(secretKey.toByteArray())
                .parseClaimsJws(token)
                .body
        } catch (ex: ExpiredJwtException) {
            throw JwtException("토큰이 만료되었습니다")
        } catch (ex: UnsupportedJwtException) {
            throw JwtException("지원되지 않는 토큰입니다")
        } catch (ex: MalformedJwtException) {
            throw JwtException("잘못된 토큰 형식입니다")
        } catch (ex: SignatureException) {
            throw JwtException("토큰 서명이 유효하지 않습니다")
        } catch (ex: IllegalArgumentException) {
            throw JwtException("토큰이 null이거나 빈 문자열입니다")
        }
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            validateToken(token)
            true
        } catch (ex: JwtException) {
            false
        }
    }

    fun getEmailFromToken(token: String): String =
        validateToken(token).get("email", String::class.java)

    fun getUserIdFromToken(token: String): Long =
        validateToken(token).get("userId", Long::class.java)
}
