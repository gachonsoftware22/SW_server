---
name: verify
description: 빌드와 테스트를 실행하고 결과를 리포트한다. 코드 변경 후 커밋 전에 사용.
disable-model-invocation: false
---

`./gradlew build` 를 실행하여 컴파일, 테스트, 패키징을 순서대로 검증한다.

## 실행 절차

1. `./gradlew build` 실행
2. 결과를 아래 형식으로 리포트:

```
빌드 결과: 성공 / 실패
테스트: X개 통과, Y개 실패
실패한 테스트: (있으면 이름과 오류 메시지 목록)
경고: (있으면 요약)
```

3. 실패가 있으면 원인을 분석하고 수정 방법을 제안한다.

## 참고

- 컴파일만 빠르게 확인할 때는 `./gradlew compileJava`
- 테스트만 다시 실행할 때는 `./gradlew test`
- 환경변수(DB_URL 등)가 없으면 테스트 중 DB 연결 오류가 날 수 있다 — Docker Compose가 실행 중인지 확인
