package com.matchFit.user.controller

import com.matchFit.user.jwt.JwtProvider
import com.matchFit.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.RestTemplate


@Controller
class KakaoOAuthController(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    @Value("\${kakao.client-id}") private val kakaoClientId: String,
    @Value("\${kakao.redirect-uri.signup}") private val kakaoSignupRedirectUri: String,
    @Value("\${kakao.redirect-uri.login}") private val kakaoLoginRedirectUri: String
) {
    @GetMapping("/api/kakao-config")
    @ResponseBody
    fun getKakaoConfig(): Map<String, String> {
        return mapOf(
            "clientId" to kakaoClientId,
            "signupRedirectUri" to kakaoSignupRedirectUri,
            "loginRedirectUri" to kakaoLoginRedirectUri
        )
    }

    @GetMapping("/api/user/oauth/kakao/callback")
    fun kakaoSignupCallback(@RequestParam code: String): String {
        return try {
            val email = getKakaoEmail(code, kakaoSignupRedirectUri)
            "redirect:https://www.match-fit.store/signup/index.html?kakaoEmail=${email}"
        } catch (ex: Exception) {
            "redirect:http://localhost:3000/signup?error=kakao_error"
        }
    }

    @GetMapping("/api/user/oauth/kakao/login-callback")
    fun kakaoLoginCallback(@RequestParam code: String): String {
        return try {
            val email = getKakaoEmail(code, kakaoLoginRedirectUri)
            val user = userRepository.findByEmail(email).orElse(null)

            if (user != null) {
                val jwt = jwtProvider.createToken(user.id!!, user.email)
                "redirect:https://www.match-fit.store/login/index.html?kakaoToken=${jwt}"
            } else {
                "redirect:https://www.match-fit.store/signup/index.html?kakaoEmail=${email}"
            }
        } catch (ex: Exception) {
            "redirect:http://localhost:3000/login?error=kakao_error"
        }
    }

    private fun getKakaoEmail(code: String, redirectUri: String): String {
        return try {
            val restTemplate = RestTemplate()

            val params: MultiValueMap<String, String> = LinkedMultiValueMap()
            params.add("grant_type", "authorization_code")
            params.add("client_id", kakaoClientId)
            params.add("redirect_uri", redirectUri)
            params.add("code", code)

            val tokenHeaders = HttpHeaders()
            tokenHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
            val tokenRequest = HttpEntity(params, tokenHeaders)

            val tokenResponse: ResponseEntity<Map<*, *>> = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                tokenRequest,
                Map::class.java
            )

            val accessToken = tokenResponse.body?.get("access_token") as? String
                ?: throw RuntimeException("카카오 액세스 토큰이 없습니다")

            val userHeaders = HttpHeaders()
            userHeaders.setBearerAuth(accessToken)
            val userRequest = HttpEntity<String>(userHeaders)

            val userResponse: ResponseEntity<Map<*, *>> = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                userRequest,
                Map::class.java
            )

            val kakaoAccount = userResponse.body?.get("kakao_account") as? Map<*, *>?
                ?: throw RuntimeException("카카오 계정 정보가 없습니다")
            val email = kakaoAccount["email"] as? String
                ?: throw RuntimeException("카카오에서 이메일 정보를 가져올 수 없습니다")

            email
        } catch (ex: Exception) {
            throw RuntimeException("카카오 로그인 실패: ${ex.message}")
        }
    }
}
