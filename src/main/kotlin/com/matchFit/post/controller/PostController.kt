package com.matchFit.post.controller

import com.matchFit.common.code.SuccessCode
import com.matchFit.common.dto.response.ApiResponseDTO
import com.matchFit.participation.dto.response.MessageResponse
import com.matchFit.participation.service.ParticipationService
import com.matchFit.post.dto.PostInfoResponseDto
import com.matchFit.post.dto.PostRequestDto
import com.matchFit.post.dto.UpdatePostRequestDto
import com.matchFit.post.dto.UpdatePostResponseDto
import com.matchFit.post.dto.response.GetMyPostApplicants
import com.matchFit.post.dto.response.GetMyPosts
import com.matchFit.post.dto.response.GetPostsCalender
import com.matchFit.post.dto.response.GetPostsList
import com.matchFit.post.entity.SortType
import com.matchFit.post.entity.Sports
import com.matchFit.post.service.PostService
import com.matchFit.user.entity.Gender
import com.matchFit.user.security.CustomUserDetails
import com.matchFit.user.service.UserService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.YearMonth


@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService,
    private val userService: UserService,
    private val participationService: ParticipationService
) {
    @PostMapping
    fun createPosts(
        @RequestPart("postData") dto: PostRequestDto,
        @RequestPart(value = "image", required = false) image: MultipartFile?,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponseDTO<String>> {
        postService.create(dto, image, userDetails)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_CREATED, null))
    }

    @GetMapping("/{postId}")
    fun getPostDetail(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<ApiResponseDTO<PostInfoResponseDto>> {
        val userId = userDetails?.username?.let { email -> userService.findUserIdByEmail(email) }
        val dto = postService.searchPost(postId, userId)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_DETAIL, dto))
    }

    @PostMapping("/{postId}/apply")
    fun applyPost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponseDTO<MessageResponse>> {
        val email = userDetails.username
        val userId = userService.findUserIdByEmail(email)
        participationService.applyPost(postId, userId)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_APPLY, null))
    }

    @GetMapping("/list")
    fun getPostsList(
        @RequestParam(required = false) sports: Sports?,
        @RequestParam(required = false) gender: Gender?,
        @RequestParam(required = false, defaultValue = "DATE") sortType: SortType,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @PageableDefault(page = 0, size = 10) pageable: Pageable
    ): ResponseEntity<ApiResponseDTO<GetPostsList>> {
        val postsList = postService.findByFilters(sports, gender, sortType, date, pageable)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_LIST, postsList))
    }

    @GetMapping("/calender")
    fun getPostsCalender(
        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") month: YearMonth
    ): ResponseEntity<ApiResponseDTO<GetPostsCalender>> {
        val postsCalender = postService.findByMonth(month)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_CALENDER, postsCalender))
    }

    @GetMapping("/mine")
    fun getMyPosts(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<ApiResponseDTO<GetMyPosts>> {
        val myPosts = postService.getMyPosts(userDetails)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_MY_POSTS, myPosts))
    }

    @GetMapping("/{postId}/applicants")
    fun getApplicants(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponseDTO<GetMyPostApplicants>> {
        val applicants = participationService.getApplicantsByPost(postId, userDetails)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_GET_MY_APPLICANTS, applicants))
    }

    @PutMapping("/{postId}")
    fun updatePost(
        @PathVariable postId: Long,
        @RequestPart("postData") request: UpdatePostRequestDto,
        @RequestPart(value = "image", required = false) image: MultipartFile?,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponseDTO<UpdatePostResponseDto>> {
        val response = postService.updatePost(postId, request, image, userDetails)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_UPDATED, response))
    }

    @DeleteMapping("/{postId}")
    fun deleteMyPost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponseDTO<Void>> {
        postService.deleteMyPost(postId, userDetails)
        return ResponseEntity.ok(ApiResponseDTO.onSuccess(SuccessCode.POST_DELETED, null))
    }
}
