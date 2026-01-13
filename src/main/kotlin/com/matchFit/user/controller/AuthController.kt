package com.matchFit.user.controller

import com.matchFit.common.code.SuccessCode
import com.matchFit.common.dto.response.ApiResponseDTO
import com.matchFit.post.entity.Sports
import com.matchFit.user.dto.request.FindEmailRequest
import com.matchFit.user.dto.request.PasswordResetRequest
import com.matchFit.user.dto.request.SignUpRequest
import com.matchFit.user.dto.response.FindEmailResponse
import com.matchFit.user.entity.Gender
import com.matchFit.user.entity.User
import com.matchFit.user.exception.EmailAlreadyExistException
import com.matchFit.user.exception.GenderInvalidException
import com.matchFit.user.exception.InvalidPasswordException
import com.matchFit.user.exception.KakaoLoginException
import com.matchFit.user.exception.NicknameAlreadyExistException
import com.matchFit.user.exception.SportsInvalidException
import com.matchFit.user.exception.UserNotFoundException
import com.matchFit.user.jwt.JwtProvider
import com.matchFit.user.repository.UserRepository
import com.matchFit.user.service.AccountRecoveryService
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView


@RestController
@RequestMapping("/api/user")
class AuthController(
    private val accountRecoveryService: AccountRecoveryService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) {
    @GetMapping("/signup")
    fun signupPage(): ModelAndView = ModelAndView("forward:/signup.html")

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: SignUpRequest): ResponseEntity<ApiResponseDTO<Void>> {
        if (userRepository.existsByEmail(signUpRequest.email)) {
            throw EmailAlreadyExistException()
        }

        if (userRepository.existsByNickname(signUpRequest.nickname)) {
            throw NicknameAlreadyExistException()
        }

        val gender = try {
            Gender.valueOf(signUpRequest.gender)
        } catch (ex: IllegalArgumentException) {
            throw GenderInvalidException()
        }

        val sports = try {
            Sports.valueOf(signUpRequest.sports)
        } catch (ex: IllegalArgumentException) {
            throw SportsInvalidException()
        }

        val user = User().apply {
            email = signUpRequest.email
            nickname = signUpRequest.nickname
            this.gender = gender
            this.sports = sports
            age = signUpRequest.age
            town = signUpRequest.town
        }

        val rawPassword = signUpRequest.password
        user.password = if (rawPassword.isNullOrEmpty()) {
            "KAKAO_LOGIN"
        } else {
            passwordEncoder.encode(rawPassword)
        }

        userRepository.save(user)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_CREATED, null))
    }

    @PostMapping("/check-email")
    fun checkEmail(@RequestBody request: Map<String, String>): ResponseEntity<ApiResponseDTO<Void>> {
        val email = request["email"]
        if (email != null && userRepository.existsByEmail(email)) {
            throw EmailAlreadyExistException()
        }
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_EMAIL_AVAILABLE, null))
    }

    @PostMapping("/check-nickname")
    fun checkNickname(@RequestBody request: Map<String, String>): ResponseEntity<ApiResponseDTO<Void>> {
        val nickname = request["nickname"]
        if (nickname != null && userRepository.existsByNickname(nickname)) {
            throw NicknameAlreadyExistException()
        }
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_NICKNAME_AVAILABLE, null))
    }

    @GetMapping("/login")
    fun loginPage(): ModelAndView = ModelAndView("forward:/login.html")

    @PostMapping("/login")
    fun loginUser(@RequestBody request: Map<String, String>): ResponseEntity<ApiResponseDTO<Map<String, String>>> {
        val email = request["email"]
        val password = request["password"]

        val user = userRepository.findByEmail(email ?: "").orElse(null)
            ?: throw UserNotFoundException()

        if (user.password == "KAKAO_LOGIN") {
            throw KakaoLoginException()
        }

        if (password == null || !passwordEncoder.matches(password, user.password)) {
            throw InvalidPasswordException()
        }

        val jwt = jwtProvider.createToken(user.id!!, user.email)
        val payload = mapOf("token" to jwt)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, payload))
    }

    @PostMapping("/find-email")
    fun findEmail(@RequestBody findEmailRequest: FindEmailRequest): ResponseEntity<ApiResponseDTO<FindEmailResponse>> {
        val response = accountRecoveryService.findEmail(findEmailRequest)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_FIND_MY_EMAIL, response))
    }

    @PostMapping("/request-password")
    fun request(@RequestBody req: PasswordResetRequest): ResponseEntity<ApiResponseDTO<Void>> {
        accountRecoveryService.requestReset(req)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_REQUEST_PASSWORD_RESET, null))
    }

    @PostMapping("/confirm-password")
    fun confirm(@RequestBody req: PasswordResetRequest): ResponseEntity<ApiResponseDTO<Void>> {
        accountRecoveryService.confirmReset(req)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.USER_CONFIRMED_PASSWORD_RESET, null))
    }
}
