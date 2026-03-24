package com.matchFit.follow.service

import com.matchFit.common.code.ErrorCode
import com.matchFit.follow.dto.response.FollowApplyResponseDto
import com.matchFit.follow.dto.response.GetMyFollowResponseDto
import com.matchFit.follow.entity.Follow
import com.matchFit.follow.repository.FollowRepository
import com.matchFit.participation.entity.ApplicationStatus
import com.matchFit.participation.repository.ParticipationRepository
import com.matchFit.post.repository.PostRepository
import com.matchFit.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
@Transactional
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val participationRepository: ParticipationRepository
) {
    fun toggleFollow(email: String, postId: Long): FollowApplyResponseDto {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException(ErrorCode.USER_NOT_FOUND.message) }
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException(ErrorCode.POST_NOT_FOUND.message) }

        val userId = user.id!!
        val isCurrentlyFollowed = followRepository.existsByUserIdAndPostId(userId, postId)
        if (isCurrentlyFollowed) {
            followRepository.deleteByUserIdAndPostId(userId, postId)
        } else {
            val follow = Follow(user, post)
            followRepository.save(follow)
        }

        return FollowApplyResponseDto(postId, !isCurrentlyFollowed)
    }

    @Transactional(readOnly = true)
    fun getUserFollows(email: String): List<GetMyFollowResponseDto> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException(ErrorCode.USER_NOT_FOUND.message) }

        val follows = followRepository.findByUserIdWithPost(user.id!!)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val followedAtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        return follows.map { follow ->
            val post = follow.post
            val currentPeople = participationRepository.countByPost_IdAndStatus(
                post.id!!,
                ApplicationStatus.APPROVED
            ) + 1

            GetMyFollowResponseDto(
                post.id!!,
                post.title,
                post.sports.toString(),
                post.location,
                post.date.format(formatter),
                currentPeople,
                post.maxPeople,
                follow.createdAt?.format(followedAtFormatter),
                post.cost,
                post.status.toString()
            )
        }
    }

}
