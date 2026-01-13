package com.matchFit.user.service

import com.matchFit.user.dto.request.EditMyPageRequest
import com.matchFit.user.dto.response.MyPageResponse
import com.matchFit.user.exception.UserNotFoundException
import com.matchFit.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class MyPageService(
    private val userRepository: UserRepository
) {
    fun getMyPage(email: String): MyPageResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UserNotFoundException() }

        return MyPageResponse(
            user.email,
            user.nickname,
            user.town,
            user.age,
            user.sports
        )
    }

    @Transactional
    fun editMyPage(email: String, req: EditMyPageRequest): MyPageResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UserNotFoundException() }

        user.nickname = req.nickName
        user.age = req.age
        user.sports = req.sports

        return MyPageResponse(
            user.email,
            user.nickname,
            user.town,
            user.age,
            user.sports
        )
    }
}
