package com.matchFit.post.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matchFit.common.code.SuccessCode;
import com.matchFit.common.dto.response.ApiResponseDTO;
import com.matchFit.participation.dto.response.MessageResponse;
import com.matchFit.participation.service.ParticipationService;
import com.matchFit.post.dto.PostInfoResponseDto;
import com.matchFit.post.dto.PostRequestDto;
import com.matchFit.post.dto.UpdatePostRequestDto;
import com.matchFit.post.dto.UpdatePostResponseDto;
import com.matchFit.post.dto.response.GetMyPostApplicants;
import com.matchFit.post.dto.response.GetMyPosts;
import com.matchFit.post.dto.response.GetPostsCalender;
import com.matchFit.post.dto.response.GetPostsList;
import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.SortType;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.service.PostService;
import com.matchFit.user.entity.Gender;
import com.matchFit.user.security.CustomUserDetails;
import com.matchFit.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
	
	private final PostService postService;
	private final UserService userService;
	private final ParticipationService participationService;
	

	// 모집글 생성
	@PostMapping
	public ResponseEntity<ApiResponseDTO<String>> createPosts(@RequestBody PostRequestDto dto,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		postService.create(dto, userDetails);

		//return ResponseEntity.status(HttpStatus.CREATED).body("모집글 생성 성공");		


		return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_CREATED, null));		

	}
	
	// 모집글 상세 조회
	@GetMapping("/{postId}")
	public ResponseEntity<ApiResponseDTO<PostInfoResponseDto>> getPostDetail(
							@PathVariable Long postId,
							@AuthenticationPrincipal UserDetails userDetails){
		
		Long userId = null;
	    if (userDetails != null) {
	        String email = userDetails.getUsername();
	        userId = userService.findUserIdByEmail(email); // 서비스에서 userId 조회
	    }
  
		PostInfoResponseDto dto = postService.searchPost(postId, userId);
		return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_DETAIL, dto));
	}
	
	// 모집 글 신청하기
	@PostMapping("/{postId}/apply")
	public ResponseEntity<ApiResponseDTO<MessageResponse>> applyPost(
			@PathVariable Long postId,
			@AuthenticationPrincipal UserDetails userDetails){	
				
		String email = userDetails.getUsername();
        Long userId = userService.findUserIdByEmail(email);
        
        participationService.applyPost(postId, userId);
		return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_APPLY, null));
	}
	

	
	@GetMapping("/list")
    public ResponseEntity<ApiResponseDTO<GetPostsList>> getPostsList(
		@RequestParam(required = false) Sports sports,
        @RequestParam(required = false) Gender gender,
        @RequestParam(required = false, defaultValue = "DATE") SortType sortType,
        @RequestParam(required = false)
	    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        GetPostsList postsList = postService.findByFilters(sports, gender, sortType, date);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_LIST, postsList));
    }
	
	@GetMapping("/calender")
	public ResponseEntity<ApiResponseDTO<GetPostsCalender>> getPostsCalender(
			@RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
	    
		GetPostsCalender postsCalender = postService.findByMonth(month);
		return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_CALENDER, postsCalender));
	}
	
	@GetMapping("/mine")
    public ResponseEntity<ApiResponseDTO<GetMyPosts>> getMyPosts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        GetMyPosts myPosts = postService.getMyPosts(userDetails);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_MY_POSTS, myPosts));
    }
	
	@GetMapping("/{postId}/applicants")
	public ResponseEntity<ApiResponseDTO<GetMyPostApplicants>> getApplicants(
	    @PathVariable Long postId,
	    @AuthenticationPrincipal CustomUserDetails userDetails
	) {
		GetMyPostApplicants applicants = participationService.getApplicantsByPost(postId, userDetails);
	    return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_MY_APPLICANTS, applicants));
	}
	
	
	@PutMapping("/{postId}")
	public ResponseEntity<ApiResponseDTO<UpdatePostResponseDto>> updatePost(  
	    @PathVariable Long postId,
	    @RequestBody UpdatePostRequestDto request,  // 그대로 유지
	    @AuthenticationPrincipal CustomUserDetails userDetails) {
	    

        UpdatePostResponseDto response = postService.updatePost(postId, request, userDetails);
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_UPDATED, response));
	}

}
