package com.matchFit.post;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.post.dto.PostInfoResponseDto;
import com.matchFit.post.dto.PostRequestDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PostController {
	
	private final PostService postService;
	
	@PostMapping("/api/posts")
	public ResponseEntity<String> createPosts(@RequestBody PostRequestDto dto) {
		postService.create(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body("모집글 생성 성공");		
	}
	
	
	@GetMapping("/api/posts/{postId}")
	public ResponseEntity<PostInfoResponseDto> getPostDetail(
							@PathVariable Long postId,
							@RequestParam Long userId){
		//Long userId = null; // 로그인한 사용자 정보에서 가져오기
		PostInfoResponseDto dto = postService.searchPost(postId, userId);
		return ResponseEntity.ok(dto); 
	}
	
	
}
