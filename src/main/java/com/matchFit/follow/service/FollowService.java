package com.matchFit.follow.service;

import com.matchFit.post.entity.Post;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.User;
import com.matchFit.user.repository.UserRepository;
import com.matchFit.follow.entity.Follow;
import com.matchFit.follow.repository.FollowRepository;
import com.matchFit.follow.dto.response.FollowApplyResponseDto;
import com.matchFit.follow.dto.response.GetMyFollowResponseDto;
import com.matchFit.participation.repository.ParticipationRepository;
import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.common.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {
    
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ParticipationRepository participationRepository;
    
    // 팔로우 토글 (ErrorCode 사용)
    public FollowApplyResponseDto toggleFollow(String email, Long postId) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));
        
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException(ErrorCode.POST_NOT_FOUND.getMessage()));
        
        Long userId = user.getId();
        boolean isCurrentlyFollowed = followRepository.existsByUserIdAndPostId(userId, postId);
        
        if (isCurrentlyFollowed) {
            // 팔로우시 팔로우 해제
            followRepository.deleteByUserIdAndPostId(userId, postId);
        } else {
            // 팔로우 안돼있을 시 팔로우 추가
            Follow follow = new Follow(user, post);
            followRepository.save(follow);
        }
        
        boolean isFollowed = !isCurrentlyFollowed;
        
        return new FollowApplyResponseDto(postId, isFollowed);
    }
    
    // 사용자의 팔로우 목록 조회 
    @Transactional(readOnly = true)
    public List<GetMyFollowResponseDto> getUserFollows(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));
        
        List<Follow> follows = followRepository.findByUserIdWithPost(user.getId());
        
        return follows.stream()
            .map(follow -> {
                Post post = follow.getPost();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                DateTimeFormatter followedAtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                
                // 실제 승인된 사용자 수 계산
                int currentPeople = participationRepository.countByPost_IdAndStatus(post.getId(), ApplicationStatus.APPROVED)+1;
                
                return new GetMyFollowResponseDto(
                    post.getId(),
                    post.getTitle(),
                    post.getSports().toString(),        
                    post.getLocation(),                 
                    post.getDate().format(formatter),  
                    currentPeople,                      
                    post.getMaxPeople(),              
                    follow.getCreatedAt() != null ? 
                        follow.getCreatedAt().format(followedAtFormatter) : null, // 찜한 날짜
                    post.getCost(),                   
                    post.getStatus().toString()        
                );
            })
            .collect(Collectors.toList());
    }
    
    // 팔로우 상태 확인
    @Transactional(readOnly = true)
    public boolean isFollowed(Long userId, Long postId) {
        return followRepository.existsByUserIdAndPostId(userId, postId);
    }
    
    // 게시글의 팔로우 개수 
    @Transactional(readOnly = true)
    public long getFollowCount(Long postId) {
        return followRepository.countByPostId(postId);
    }
}