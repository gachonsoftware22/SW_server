# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

건강 데이터 기반 AI 분석 플랫폼. Java 17, Spring Boot 4.0.6, MySQL, Google Gemini API.

상세 설계는 `.claude/commands/build.md`를 source of truth로 참조한다. 코드 작성 전 반드시 확인할 것.

## 빌드 및 실행

```bash
./gradlew build        # 빌드 + 테스트
./gradlew bootRun      # 로컬 실행
./gradlew test         # 테스트만
./gradlew compileJava  # 컴파일만 (빠른 오류 확인)
```

## 필수 환경변수

Docker Compose로 주입. `.env` 파일 또는 `docker-compose.yml`에 설정해야 앱이 기동된다.

| 변수 | 용도 |
|------|------|
| `DB_URL` | MySQL JDBC URL (예: `jdbc:mysql://localhost:3306/sw_ai_doc`) |
| `DB_USERNAME` | DB 사용자명 |
| `DB_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET` | JJWT 서명 키 (최소 32자 이상 랜덤 문자열) |
| `GEMINI_API_KEY` | Google Gemini API 키 |

`hibernate.ddl-auto: update`이므로 앱 기동 시 테이블이 자동 생성/변경된다. 별도 마이그레이션 스크립트 불필요.

## 아키텍처 규칙

### 레이어 구조
모든 도메인은 `Controller → Service(인터페이스) → ServiceImpl → Repository → Entity` 구조를 따른다.
- Controller는 반드시 Service **인터페이스**에 의존한다 (DIP).
- ServiceImpl은 인터페이스를 구현하며 실제 비즈니스 로직을 처리한다.
- DTO와 Entity는 분리한다. Entity를 직접 반환하지 말 것.

### 응답 형식
모든 API 응답은 `ApiResponse<T>` 래퍼를 사용한다.

```json
{ "status": 200, "message": "...", "data": { } }
```

`ApiResponse.success(status, message, data)` / `ApiResponse.error(status, message)` 정적 팩토리 메서드 사용.

### 예외 처리
도메인별 커스텀 예외를 던지고, `GlobalExceptionHandler`(`@RestControllerAdvice`)가 `ApiResponse` 형식으로 일관되게 처리한다. 컨트롤러에서 직접 try-catch로 HTTP 응답을 만들지 말 것.

### Soft Delete
`Prescription` 삭제는 `status = DELETED`로 변경한다. `DELETE FROM` 쿼리를 사용하지 않는다.

## JWT 설계

| 토큰 | 만료 | 전달 방식 |
|------|------|---------|
| Access Token | 30분 | `Authorization: Bearer {token}` 헤더 |
| Refresh Token | 7일 | HttpOnly Cookie (DB에 SHA-256 hash만 저장) |

Refresh Token 원본은 클라이언트 Cookie에만 존재한다. 로그아웃/탈퇴 시 `revoked = true`로 폐기한다.

## 공개 엔드포인트 (SecurityConfig 기준)

인증 없이 접근 가능한 엔드포인트 (실제 구현 기준):
- `POST /api/member/signup`
- `POST /api/auth/login`
- `POST /api/auth/token/refresh`
- `GET /actuator/**`

> build.md에 명시된 `/api/member/find-id`, `/api/member/reset-password`는 **미구현** 상태.

## 도메인 및 컨트롤러 매핑

| 도메인 | 컨트롤러 | Base URL | 역할 |
|--------|---------|----------|------|
| auth | `AuthController` | `/api/auth` | 로그인, 로그아웃, 토큰 재발급, 비밀번호 확인 |
| member | `MemberController` | `/api/member` | 회원가입, 내 정보 CRUD, 탈퇴 |
| health | `HealthController` | `/api/health` | 날짜별 증상·병력·메모 기록 (AI 입력 데이터) |
| prescription | `PrescriptionController` | `/api/prescription` | 처방전 + 처방약 CRUD |
| ai | `AiController` | `/api/ai` | AI 분석 트리거 및 결과 조회 |

> **주의**: 로그인은 `AuthController(/api/auth/login)`이고 회원가입은 `MemberController(/api/member/signup)`이다. build.md의 일부 경로와 실제 구현이 다르므로 실제 코드를 우선한다.

## AI 파이프라인

`POST /api/ai/trigger` 호출 시 동기 처리 흐름:

1. `AiDatasetService` — health/prescription 데이터를 CSV 형식 DTO로 조합
2. `AiResultService` (→ `AiResultServiceImpl`) — Gemini API 호출, JSON 응답 파싱
3. `AiResultRepository` — `ai_result` 테이블에 저장

- userId는 요청 바디가 아닌 `@AuthenticationPrincipal`로 토큰에서 추출한다. 요청 바디 불필요.
- LLM 호출 실패 시 최대 3회 재시도 (Exponential Backoff: 1s→2s→4s). 파싱 실패 시 트랜잭션 롤백.
- `potential_diseases`, `recommended_foods`, `recommended_exercises`, `raw_llm_response`는 MySQL JSON 타입 컬럼이며 `StringListConverter`로 직렬화/역직렬화한다.
