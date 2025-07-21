package com.matchFit.post.service;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.matchFit.post.dto.response.GetPost;
import com.matchFit.post.dto.response.GetPostCalender;
import com.matchFit.post.dto.response.GetPostsCalender;
import com.matchFit.post.dto.response.GetPostsList;
import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.Gender;

@Service
public class PostService {

    private final PostRepository postRepository;
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public GetPostsList findByFilters(Sports sports, Gender gender, boolean nearest) {
        
        List<Post> posts = postRepository.findByFilters(
            sports != null ? sports.name() : null, 
            gender != null ? gender.name() : null, 
            nearest
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
}
