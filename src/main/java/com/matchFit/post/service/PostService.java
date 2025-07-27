package com.matchFit.post.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.matchFit.user.entity.Gender;
import com.matchFit.user.entity.User;
import com.matchFit.user.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;


@Transactional
@Service
@RequiredArgsConstructor
public class PostService {

	private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final PostViewService postViewService;

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
    	
    	if (sortType == SortType.DATE) {
           posts = sortPostsByDate(posts, sortType);
    	} else if (sortType == SortType.POPULAR) {
           posts = sortPostsByPopularity(posts, counts);
    	} else {
            throw new InvalidSortingTypeException();
        }
        
		List<GetPost> postDtos = GetPost.from(posts, counts);

        return GetPostsList.of(postDtos);
    }


    public GetPostsCalender findByMonth(YearMonth month) {
        validateNotPastMonth(month);

        LocalDate startDate = determineStartDate(month);
        LocalDateTime fromDate = startDate.atStartOfDay();
        LocalDateTime toDate   = month.atEndOfMonth().atTime(LocalTime.MAX);

        List<Post> posts = postRepository.findAllByDateBetween(fromDate, toDate);
        Map<LocalDate, Map<Sports, Long>> grouped = groupByDateAndSport(posts);
        List<GetPostCalender> calendarEntries = toCalendarEntries(grouped);

        return GetPostsCalender.of(calendarEntries);
    }

    
	
	// 모집 글 생성
	public Post create(PostRequestDto dto, @AuthenticationPrincipal CustomUserDetails userDetails) {
		User currentUser = userDetails.getUser();
		return postRepository.save(dto.toEntity(currentUser));
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
	        isBookmarked = participationRepository.existsByPostIdAndUserIdAndFollowTrue(postId, userId);
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
	
	
	@Transactional
	public UpdatePostResponseDto updatePost(Long postId, UpdatePostRequestDto request, CustomUserDetails userDetails) {
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
	    
	    // 게시글 정보 업데이트 (그대로)
	    post.setTitle(request.getTitle());
	    post.setDescription(request.getDescription());
	    post.setLocation(request.getLocation());
	    post.setDate(request.getDate());
	    post.setMaxPeople(request.getMaxPeople());
	    post.setGender(request.getGender());
	    post.setStatus(request.getStatus());
	    post.setCost(request.getCost());
	    post.setImageUrl(request.getImageUrl());
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

    private Map<LocalDate, Map<Sports, Long>> groupByDateAndSport(List<Post> posts) {
        return posts.stream()
            .collect(Collectors.groupingBy(
                post -> post.getDate().toLocalDate(),
                Collectors.groupingBy(Post::getSports, Collectors.counting())
            ));
    }

    private List<GetPostCalender> toCalendarEntries(Map<LocalDate, Map<Sports, Long>> grouped) {
        return grouped.entrySet().stream()
            .flatMap(dayEntry ->
                dayEntry.getValue().entrySet().stream()
                    .map(sportEntry -> new GetPostCalender(
                        dayEntry.getKey().toString(),
                        new GetPostCalender.Event(
                            sportEntry.getKey().getLabel(),
                            sportEntry.getValue().intValue()
                        )
                    ))
            )
            .sorted(Comparator.comparing(GetPostCalender::getDay))
            .collect(Collectors.toList());
    }
}
