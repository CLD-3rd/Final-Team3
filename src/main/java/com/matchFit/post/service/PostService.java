package com.matchFit.post.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.matchFit.follow.repository.FollowRepository;
import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.repository.ParticipationRepository;
import com.matchFit.post.dto.PostInfoResponseDto;
import com.matchFit.post.dto.PostRequestDto;
import com.matchFit.post.dto.UpdatePostRequestDto;
import com.matchFit.post.dto.UpdatePostResponseDto;
import com.matchFit.post.dto.response.GetMyPost;
import com.matchFit.post.dto.response.GetMyPosts;
import com.matchFit.post.dto.response.GetPost;
import com.matchFit.post.dto.response.GetPostCalender;
import com.matchFit.post.dto.response.GetPostsCalender;
import com.matchFit.post.dto.response.GetPostsList;
import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.SortType;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Status;
import com.matchFit.post.exception.InvalidSortingTypeException;
import com.matchFit.post.exception.PastDayException;
import com.matchFit.post.exception.PastEventModificationException;
import com.matchFit.post.exception.PastMonthException;
import com.matchFit.post.exception.PostNotFoundException;
import com.matchFit.post.exception.UnauthorizedUserException;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.s3.service.S3Service;
import com.matchFit.user.entity.Gender;
import com.matchFit.user.entity.User;
import com.matchFit.user.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;


@Transactional
@Service
@RequiredArgsConstructor
public class PostService {
	private final FollowRepository followRepository;
	private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final PostViewService postViewService;
    private final S3Service s3Service;
    
    public GetPostsList findByFilters(Sports sports, Gender gender, SortType sortType, LocalDate date) {
    	// 이전 날짜값이 들어오면 예외 처리
    	if (date != null) {
    	    validateNotPastDate(date);
    	}

    	List<Post> posts = postRepository.findByFilters(
            sports != null ? sports.name() : null, 
            gender != null ? gender.name() : null, 
            date
        );
    	
    	// 1) ID 리스트 수집
    	List<Long> ids = posts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());
    	
    	// 2) 조회수 맵 조회
        Map<Long, Long> counts = postViewService.getViewCounts(ids);
    	
        // 현재 신청인원 count
        List<Object[]> approvedCounts = participationRepository.countApprovedByPostIds(ids, ApplicationStatus.APPROVED);
        Map<Long, Integer> currentPeopleMap = new HashMap<>();
        for (Object[] row : approvedCounts) {
            Long postId = (Long) row[0];
            Long count = (Long) row[1];
            currentPeopleMap.put(postId, count.intValue());
        }
        
    	if (sortType == SortType.DATE) {
           posts = sortPostsByDate(posts, sortType);
    	} else if (sortType == SortType.POPULAR) {
           posts = sortPostsByPopularity(posts, counts);
    	} else {
            throw new InvalidSortingTypeException();
        }
        
		List<GetPost> postDtos = GetPost.from(posts, counts, currentPeopleMap);

        return GetPostsList.of(postDtos);
    }


    public GetPostsCalender findByMonth(YearMonth month) {
        validateNotPastMonth(month);

        LocalDate startDate = determineStartDate(month);
        LocalDateTime fromDate = startDate.atStartOfDay();
        LocalDateTime toDate   = month.atEndOfMonth().atTime(LocalTime.MAX);

        List<Post> posts = postRepository.findAllByDateBetween(fromDate, toDate);
        Map<LocalDate, Map<Sports, List<Post>>> grouped = groupByDateAndSport(posts);
        List<GetPostCalender> calendarEntries = toCalendarEntries(grouped);

        return GetPostsCalender.of(calendarEntries);
    }

    
	
	// 모집 글 생성
    public Post create(PostRequestDto dto, MultipartFile image, @AuthenticationPrincipal CustomUserDetails userDetails) {
		String imageUrl = null;
		
		// 이미지가 있으면 S3에 업로드
	    if (image != null && !image.isEmpty()) {
	        if (!s3Service.isValidImageFile(image)) {
	            throw new RuntimeException("이미지 파일만 업로드 가능합니다.");
	        }
	        
	        try {
	            imageUrl = s3Service.uploadFile(image);
	        } catch (Exception e) {
	            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
	        }
	    }
		
		User currentUser = userDetails.getUser();
		
		Post post = dto.toEntity(currentUser);
		post.setImageUrl(imageUrl);  // 이미지 URL 설정
		
		return postRepository.save(post);
	}
	
	// 모집 글 상세 조회
	public PostInfoResponseDto searchPost(Long postId, Long userId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(()-> new PostNotFoundException());

		int currentParticipantsCount = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
		
		if(currentParticipantsCount >= post.getMaxPeople()) {
			post.setStatus(Status.CLOSED);
			postRepository.save(post);
		}
		postViewService.recordView(postId, userId);
		
		boolean isBookmarked = false; 
	    if (userId != null) {
	    	isBookmarked = followRepository.existsByUserIdAndPostId(userId, postId);
	    }	
		return new PostInfoResponseDto(post, currentParticipantsCount, isBookmarked);
	}
	
	@Transactional(readOnly = true)
	public GetMyPosts getMyPosts(@AuthenticationPrincipal CustomUserDetails userDetails) {
		User currentUser = userDetails.getUser();
	    System.out.println("currentUser = " + currentUser.getId());
        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        System.out.println("posts = " + posts);
        List<GetMyPost> myPosts = posts.stream()
            .map(post -> new GetMyPost(
            		post.getId(), 
                    post.getTitle(),
                    post.getDate(),
                    participationRepository.countByPost_IdAndStatus(post.getId(),ApplicationStatus.APPROVED),
                    post.getMaxPeople(),
                    post.getStatus().name()
            ))
            .collect(Collectors.toList());
        return GetMyPosts.of(myPosts);
    }
	
	
	private List<Post> sortPostsByDate(List<Post> posts, SortType sortType) {
		return posts.stream()
                .sorted(Comparator.comparing(
                    p -> Math.abs(
                        ChronoUnit.SECONDS.between(p.getDate(), LocalDateTime.now())
                    )
                ))
                .collect(Collectors.toList());
	}
	
	private List<Post> sortPostsByPopularity(List<Post> posts, Map<Long, Long> counts) {
        // 3) 조회수 내림차순 정렬
        return posts.stream()
                    .sorted(Comparator.comparingLong(
                        p -> counts.getOrDefault(((Post) p).getId(), 0L)
                    ).reversed())
                    .collect(Collectors.toList());
    }
	
	// 모집글 수정
	@Transactional
	public UpdatePostResponseDto updatePost(Long postId, UpdatePostRequestDto request, MultipartFile image, CustomUserDetails userDetails) {
	    // 게시글 조회
	    Post post = postRepository.findById(postId)
	        .orElseThrow(() -> new PostNotFoundException());
	    
	    // 작성자 권한 확인
	    User currentUser = userDetails.getUser();
	    if (!post.getUser().getId().equals(currentUser.getId())) {
	        throw new UnauthorizedUserException();
	    }
	    
	    // 날짜 확인
	    LocalDateTime now = LocalDateTime.now();
	    if (post.getDate().isBefore(now)) {
	        throw new PastEventModificationException();
	    }
	    
	    String newImageUrl = post.getImageUrl(); // 기존 이미지 URL
	    
	    // 이미지 처리 로직
	    if (image != null && !image.isEmpty()) {
	        // 새 이미지가 있는 경우
	        if (!s3Service.isValidImageFile(image)) {
	            throw new RuntimeException("이미지 파일만 업로드 가능합니다.");
	        }
	        
	        try {
	            // 새 이미지 업로드
	            newImageUrl = s3Service.uploadFile(image);
	            
	            // 기존 이미지가 있었다면 삭제
	            if (post.getImageUrl() != null) {
	                s3Service.deleteFileByUrl(post.getImageUrl());
	            }
	        } catch (Exception e) {
	            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
	        }
	    } else if (request.getRemoveImage() != null && request.getRemoveImage()) {
	        // 이미지 삭제 요청인 경우
	        if (post.getImageUrl() != null) {
	            try {
	                s3Service.deleteFileByUrl(post.getImageUrl());
	                newImageUrl = null;
	            } catch (Exception e) {
	                throw new RuntimeException("이미지 삭제에 실패했습니다.", e);
	            }
	        }
	    }
	    
	    // 게시글 정보 업데이트
	    post.setTitle(request.getTitle());
	    post.setDescription(request.getDescription());
	    post.setLocation(request.getLocation());
	    post.setDate(request.getDate());
	    post.setMaxPeople(request.getMaxPeople());
	    post.setGender(request.getGender());
	    post.setStatus(request.getStatus());
	    post.setCost(request.getCost());
	    post.setImageUrl(newImageUrl);  // 이미지 URL 업데이트
	    post.setSports(request.getSports());
	    post.setTown(request.getTown());
	    
	    Post updatedPost = postRepository.save(post);
	    
	    return UpdatePostResponseDto.from(updatedPost);
	}
	
	private void validateNotPastMonth(YearMonth month) {
        YearMonth currentMonth = YearMonth.now();
        if (month.isBefore(currentMonth)) {
            throw new PastMonthException();
        }
    }
	
	private void validateNotPastDate(LocalDate date) {
		LocalDate today = LocalDate.now();
	    if (date.isBefore(today)) {
	        throw new PastDayException();
	    }
		
	}

    private LocalDate determineStartDate(YearMonth month) {
        YearMonth current = YearMonth.from(LocalDate.now());
        if (month.equals(current)) {
            return LocalDate.now();
        }
        return month.atDay(1);
    }

    private Map<LocalDate, Map<Sports, List<Post>>> groupByDateAndSport(List<Post> posts) {
        return posts.stream()
            .collect(Collectors.groupingBy(

                post -> post.getDate().toLocalDate(),
                Collectors.groupingBy(Post::getSports, Collectors.toList())
            ));
    }

    private List<GetPostCalender> toCalendarEntries(Map<LocalDate, Map<Sports, List<Post>>> grouped) {
    	return grouped.entrySet().stream()
    	        .flatMap(dayEntry ->
    	            dayEntry.getValue().entrySet().stream()
    	                .map(sportEntry -> {
    	                    List<Post> postList = sportEntry.getValue();
    	                    // 모집글 시간들만 뽑기 (예: "14:00")
    	                    List<String> times = postList.stream()
    	                        .map(post -> post.getDate().toLocalTime().toString())
    	                        .sorted()
    	                        .collect(Collectors.toList());

    	                    return new GetPostCalender(
    	                        dayEntry.getKey().toString(),
    	                        new GetPostCalender.Event(
    	                            sportEntry.getKey().getLabel(),
    	                            postList.size(),
    	                            times
    	                        )
    	                    );
    	                })
    	        )
    	        .sorted(Comparator.comparing(GetPostCalender::getDay))
    	        .collect(Collectors.toList());
    }
}
