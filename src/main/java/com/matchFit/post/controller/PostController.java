package com.matchFit.post.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.matchFit.post.dto.PostInfoResponseDto;
import com.matchFit.post.dto.PostRequestDto;
import com.matchFit.post.service.PostService;
import com.matchFit.user.service.UserService;


import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PostController {
	
	private final PostService postService;
	private final UserService userService;
	
	@PostMapping("/api/posts")
	public ResponseEntity<String> createPosts(@RequestBody PostRequestDto dto) {
		postService.create(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body("모집글 생성 성공");		
	}
	
	
	@GetMapping("/api/posts/{postId}")
	public ResponseEntity<PostInfoResponseDto> getPostDetail(
							@PathVariable Long postId,
							@AuthenticationPrincipal UserDetails userDetails){
		
		Long userId = null;
	    if (userDetails != null) {
	        String email = userDetails.getUsername();
	        userId = userService.findUserIdByEmail(email); // 서비스에서 userId 조회
	    }
  
		PostInfoResponseDto dto = postService.searchPost(postId, userId);
		return ResponseEntity.ok(dto); 
	}
	
	
}
