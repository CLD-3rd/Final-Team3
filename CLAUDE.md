# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# 인프라 시작 (MySQL:3307, Redis:6379, Prometheus:9090, Grafana:3001)
docker-compose up -d

# 테스트 제외 빌드
./mvnw -B -DskipTests package

# 전체 빌드 (테스트 포함)
./mvnw clean package

# 전체 테스트 실행
./mvnw test

# 단일 테스트 클래스 실행
./mvnw test -Dtest=MatchFitApplicationTests

# 애플리케이션 실행 (포트 8080)
./mvnw spring-boot:run
```

QueryDSL Q클래스는 빌드 시 KAPT로 생성된다. Q클래스 관련 오류가 발생하면 `./mvnw clean package -DskipTests`로 재생성한다.

## Architecture Overview

Spring Boot 3 / Kotlin 기반의 표준 레이어드 아키텍처(Controller → Service → Repository)로 구성되어 있다. 모듈 분리 없이 모든 코드가 `src/main/kotlin/com/matchFit/` 하위에 위치한다.

```
com.matchFit/
├── user/           # 회원가입, JWT 인증, 카카오 OAuth, 이메일 비밀번호 재설정(Redis 토큰), 마이페이지
├── post/           # 스포츠 모집글 CRUD; 복잡한 필터링/정렬은 QueryDSL(PostRepositoryImpl) 사용
├── participation/  # 모집글 신청, 호스트의 승인/거절, 취소 가능 시간 제한 처리
├── follow/         # 유저 팔로우/언팔로우
├── s3/             # AWS S3 이미지 업로드
└── common/         # BaseEntity(감사 필드), ApiResponseDTO, ErrorCode/SuccessCode, GlobalExceptionHandler, TimingAspect
```

**주요 공통 패턴:**
- 모든 API 응답은 `ApiResponseDTO`로 래핑되며 `SuccessCode`/`ErrorCode` enum을 사용한다.
- 도메인 예외는 `GeneralException`을 상속하고 `GlobalExceptionHandler`에서 일괄 처리된다.
- Redis는 두 가지 목적으로 사용된다: 게시글 조회수 캐싱(`PostViewService`), 비밀번호 재설정 토큰 저장(`RedisPasswordResetToken`).
- Redis keyspace expiry 이벤트(`notify-keyspace-events Ex`)로 조회수 만료 시 MySQL에 반영한다.

## Tech Stack

```
Language     Kotlin 1.9.24 + Java 17
Framework    Spring Boot 3.5.3
ORM          Spring Data JPA + Hibernate (MySQL8Dialect)
복잡 쿼리    QueryDSL 5.1.0 (Jakarta) — KAPT로 Q클래스 생성
인증         Spring Security + JJWT 0.11.5 (stateless JWT) + 카카오 OAuth
캐시/메시징  Spring Data Redis
SQL 디버깅   P6Spy (JDBC URL 래핑: jdbc:p6spy:mysql://...)
스토리지     AWS S3 SDK 1.12.x
메트릭       Spring Actuator → Prometheus → Grafana
빌드         Maven + Kotlin Maven Plugin
```

## Database

- MySQL 8.0, 포트 **3307** (Docker 내부 3306 → 호스트 3307 매핑)
- DB명: `matchFit`, root 비밀번호: `root_password`
- `ddl-auto=update` 설정으로 Hibernate가 스키마를 자동 관리하며, 별도 마이그레이션 도구는 사용하지 않는다.
- QueryDSL 커스텀 쿼리는 `PostRepositoryImpl`에 구현되어 있으며 `PostRepositoryCustom` 인터페이스를 구현한다.

## Security

JWT는 stateless 방식이며 `JwtAuthenticationFilter`가 모든 요청에서 토큰을 검증한다. 공개/보호 라우트는 `SecurityConfig`에서 정의한다. CORS는 `WebConfig`에서 `localhost:3000`, `www.match-fit.store`, CloudFront 오리진을 허용한다.

## CI/CD

`.github/workflows/` 아래 두 개의 GitHub Actions 워크플로가 있다.

```
ci-ecr.yaml       멀티 아키텍처 Docker 이미지 빌드 → AWS ECR 푸시 → ArgoCD로 EKS 배포
ci-dockerhub.yaml 멀티 아키텍처 Docker 이미지 빌드 → DockerHub 푸시 → kubectl 배포
```

Docker 이미지는 eclipse-temurin:17-jre 기반 단일 스테이지로 빌드된다.

## Code Conventions

**네이밍 규칙:**
```
클래스/인터페이스   PascalCase         (PostService, FollowController)
함수/변수           camelCase          (findByFilters, currentPeople)
상수                SCREAMING_SNAKE_CASE (companion object 내 const val)
패키지              camelCase          (com.matchFit.post)
Redis 키            콜론(:) 구분자     (applicants:post_1, view:post_1)
```

**레이어별 패턴:**

- **Controller**: 모든 핸들러는 `ResponseEntity<ApiResponseDTO<T>>`를 반환한다. 성공 응답은 반드시 `ApiResponseDTO.onSuccess(SuccessCode.XXX, data)` 형식을 사용한다.
- **Service**: 클래스 레벨에 `@Transactional`을 선언하고, 조회 전용 메서드에만 `@Transactional(readOnly = true)`를 별도 지정한다.
- **예외**: 새 예외 클래스는 반드시 `GeneralException(ErrorCode.XXX)`를 상속해 한 줄로 작성한다. `ErrorCode` enum에 `도메인코드+순번` 형식(예: `POST402`, `USER404`)으로 항목을 추가하고 한국어 메시지를 작성한다.

**엔티티 작성:**
- `BaseEntity`를 상속해 `createdAt`/`updatedAt` 감사 필드를 자동 포함한다.
- non-null JPA 필드는 `lateinit var`로 선언하고, PK는 `var id: Long? = null`로 선언한다.
- Enum 컬럼에는 반드시 `@Enumerated(EnumType.STRING)`을 붙인다.

**DTO 작성:**
- Request DTO: 일반 `class`에 `var` 필드로 선언하고 `toEntity()` 변환 메서드를 포함한다.
- Response DTO: `data class`로 선언하고, 생성은 `companion object` 내 `from(...)` 또는 `of(...)` factory 메서드를 통해서만 한다.

## Agent Rules

Complex tasks (new feature / refactor / bug fix) → mandatory pipeline:
1. @researcher → codebase analysis
2. @planner → implementation plan (no code)
3. implement
4. @reviewer → review only (no fix)

Skip pipeline: typo, comment, simple constant change