package com.matchFit.post.service

import com.matchFit.follow.repository.FollowRepository
import com.matchFit.participation.entity.ApplicationStatus
import com.matchFit.participation.entity.Participation
import com.matchFit.participation.repository.ParticipationRepository
import com.matchFit.post.dto.PostInfoResponseDto
import com.matchFit.post.dto.PostRequestDto
import com.matchFit.post.dto.UpdatePostRequestDto
import com.matchFit.post.dto.UpdatePostResponseDto
import com.matchFit.post.dto.response.GetMyPost
import com.matchFit.post.dto.response.GetMyPosts
import com.matchFit.post.dto.response.GetPost
import com.matchFit.post.dto.response.GetPostCalender
import com.matchFit.post.dto.response.GetPostsCalender
import com.matchFit.post.dto.response.GetPostsList
import com.matchFit.post.entity.Post
import com.matchFit.post.entity.SortType
import com.matchFit.post.entity.Sports
import com.matchFit.post.entity.Status
import com.matchFit.post.exception.InvalidSortingTypeException
import com.matchFit.post.exception.PastDayException
import com.matchFit.post.exception.PastEventModificationException
import com.matchFit.post.exception.PastMonthException
import com.matchFit.post.exception.PostNotFoundException
import com.matchFit.post.exception.UnauthorizedUserException
import com.matchFit.post.repository.PostRepository
import com.matchFit.s3.service.S3Service
import com.matchFit.user.entity.Gender
import com.matchFit.user.entity.User
import com.matchFit.user.security.CustomUserDetails
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth


@Transactional
@Service
class PostService(
    private val followRepository: FollowRepository,
    private val participationRepository: ParticipationRepository,
    private val postRepository: PostRepository,
    private val postViewService: PostViewService,
    private val s3Service: S3Service,
    private val redisTemplate: StringRedisTemplate
) {
    private val log = LoggerFactory.getLogger(PostService::class.java)

    fun findByFilters(
        sports: Sports?,
        gender: Gender?,
        sortType: SortType,
        date: LocalDate?,
        pageable: Pageable,
        userId: Long? = null
    ): GetPostsList {
        if (date != null) validateNotPastDate(date)

        val followedPostIds = if (userId != null) followRepository.findPostIdsByUserId(userId) else emptySet()

        return when (sortType) {
            SortType.DATE -> findByDateSorted(sports, gender, date, pageable, followedPostIds)
            SortType.POPULAR -> findByPopularitySorted(sports, gender, date, pageable, followedPostIds)
            else -> throw InvalidSortingTypeException()
        }
    }

    private fun findByDateSorted(
        sports: Sports?,
        gender: Gender?,
        date: LocalDate?,
        pageable: Pageable,
        followedPostIds: Set<Long>
    ): GetPostsList {
        val page: Page<Post> = postRepository.findByFilters(sports, gender, date, pageable)

        val ids = extractIds(page.content)
        val counts = postViewService.getViewCounts(ids)
        val currentPeople = buildCurrentPeopleMap(ids)

        val dtos = GetPost.of(page.content, counts, currentPeople, followedPostIds)
        return GetPostsList.of(dtos, page.number, page.size, page.totalElements, page.totalPages)
    }

    private fun findByPopularitySorted(
        sports: Sports?,
        gender: Gender?,
        date: LocalDate?,
        pageable: Pageable,
        followedPostIds: Set<Long>
    ): GetPostsList {
        val totalPopularCount = postViewService.getPopularPostCount()
        if (totalPopularCount == 0L) {
            return GetPostsList.of(emptyList(), pageable.pageNumber, pageable.pageSize, 0, 0)
        }

        val allPopularIds = postViewService.getPopularPostIds(0, totalPopularCount - 1)
        if (allPopularIds.isEmpty()) {
            return GetPostsList.of(emptyList(), pageable.pageNumber, pageable.pageSize, 0, 0)
        }

        val filteredPosts = postRepository.findByFiltersAndIds(sports, gender, date, allPopularIds)
        val postMap = filteredPosts.associateBy { it.id!! }
        val sortedPosts = allPopularIds.mapNotNull { postMap[it] }

        val pageSize = pageable.pageSize
        val pageStartIndex = pageable.pageNumber * pageSize
        val pageContent = sortedPosts.drop(pageStartIndex).take(pageSize)

        val totalElements = sortedPosts.size.toLong()
        val totalPages = if (totalElements == 0L) 0 else kotlin.math.ceil(totalElements.toDouble() / pageSize).toInt()

        val pageIds = extractIds(pageContent)
        val counts = postViewService.getViewCounts(pageIds)
        val currentPeople = buildCurrentPeopleMap(pageIds)
        val dtos = GetPost.of(pageContent, counts, currentPeople, followedPostIds)

        return GetPostsList.of(dtos, pageable.pageNumber, pageable.pageSize, totalElements, totalPages)
    }

    fun findByMonth(month: YearMonth): GetPostsCalender {
        validateNotPastMonth(month)

        val startDate = determineStartDate(month)
        val fromDate = startDate.atStartOfDay()
        val toDate = month.atEndOfMonth().atTime(LocalTime.MAX)

        val posts = postRepository.findAllByDateBetween(fromDate, toDate)
        val grouped = groupByDateAndSport(posts)
        val calendarEntries = toCalendarEntries(grouped)

        return GetPostsCalender.from(calendarEntries)
    }

    fun create(dto: PostRequestDto, image: MultipartFile?, @AuthenticationPrincipal userDetails: CustomUserDetails): Post {
        var imageUrl: String? = null

        if (image != null && !image.isEmpty) {
            if (!s3Service.isValidImageFile(image)) {
                throw RuntimeException("이미지 파일만 업로드 가능합니다.")
            }

            try {
                imageUrl = s3Service.uploadFile(image)
            } catch (ex: Exception) {
                throw RuntimeException("이미지 업로드에 실패했습니다.", ex)
            }
        }

        val currentUser = userDetails.user
        val post = dto.toEntity(currentUser)
        post.imageUrl = imageUrl

        val saved = postRepository.save(post)

        val authorParticipation = Participation(currentUser, saved)
        authorParticipation.status = ApplicationStatus.APPROVED
        participationRepository.save(authorParticipation)

        return saved
    }

    fun searchPost(postId: Long, userId: Long?): PostInfoResponseDto {
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException() }

        val currentParticipantsCount =
            participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED)

        if (currentParticipantsCount >= post.maxPeople) {
            post.status = Status.CLOSED
            postRepository.save(post)
        }

        var isBookmarked = false
        if (userId != null) {
            postViewService.recordView(postId, userId)
            isBookmarked = followRepository.existsByUserIdAndPostId(userId, postId)
        }

        return PostInfoResponseDto(post, currentParticipantsCount, isBookmarked)
    }

    @Transactional(readOnly = true)
    fun getMyPosts(@AuthenticationPrincipal userDetails: CustomUserDetails): GetMyPosts {
        val currentUser: User = userDetails.user
        val posts = postRepository.findByUserIdOrderByCreatedAtDesc(currentUser.id!!)

        val myPosts = posts.map { post ->
            val key = applicantKey(post.id!!)
            val cachedValue = redisTemplate.opsForValue().get(key)

            val currentPeople = if (cachedValue != null) {
                cachedValue.toInt()
            } else {
                participationRepository.countByPost_IdAndStatus(post.id!!, ApplicationStatus.APPROVED).toInt()
            }

            GetMyPost(
                post.id!!,
                post.title,
                post.date,
                currentPeople,
                post.maxPeople,
                post.status.name
            )
        }

        return GetMyPosts.from(myPosts)
    }

    fun updatePost(
        postId: Long,
        request: UpdatePostRequestDto,
        image: MultipartFile?,
        userDetails: CustomUserDetails
    ): UpdatePostResponseDto {
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException() }

        val currentUser = userDetails.user
        if (post.user.id != currentUser.id) {
            throw UnauthorizedUserException()
        }

        val now = LocalDateTime.now()
        if (post.date.isBefore(now)) {
            throw PastEventModificationException()
        }

        var newImageUrl = post.imageUrl

        if (image != null && !image.isEmpty) {
            if (!s3Service.isValidImageFile(image)) {
                throw RuntimeException("이미지 파일만 업로드 가능합니다.")
            }

            try {
                newImageUrl = s3Service.uploadFile(image)
                if (post.imageUrl != null) {
                    s3Service.deleteFileByUrl(post.imageUrl)
                }
            } catch (ex: Exception) {
                throw RuntimeException("이미지 업로드에 실패했습니다.", ex)
            }
        } else if (request.removeImage == true) {
            if (post.imageUrl != null) {
                try {
                    s3Service.deleteFileByUrl(post.imageUrl)
                    newImageUrl = null
                } catch (ex: Exception) {
                    throw RuntimeException("이미지 삭제에 실패했습니다.", ex)
                }
            }
        }

        post.title = requireNotNull(request.title)
        post.description = requireNotNull(request.description)
        post.location = requireNotNull(request.location)
        post.date = requireNotNull(request.date)
        post.maxPeople = requireNotNull(request.maxPeople)
        post.gender = requireNotNull(request.gender)
        post.cost = requireNotNull(request.cost)
        post.imageUrl = newImageUrl
        post.sports = requireNotNull(request.sports)
        post.town = requireNotNull(request.town)

        val currentParticipantsCount =
            participationRepository.countByPost_IdAndStatus(postId, ApplicationStatus.APPROVED)
        if (currentParticipantsCount >= post.maxPeople) {
            post.status = Status.CLOSED
        } else if (post.status == Status.CLOSED && currentParticipantsCount < post.maxPeople) {
            post.status = Status.OPEN
        }

        val updatedPost = postRepository.save(post)
        return UpdatePostResponseDto.from(updatedPost)
    }

    fun buildCurrentPeopleMap(ids: List<Long>?): Map<Long, Int> {
        if (ids.isNullOrEmpty()) return emptyMap()

        val currentPeopleMap = HashMap<Long, Int>(ids.size)
        val keys = ArrayList<String>(ids.size)
        val keyToPostId = HashMap<String, Long>(ids.size)

        for (postId in ids) {
            val key = String.format(APPLICANT_KEY_FMT, postId)
            keys.add(key)
            keyToPostId[key] = postId
            currentPeopleMap[postId] = 0
        }

        val missingIds = ArrayList<Long>()
        var values: List<String?>? = null

        try {
            values = redisTemplate.opsForValue().multiGet(keys)
        } catch (ex: Exception) {
            log.warn("Redis multiGet failed, falling back to DB for all ids: {}", ex.message)
            missingIds.addAll(ids)
            values = null
        }

        if (values != null) {
            for (i in keys.indices) {
                val key = keys[i]
                val value = values[i]
                val postId = keyToPostId[key]

                if (value == null) {
                    if (postId != null) {
                        missingIds.add(postId)
                    }
                    continue
                }

                try {
                    val parsed = value.toInt()
                    if (postId != null) {
                        currentPeopleMap[postId] = parsed
                    }
                } catch (ex: NumberFormatException) {
                    log.warn("Invalid redis value for key {}: {}, fallback to DB", key, value)
                    if (postId != null) {
                        missingIds.add(postId)
                    }
                }
            }
        }

        if (missingIds.isNotEmpty()) {
            val approvedCounts = participationRepository.countApprovedByPostIds(missingIds, ApplicationStatus.APPROVED)
            val touched = HashSet<Long>()

            for (row in approvedCounts) {
                val postId = row[0] as Long
                val approvedCount = row[1] as Long

                val count = approvedCount.toInt()
                currentPeopleMap[postId] = count
                touched.add(postId)

                val key = String.format(APPLICANT_KEY_FMT, postId)
                try {
                    redisTemplate.opsForValue().setIfAbsent(
                        key,
                        count.toString(),
                        Duration.ofMinutes(APPLICANT_KEY_TTL_MINUTES.toLong())
                    )
                } catch (ex: Exception) {
                    log.warn("Failed to set redis key {} to {}: {}", key, count, ex.message)
                }
            }

            for (postId in missingIds) {
                if (!touched.contains(postId)) {
                    currentPeopleMap[postId] = 0
                    val key = String.format(APPLICANT_KEY_FMT, postId)
                    try {
                        redisTemplate.opsForValue().setIfAbsent(
                            key,
                            "0",
                            Duration.ofMinutes(APPLICANT_KEY_TTL_MINUTES.toLong())
                        )
                    } catch (ex: Exception) {
                        log.debug("Failed to set redis key {} to 1: {}", key, ex.message)
                    }
                }
            }
        }

        return currentPeopleMap
    }

    private fun validateNotPastMonth(month: YearMonth) {
        val currentMonth = YearMonth.now()
        if (month.isBefore(currentMonth)) {
            throw PastMonthException()
        }
    }

    private fun validateNotPastDate(date: LocalDate) {
        val today = LocalDate.now()
        if (date.isBefore(today)) {
            throw PastDayException()
        }
    }

    private fun determineStartDate(month: YearMonth): LocalDate {
        val current = YearMonth.from(LocalDate.now())
        return if (month == current) LocalDate.now() else month.atDay(1)
    }

    private fun groupByDateAndSport(posts: List<Post>): Map<LocalDate, Map<Sports, List<Post>>> {
        return posts.groupBy { it.date.toLocalDate() }
            .mapValues { entry -> entry.value.groupBy { it.sports } }
    }

    private fun toCalendarEntries(grouped: Map<LocalDate, Map<Sports, List<Post>>>): List<GetPostCalender> {
        return grouped.entries.flatMap { dayEntry ->
            dayEntry.value.entries.map { sportEntry ->
                val postList = sportEntry.value
                val times = postList.map { it.date.toLocalTime().toString() }.sorted()

                GetPostCalender(
                    dayEntry.key.toString(),
                    GetPostCalender.Event(
                        sportEntry.key.label,
                        postList.size,
                        times
                    )
                )
            }
        }.sortedBy { it.day }
    }

    private fun extractIds(posts: List<Post>): List<Long> {
        return posts.map { it.id!! }
    }

    fun deleteMyPost(postId: Long, userDetails: CustomUserDetails) {
        val currentUserId = userDetails.userId

        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException() }

        if (post.user.id != currentUserId) {
            throw UnauthorizedUserException()
        }

        participationRepository.deleteByPostId(postId)
        followRepository.deleteByPostId(postId)

        if (!post.imageUrl.isNullOrBlank()) {
            s3Service.deleteByUrl(post.imageUrl)
        }

        postRepository.delete(post)
    }

    @Scheduled(cron = "0 0/10 * * * *")
    fun expirePosts() {
        val now = LocalDateTime.now()
        postRepository.markExpired(now, Status.EXPIRED, Status.OPEN)
    }

    private fun applicantKey(postId: Long): String {
        return String.format(APPLICANT_KEY_FMT, postId)
    }

    companion object {
        private const val APPLICANT_KEY_FMT = "applicants:post_%d"
        private const val APPLICANT_KEY_TTL_MINUTES = 5
    }
}
