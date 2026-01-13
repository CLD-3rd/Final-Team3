package com.matchFit.user.service

import com.matchFit.user.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun findUserIdByEmail(email: String): Long {
        return userRepository.findByEmail(email)
            .map { it.id }
            .orElseThrow { UsernameNotFoundException("이메일이 존재하지 않음") }
            ?: throw UsernameNotFoundException("이메일이 존재하지 않음")
    }
}
