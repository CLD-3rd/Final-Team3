package com.matchFit.post.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.post.dto.response.GetPostsList;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.service.PostService;
import com.matchFit.user.entity.Gender;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
	
	private final PostService postService;
	
	@GetMapping("/list")
    public ResponseEntity<GetPostsList> listPosts(
		@RequestParam(required = false) Sports sports,
        @RequestParam(required = false) Gender gender,
        @RequestParam(required = false, defaultValue = "false") boolean nearest
    ) {
        GetPostsList postList = postService.findByFilters(sports, gender, nearest);
        return ResponseEntity.ok(postList);
    }
}
