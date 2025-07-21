package com.matchFit.post;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {
	
	@PostMapping("api/posts")
	public ResponseEntity<PostRequestDto> makePosts() {
		
		
	}
	
	
	
}
