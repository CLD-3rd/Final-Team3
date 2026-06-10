package com.matchFit.post.service;

import com.matchFit.follow.repository.FollowRepository;
import com.matchFit.participation.entity.ApplicationStatus;
import com.matchFit.participation.entity.Participation;
import com.matchFit.participation.repository.ParticipationRepository;
import com.matchFit.payment.repository.PaymentRepository;
import com.matchFit.payment.service.PaymentService;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private static final String APPLICANT_KEY_FMT = "applicants:post_%d";
    private static final int APPLICANT_KEY_TTL_MINUTES = 5;

    private final FollowRepository followRepository;
    private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final PostViewService postViewService;
    private final S3Service s3Service;
    private final StringRedisTemplate redisTemplate;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final PlatformTransactionManager transactionManager;

    @Transactional(readOnly = true)
    public GetPostsList findByFilters(
            Sports sports,
            Gender gender,
            SortType sortType,
            LocalDate date,
            Pageable pageable,
            Long userId
    ) {
        if (date != null) validateNotPastDate(date);

        Set<Long> followedPostIds = userId != null
                ? followRepository.findPostIdsByUserId(userId)
                : Collections.emptySet();

        if (sortType == SortType.DATE) {
            return findByDateSorted(sports, gender, date, pageable, followedPostIds);
        }
        if (sortType == SortType.POPULAR) {
            return findByPopularitySorted(sports, gender, date, pageable, followedPostIds);
        }
        throw new InvalidSortingTypeException();
    }

    private GetPostsList findByDateSorted(
            Sports sports,
            Gender gender,
            LocalDate date,
            Pageable pageable,
            Set<Long> followedPostIds
    ) {
        Page<Post> page = postRepository.findByFilters(sports, gender, date, pageable);

        List<Long> ids = extractIds(page.getContent());
        Map<Long, Long> counts = postViewService.getViewCounts(ids);
        Map<Long, Integer> currentPeople = buildCurrentPeopleMap(ids);

        List<GetPost> dtos = GetPost.of(page.getContent(), counts, currentPeople, followedPostIds);
        return GetPostsList.of(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private GetPostsList findByPopularitySorted(
            Sports sports,
            Gender gender,
            LocalDate date,
            Pageable pageable,
            Set<Long> followedPostIds
    ) {
        long totalPopularCount = postViewService.getPopularPostCount();
        if (totalPopularCount == 0L) {
            return GetPostsList.of(Collections.emptyList(), pageable.getPageNumber(), pageable.getPageSize(), 0L, 0);
        }

        List<Long> allPopularIds = postViewService.getPopularPostIds(0, totalPopularCount - 1);
        if (allPopularIds.isEmpty()) {
            return GetPostsList.of(Collections.emptyList(), pageable.getPageNumber(), pageable.getPageSize(), 0L, 0);
        }

        List<Post> filteredPosts = postRepository.findByFiltersAndIds(sports, gender, date, allPopularIds);
        Map<Long, Post> postMap = filteredPosts.stream()
                .collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a));
        List<Post> sortedPosts = new ArrayList<>();
        for (Long id : allPopularIds) {
            Post p = postMap.get(id);
            if (p != null) sortedPosts.add(p);
        }

        int pageSize = pageable.getPageSize();
        int pageStartIndex = pageable.getPageNumber() * pageSize;
        List<Post> pageContent = pageStartIndex >= sortedPosts.size()
                ? Collections.emptyList()
                : sortedPosts.subList(pageStartIndex, Math.min(sortedPosts.size(), pageStartIndex + pageSize));

        long totalElements = sortedPosts.size();
        int totalPages = totalElements == 0L ? 0 : (int) Math.ceil((double) totalElements / pageSize);

        List<Long> pageIds = extractIds(pageContent);
        Map<Long, Long> counts = postViewService.getViewCounts(pageIds);
        Map<Long, Integer> currentPeople = buildCurrentPeopleMap(pageIds);
        List<GetPost> dtos = GetPost.of(pageContent, counts, currentPeople, followedPostIds);

        return GetPostsList.of(dtos, pageable.getPageNumber(), pageable.getPageSize(), totalElements, totalPages);
    }

    @Transactional(readOnly = true)
    public GetPostsCalender findByMonth(YearMonth month) {
        validateNotPastMonth(month);

        LocalDate startDate = determineStartDate(month);
        LocalDateTime fromDate = startDate.atStartOfDay();
        LocalDateTime toDate = month.atEndOfMonth().atTime(LocalTime.MAX);

        List<Post> posts = postRepository.findAllByDateBetween(fromDate, toDate);
        Map<LocalDate, Map<Sports, List<Post>>> grouped = groupByDateAndSport(posts);
        List<GetPostCalender> calendarEntries = toCalendarEntries(grouped);

        return GetPostsCalender.from(calendarEntries);
    }

    @Transactional
    public Post create(PostRequestDto dto, MultipartFile image, CustomUserDetails userDetails) {
        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            if (!s3Service.isValidImageFile(image)) {
                throw new RuntimeException("이미지 파일만 업로드 가능합니다.");
            }
            try {
                imageUrl = s3Service.uploadFile(image);
            } catch (Exception ex) {
                throw new RuntimeException("이미지 업로드에 실패했습니다.", ex);
            }
        }

        User currentUser = userDetails.getUser();
        Post post = dto.toEntity(currentUser);
        post.setImageUrl(imageUrl);

        Post saved = postRepository.save(post);

        Participation authorParticipation = new Participation(currentUser, saved);
        authorParticipation.setStatus(ApplicationStatus.APPROVED);
        participationRepository.save(authorParticipation);

        return saved;
    }

    @Transactional
    public PostInfoResponseDto searchPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        int currentParticipantsCount =
                participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);

        if (currentParticipantsCount >= post.getMaxPeople()) {
            post.setStatus(Status.CLOSED);
            postRepository.save(post);
        }

        boolean isBookmarked = false;
        if (userId != null) {
            postViewService.recordView(postId, userId);
            isBookmarked = followRepository.existsByUserIdAndPostId(userId, postId);
        }

        return new PostInfoResponseDto(post, currentParticipantsCount, isBookmarked);
    }

    @Transactional(readOnly = true)
    public GetMyPosts getMyPosts(CustomUserDetails userDetails) {
        User currentUser = userDetails.getUser();
        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());

        List<GetMyPost> myPosts = new ArrayList<>(posts.size());
        for (Post post : posts) {
            String key = applicantKey(post.getId());
            String cachedValue = redisTemplate.opsForValue().get(key);

            int currentPeople = cachedValue != null
                    ? Integer.parseInt(cachedValue)
                    : participationRepository.countByPost_IdAndStatus(post.getId(), ApplicationStatus.APPROVED);

            myPosts.add(new GetMyPost(
                    post.getId(),
                    post.getTitle(),
                    post.getDate(),
                    currentPeople,
                    post.getMaxPeople(),
                    post.getStatus().name()
            ));
        }

        return GetMyPosts.from(myPosts);
    }

    @Transactional
    public UpdatePostResponseDto updatePost(
            Long postId,
            UpdatePostRequestDto request,
            MultipartFile image,
            CustomUserDetails userDetails
    ) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        User currentUser = userDetails.getUser();
        if (!Objects.equals(post.getUser().getId(), currentUser.getId())) {
            throw new UnauthorizedUserException();
        }

        LocalDateTime now = LocalDateTime.now();
        if (post.getDate().isBefore(now)) {
            throw new PastEventModificationException();
        }

        String newImageUrl = post.getImageUrl();

        if (image != null && !image.isEmpty()) {
            if (!s3Service.isValidImageFile(image)) {
                throw new RuntimeException("이미지 파일만 업로드 가능합니다.");
            }
            try {
                newImageUrl = s3Service.uploadFile(image);
                if (post.getImageUrl() != null) {
                    s3Service.deleteFileByUrl(post.getImageUrl());
                }
            } catch (Exception ex) {
                throw new RuntimeException("이미지 업로드에 실패했습니다.", ex);
            }
        } else if (Boolean.TRUE.equals(request.getRemoveImage())) {
            if (post.getImageUrl() != null) {
                try {
                    s3Service.deleteFileByUrl(post.getImageUrl());
                    newImageUrl = null;
                } catch (Exception ex) {
                    throw new RuntimeException("이미지 삭제에 실패했습니다.", ex);
                }
            }
        }

        post.setTitle(Objects.requireNonNull(request.getTitle()));
        post.setDescription(Objects.requireNonNull(request.getDescription()));
        post.setLocation(Objects.requireNonNull(request.getLocation()));
        post.setDate(Objects.requireNonNull(request.getDate()));
        post.setMaxPeople(Objects.requireNonNull(request.getMaxPeople()));
        post.setGender(Objects.requireNonNull(request.getGender()));
        post.setCost(Objects.requireNonNull(request.getCost()));
        post.setImageUrl(newImageUrl);
        post.setSports(Objects.requireNonNull(request.getSports()));
        post.setTown(Objects.requireNonNull(request.getTown()));

        int currentParticipantsCount =
                participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED);
        if (currentParticipantsCount >= post.getMaxPeople()) {
            post.setStatus(Status.CLOSED);
        } else if (post.getStatus() == Status.CLOSED && currentParticipantsCount < post.getMaxPeople()) {
            post.setStatus(Status.OPEN);
        }

        Post updatedPost = postRepository.save(post);
        return UpdatePostResponseDto.from(updatedPost);
    }

    public Map<Long, Integer> buildCurrentPeopleMap(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();

        Map<Long, Integer> currentPeopleMap = new HashMap<>(ids.size());
        List<String> keys = new ArrayList<>(ids.size());
        Map<String, Long> keyToPostId = new HashMap<>(ids.size());

        for (Long postId : ids) {
            String key = String.format(APPLICANT_KEY_FMT, postId);
            keys.add(key);
            keyToPostId.put(key, postId);
            currentPeopleMap.put(postId, 0);
        }

        List<Long> missingIds = new ArrayList<>();
        List<String> values = null;

        try {
            values = redisTemplate.opsForValue().multiGet(keys);
        } catch (Exception ex) {
            log.warn("Redis multiGet failed, falling back to DB for all ids: {}", ex.getMessage());
            missingIds.addAll(ids);
        }

        if (values != null) {
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                String value = values.get(i);
                Long postId = keyToPostId.get(key);

                if (value == null) {
                    if (postId != null) missingIds.add(postId);
                    continue;
                }

                try {
                    int parsed = Integer.parseInt(value);
                    if (postId != null) currentPeopleMap.put(postId, parsed);
                } catch (NumberFormatException ex) {
                    log.warn("Invalid redis value for key {}: {}, fallback to DB", key, value);
                    if (postId != null) missingIds.add(postId);
                }
            }
        }

        if (!missingIds.isEmpty()) {
            List<Object[]> approvedCounts =
                    participationRepository.countApprovedByPostIds(missingIds, ApplicationStatus.APPROVED);
            Set<Long> touched = new HashSet<>();

            for (Object[] row : approvedCounts) {
                Long postId = (Long) row[0];
                Long approvedCount = (Long) row[1];
                int count = approvedCount.intValue();
                currentPeopleMap.put(postId, count);
                touched.add(postId);

                String key = String.format(APPLICANT_KEY_FMT, postId);
                try {
                    redisTemplate.opsForValue().setIfAbsent(
                            key,
                            Integer.toString(count),
                            Duration.ofMinutes(APPLICANT_KEY_TTL_MINUTES)
                    );
                } catch (Exception ex) {
                    log.warn("Failed to set redis key {} to {}: {}", key, count, ex.getMessage());
                }
            }

            for (Long postId : missingIds) {
                if (!touched.contains(postId)) {
                    currentPeopleMap.put(postId, 0);
                    String key = String.format(APPLICANT_KEY_FMT, postId);
                    try {
                        redisTemplate.opsForValue().setIfAbsent(
                                key,
                                "0",
                                Duration.ofMinutes(APPLICANT_KEY_TTL_MINUTES)
                        );
                    } catch (Exception ex) {
                        log.debug("Failed to set redis key {} to 0: {}", key, ex.getMessage());
                    }
                }
            }
        }

        return currentPeopleMap;
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
        return month.equals(current) ? LocalDate.now() : month.atDay(1);
    }

    private Map<LocalDate, Map<Sports, List<Post>>> groupByDateAndSport(List<Post> posts) {
        Map<LocalDate, Map<Sports, List<Post>>> grouped = new LinkedHashMap<>();
        for (Post p : posts) {
            LocalDate day = p.getDate().toLocalDate();
            grouped.computeIfAbsent(day, k -> new LinkedHashMap<>())
                    .computeIfAbsent(p.getSports(), k -> new ArrayList<>())
                    .add(p);
        }
        return grouped;
    }

    private List<GetPostCalender> toCalendarEntries(Map<LocalDate, Map<Sports, List<Post>>> grouped) {
        List<GetPostCalender> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Map<Sports, List<Post>>> dayEntry : grouped.entrySet()) {
            for (Map.Entry<Sports, List<Post>> sportEntry : dayEntry.getValue().entrySet()) {
                List<Post> postList = sportEntry.getValue();
                List<String> times = postList.stream()
                        .map(p -> p.getDate().toLocalTime().toString())
                        .sorted()
                        .collect(Collectors.toList());

                result.add(new GetPostCalender(
                        dayEntry.getKey().toString(),
                        new GetPostCalender.Event(
                                sportEntry.getKey().getLabel(),
                                postList.size(),
                                times
                        )
                ));
            }
        }
        result.sort(Comparator.comparing(GetPostCalender::getDay));
        return result;
    }

    private List<Long> extractIds(List<Post> posts) {
        return posts.stream().map(Post::getId).collect(Collectors.toList());
    }

    /**
     * 모집글 삭제.
     * <p>트랜잭션 원칙: AUTHORIZED 결제를 PG void 먼저 처리(트랜잭션 없음)한 후,
     * DB 레코드(Payment → Participation → Follow → Post)를 단일 트랜잭션으로 삭제한다.</p>
     */
    public void deleteMyPost(Long postId, CustomUserDetails userDetails) {
        Long currentUserId = userDetails.getUserId();
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        if (!Objects.equals(post.getUser().getId(), currentUserId)) {
            throw new UnauthorizedUserException();
        }

        // 1. CAPTURED 결제 PG 환불 + CANCELLED 표시 (트랜잭션 없음)
        paymentService.voidAllCapturedByPost(postId);

        // 2. S3 이미지 삭제 (외부 호출, 트랜잭션 없음)
        String imageUrl = post.getImageUrl();
        if (imageUrl != null && !imageUrl.isBlank()) {
            s3Service.deleteByUrl(imageUrl);
        }

        // 3. DB 레코드 원자적 삭제
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            paymentRepository.deleteByPost_Id(postId);
            participationRepository.deleteByPostId(postId);
            followRepository.deleteByPostId(postId);
            postRepository.delete(post);
        });
    }

    @Scheduled(cron = "0 0/10 * * * *")
    public void expirePosts() {
        LocalDateTime now = LocalDateTime.now();

        // OPEN(정원 미달) → CAPTURED 결제 환불 후 EXPIRED
        List<Long> openIds = postRepository.findExpiredCandidateIds(now, Status.OPEN);
        if (!openIds.isEmpty()) {
            paymentService.refundByPostIds(openIds);
        }

        // CLOSED(정원 충족) → 결제는 이미 CAPTURED, 상태만 EXPIRED로 전환
        postRepository.markExpired(now, Status.EXPIRED, Status.CLOSED);
        postRepository.markExpired(now, Status.EXPIRED, Status.OPEN);
    }

    private String applicantKey(long postId) {
        return String.format(APPLICANT_KEY_FMT, postId);
    }
}
