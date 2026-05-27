package com.matchFit.follow.service;

import com.matchFit.common.code.ErrorCode;
import com.matchFit.follow.dto.response.FollowApplyResponseDto;
import com.matchFit.follow.dto.response.GetMyFollowResponseDto;
import com.matchFit.follow.entity.Follow;
import com.matchFit.follow.repository.FollowRepository;
import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.repository.ParticipationRepository;
import com.matchFit.post.entity.Post;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.User;
import com.matchFit.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ParticipationRepository participationRepository;

    public FollowApplyResponseDto toggleFollow(String email, Long postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.POST_NOT_FOUND.getMessage()));

        Long userId = user.getId();
        boolean isCurrentlyFollowed = followRepository.existsByUserIdAndPostId(userId, postId);
        if (isCurrentlyFollowed) {
            followRepository.deleteByUserIdAndPostId(userId, postId);
        } else {
            Follow follow = new Follow(user, post);
            followRepository.save(follow);
        }

        return new FollowApplyResponseDto(postId, !isCurrentlyFollowed);
    }

    @Transactional(readOnly = true)
    public List<GetMyFollowResponseDto> getUserFollows(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));

        List<Follow> follows = followRepository.findByUserIdWithPost(user.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter followedAtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<GetMyFollowResponseDto> result = new ArrayList<>(follows.size());
        for (Follow follow : follows) {
            Post post = follow.getPost();
            int currentPeople = participationRepository.countByPost_IdAndStatus(
                    post.getId(),
                    ApplicationStatus.APPROVED
            ) + 1;

            LocalDateTime createdAt = follow.getCreatedAt();
            String followedAt = createdAt != null ? createdAt.format(followedAtFormatter) : null;

            result.add(new GetMyFollowResponseDto(
                    post.getId(),
                    post.getTitle(),
                    post.getSports().toString(),
                    post.getLocation(),
                    post.getDate().format(formatter),
                    currentPeople,
                    post.getMaxPeople(),
                    followedAt,
                    post.getCost(),
                    post.getStatus().toString()
            ));
        }
        return result;
    }
}
