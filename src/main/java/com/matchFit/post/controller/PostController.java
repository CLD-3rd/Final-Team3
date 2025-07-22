package com.matchFit.post.controller;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.post.dto.response.GetPostsCalender;
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
    public ResponseEntity<GetPostsList> getPostsList(
		@RequestParam(required = false) Sports sports,
        @RequestParam(required = false) Gender gender,
        @RequestParam(required = false, defaultValue = "false") boolean nearest,
        @RequestParam(required = true)
	    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        GetPostsList postsList = postService.findByFilters(sports, gender, nearest, date);
        return ResponseEntity.ok(postsList);
    }
	
	@GetMapping("/calender")
	public ResponseEntity<GetPostsCalender> getPostsCalender(
			@RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
	    
		GetPostsCalender postsCalender = postService.findByMonth(month);
		return ResponseEntity.ok(postsCalender);
	}
}
