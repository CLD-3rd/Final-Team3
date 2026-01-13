package com.matchFit.user.service

import com.matchFit.common.code.ErrorCode
import com.matchFit.common.exception.GeneralException
import com.matchFit.user.dto.request.FindEmailRequest
import com.matchFit.user.dto.request.PasswordResetRequest
import com.matchFit.user.dto.response.FindEmailResponse
import com.matchFit.user.repository.UserRepository
import com.matchFit.user.token.RedisPasswordResetToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder


@Service
@Transactional
class AccountRecoveryService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenStore: RedisPasswordResetToken,
    private val mailSender: JavaMailSender,
    @Value("\${app.mail.from}") private val fromAddress: String,
    @Value("\${app.frontend.reset-password-url}") private val resetUrlBase: String
) {
    private fun requireNonBlank(value: String?, name: String) {
        if (value.isNullOrBlank()) {
            throw IllegalArgumentException("${name}은(는) 필수입니다.")
        }
    }

    fun requestReset(req: PasswordResetRequest) {
        requireNonBlank(req.email, "email")

        val user = userRepository.findByEmail(req.email!!)
            .orElseThrow { GeneralException(ErrorCode.EMAIL_NOT_FOUND) }

        val token = tokenStore.issueToken(user.id!!)
        sendPasswordResetMail(user.email, token)
    }

    fun confirmReset(req: PasswordResetRequest) {
        requireNonBlank(req.token, "token")
        requireNonBlank(req.newPassword, "newPassword")
        if (req.newPassword!!.length < 8) {
            throw IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.")
        }

        val userId = tokenStore.peekUserId(req.token!!)
            ?: throw IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.")

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("계정을 찾을 수 없습니다.") }

        if (passwordEncoder.matches(req.newPassword, user.password)) {
            throw GeneralException(ErrorCode.PASSWORD_SAME)
        }

        user.password = passwordEncoder.encode(req.newPassword)
        userRepository.save(user)

        tokenStore.consumeToken(req.token!!)
    }

    private fun sendPasswordResetMail(to: String, token: String) {
        val link = UriComponentsBuilder.fromHttpUrl(resetUrlBase)
            .replaceQuery(null)
            .queryParam("token", token.trim())
            .build(true)
            .toUriString()

        val subject = "[MatchFit] 비밀번호 재설정 안내"
        val body = String.format(
            """
            안녕하세요.

            비밀번호 재설정을 요청하셨다면 아래 링크를 클릭하세요.


            %s


            """,
            link
        )

        val msg = SimpleMailMessage().apply {
            setFrom(fromAddress)
            setTo(to)
            setSubject(subject)
            setText(body)
        }
        mailSender.send(msg)
    }

    @Transactional(readOnly = true)
    fun findEmail(req: FindEmailRequest): FindEmailResponse {
        requireNonBlank(req.nickname, "nickname")

        val user = userRepository.findByNickname(req.nickname)
            .orElseThrow { IllegalArgumentException("일치하는 계정이 없습니다.") }

        return FindEmailResponse(maskEmail(user.email))
    }

    private fun maskEmail(email: String?): String? {
        if (email == null) return null
        val at = email.indexOf('@')
        if (at <= 1) return "***" + email.substring(kotlin.math.max(at, 0))
        val local = email.substring(0, at)
        val domain = email.substring(at)
        val keep = kotlin.math.max(1, local.length / 3)
        val visible = local.substring(0, keep)
        val stars = "*".repeat(kotlin.math.max(1, local.length - keep))
        return visible + stars + domain
    }
}
