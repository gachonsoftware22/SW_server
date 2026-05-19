# API 명세서

Base URL: `/api`  
인증: `Authorization: Bearer {accessToken}` 헤더  
공통 응답: `{ "status": 200, "message": "...", "data": { } }`

---

## Auth `/api/auth`

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | `/auth/login` | X | 로그인 |
| POST | `/auth/logout` | O | 로그아웃 |
| POST | `/auth/token/refresh` | Cookie | Access Token 재발급 |
| POST | `/auth/verify-password` | O | 비밀번호 본인 확인 |

### POST /auth/login
```json
// Request
{ "loginId": "user123", "password": "Abcd1234!" }

// Response 200  (+Set-Cookie: refreshToken=...; HttpOnly)
{ "status": 200, "message": "로그인 성공", "data": { "accessToken": "eyJ...", "name": "홍길동" } }
```
오류: `401` 비밀번호 불일치 · `423` 계정 잠금 · `404` 아이디 없음

### POST /auth/token/refresh
Refresh Token은 Cookie에서 자동 추출. 요청 바디 없음.
```json
// Response 200
{ "status": 200, "message": "토큰 재발급 성공", "data": { "accessToken": "eyJ..." } }
```

### POST /auth/verify-password
```json
// Request
{ "password": "Abcd1234!" }

// Response 200
{ "status": 200, "message": "본인 확인 성공", "data": null }
```

---

## Member `/api/member`

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | `/member/signup` | X | 회원가입 |
| GET | `/member/me` | O | 내 정보 조회 |
| PUT | `/member/me` | O | 내 정보 수정 |
| DELETE | `/member/me` | O | 회원 탈퇴 |

### POST /member/signup
```json
// Request
{
  "loginId": "user123",       // 필수, UNIQUE
  "password": "Abcd1234!",    // 필수, 영문+숫자+특수 8자↑
  "name": "홍길동",            // 필수
  "email": "test@test.com",   // 필수, UNIQUE
  "phone": "010-1234-5678",   // 선택
  "birthDate": "2003-05-10",  // 필수
  "gender": "M"               // 선택 (M/F)
}

// Response 200
{ "status": 200, "message": "회원가입 완료", "data": { "userId": 1, "loginId": "user123" } }
```
오류: `409` loginId 중복 · `409` email 중복 · `400` 유효성 실패

### GET /member/me
```json
// Response 200
{
  "status": 200, "message": "조회 성공",
  "data": {
    "userId": 1, "loginId": "user123", "name": "홍길동",
    "email": "test@test.com", "phone": "010-1234-5678",
    "birthDate": "2003-05-10", "gender": "M",
    "createdAt": "2026-05-01T10:00:00", "updatedAt": null
  }
}
```

### PUT /member/me
변경할 필드만 포함.
```json
// Request
{ "name": "김철수", "phone": "010-9999-8888", "newPassword": "Newpass1!" }

// Response 200
{ "status": 200, "message": "회원 정보가 수정되었습니다.", "data": null }
```

### DELETE /member/me
```json
// Request
{ "reason": "서비스 이용 안함" }  // 선택

// Response 200
{ "status": 200, "message": "회원 탈퇴가 완료되었습니다.", "data": null }
```

---

## Health `/api/health`

> 모든 엔드포인트 인증 필요. 본인 데이터만 접근 가능.

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/health/create` | 건강 기록 생성 |
| GET | `/health/list` | 목록 조회 (최신순) |
| GET | `/health/{healthId}` | 단건 조회 |
| PUT | `/health/update/{healthId}` | 수정 |
| DELETE | `/health/delete/{healthId}` | 삭제 |

### POST /health/create
```json
// Request
{
  "symptom": "두통, 발열",    // 필수
  "history": "고혈압 병력",   // 선택
  "note": "어제부터 시작",    // 선택
  "recordDate": "2026-05-01"  // 필수
}

// Response 201
{
  "status": 201, "message": "건강 기록이 생성되었습니다.",
  "data": { "healthId": 1, "userId": 42, "symptom": "두통, 발열", "recordDate": "2026-05-01", "createdAt": "..." }
}
```

---

## Prescription `/api/prescription`

> 모든 엔드포인트 인증 필요. 삭제는 Soft Delete (`status = DELETED`).

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/prescription/create` | 처방전 생성 |
| GET | `/prescription/list` | 목록 조회 |
| GET | `/prescription/{prescriptionId}` | 상세 조회 (약 목록 포함) |
| PUT | `/prescription/update/{prescriptionId}` | 수정 |
| DELETE | `/prescription/delete/{prescriptionId}` | 삭제 |

### POST /prescription/create
```json
// Request
{
  "prescriptionDate": "2026-04-26",
  "hospitalName": "서울병원",
  "medicineList": [
    { "medicineName": "타이레놀", "dosage": "1일 2회", "duration": "5일" }
  ]
}

// Response 201
{ "status": 201, "message": "처방전이 생성되었습니다.", "data": { "prescriptionId": 1, ... } }
```

---

## AI 분석 `/api/ai`

> 모든 엔드포인트 인증 필요. 본인 데이터만 접근 가능.

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/ai/trigger` | AI 분석 실행 (동기) |
| GET | `/ai/result/me` | 최신 분석 결과 전체 |
| GET | `/ai/result/me/summary` | 요약 (healthStatus, summaryNote, analysisDate) |
| GET | `/ai/result/me/diseases` | 추정 질환 목록 |
| GET | `/ai/result/me/foods` | 권장 식품 목록 |
| GET | `/ai/result/me/exercises` | 권장 운동 목록 |
| GET | `/ai/result/me/precautions` | 주의사항 |

### POST /ai/trigger
요청 바디 없음. 토큰에서 userId 자동 추출.
```json
// Response 200
{
  "status": 200, "message": "AI 분석 완료",
  "data": {
    "resultId": 15, "userId": 42,
    "healthStatus": "CAUTION",
    "summaryNote": "최근 두통과 피로감이 지속되고 있습니다.",
    "potentialDiseases": ["고혈압", "만성피로증후군"],
    "recommendedFoods": ["바나나", "견과류"],
    "recommendedExercises": ["걷기", "스트레칭"],
    "precautions": "카페인 섭취를 줄이고 충분한 수면을 취하십시오.",
    "analysisDate": "2026-05-19T10:30:00"
  }
}
```

| healthStatus | 의미 |
|---|---|
| `GOOD` | 건강 상태 양호 |
| `CAUTION` | 주의 필요 |
| `WARNING` | 위험, 즉각 조치 권장 |

오류: `502` Gemini API 3회 재시도 실패 · `500` AI 응답 파싱 실패 · `204` 분석 결과 없음

---

## 공통 에러 코드

| HTTP | 상황 |
|------|------|
| 400 | 필수값 누락 또는 유효성 검사 실패 |
| 401 | 인증 토큰 없음, 만료, 또는 비밀번호 불일치 |
| 403 | 타인 데이터 접근 시도 |
| 404 | 존재하지 않는 리소스 또는 사용자 |
| 409 | loginId 또는 email 중복 |
| 423 | 계정 잠금 상태 |
| 500 | 서버 내부 오류 |
| 502 | Gemini API 연결 실패 |
