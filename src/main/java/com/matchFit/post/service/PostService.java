package com.matchFit.post.service;

import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.repository.ParticipationRepository;
import com.matchFit.post.dto.PostInfoResponseDto;
import com.matchFit.post.dto.PostRequestDto;
	
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.matchFit.post.dto.response.GetMyPost;
import com.matchFit.post.dto.response.GetMyPosts;
import com.matchFit.post.dto.response.GetPost;
import com.matchFit.post.dto.response.GetPostCalender;
import com.matchFit.post.dto.response.GetPostsCalender;
import com.matchFit.post.dto.response.GetPostsList;
import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Status;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.Gender;
import com.matchFit.user.entity.User;
import com.matchFit.user.repository.UserRepository;
import com.matchFit.user.security.CustomUserDetails;


@Transactional
@Service
@RequiredArgsConstructor
public class PostService {

	private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public GetPostsList findByFilters(Sports sports, Gender gender, boolean nearest, LocalDate date) {
        
        List<Post> posts = postRepository.findByFilters(
            sports != null ? sports.name() : null, 
            gender != null ? gender.name() : null, 
            nearest,
            date
        );
        
		List<GetPost> postDtos = GetPost.from(posts);

        return GetPostsList.of(postDtos);
    }

	public GetPostsCalender findByMonth(YearMonth month) {
		LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<Post> posts = postRepository.findAllByDateBetween(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        
        // 3. 날짜별 → 종목별로 개수 집계
        Map<LocalDate, Map<Sports, Long>> grouped = posts.stream()
                .collect(Collectors.groupingBy(
                    post -> post.getDate().toLocalDate(),
                    Collectors.groupingBy(Post::getSports, Collectors.counting())
                ));
        
        // 4. DTO(GetPostCalender) 리스트로 변환
        List<GetPostCalender> calendarEntries = grouped.entrySet().stream()
            .flatMap(dayEntry ->
                dayEntry.getValue().entrySet().stream()
                    .map(sportEntry -> new GetPostCalender(
                        dayEntry.getKey().toString(),                       // "2025-07-13" 같은 day 문자열
                        new GetPostCalender.Event(
                            sportEntry.getKey().getLabel(),           // enum 한글명, ex. "축구"
                            sportEntry.getValue().intValue()               // 해당 일자·종목 이벤트 개수
                        )
                    ))
            )
            .sorted(Comparator.comparing(GetPostCalender::getDay))           // day 오름차순 정렬
            .collect(Collectors.toList());
        // 5. 전체 결과를 GetPostsCalender로 감싸서 반환
        return GetPostsCalender.of(calendarEntries);
	}
	
	// 모집 글 생성
	public Post create(PostRequestDto dto, Long userId) {	
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

	    // 3. recruitCount 직접 증가 
	    int currentRecruitCount = user.getRecruitCount(); 
	    user.setRecruitCount(currentRecruitCount + 1);    
	    userRepository.save(user);

	    // 4. Post 생성 및 user 연결
	    Post post = dto.toEntity(user);
	    post.setUser(user);
		
		return postRepository.save(post);

	}
	
	// 모집 글 상세 조회
	public PostInfoResponseDto searchPost(Long postId, Long userId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(()-> new IllegalArgumentException("해당 게시글이 없습니다."));

		int currentParticipantsCount = participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
		
		if(currentParticipantsCount >= post.getMaxPeople()) {
			post.setStatus(Status.CLOSED);
			postRepository.save(post);
		}
		
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
                    participationRepository.countByPostId(post.getId()),
                    post.getMaxPeople(),
                    post.getStatus().name()
            ))
            .collect(Collectors.toList());
        return GetMyPosts.of(myPosts);
    }
}
