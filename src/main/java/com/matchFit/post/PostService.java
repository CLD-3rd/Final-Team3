package com.matchFit.post;

import org.springframework.stereotype.Service;

import com.matchFit.post.entity.Post;

import jakarta.transaction.Transactional;

import com.matchFit.participation.entity.ParticipationRepository;
import com.matchFit.post.dto.PostInfoResponseDto;
import com.matchFit.post.dto.PostRequestDto;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
	private final ParticipationRepository participationRepository;
		
	public Post create(PostRequestDto dto) {
		return postRepository.save(dto.toEntity());
	}
	
	public PostInfoResponseDto searchPost(Long postId, Long userId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(()-> new IllegalArgumentException("해당 게시글이 없습니다."));

		int currentParticipantsCount = participationRepository.countByPostId(postId);
		boolean isBookmarked = participationRepository.existsByPostIdAndUserIdAndFollowTrue(postId, userId);
		
		return new PostInfoResponseDto(post, currentParticipantsCount, isBookmarked);
	}
}
