# ── Stage 1: build ──────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

# Gradle wrapper (변경 빈도 최저)
COPY gradlew .
COPY gradle/ gradle/
RUN chmod +x gradlew

# 의존성 선언 파일
COPY build.gradle settings.gradle ./

# 의존성 다운로드 레이어 캐싱 — 소스 변경 시 재실행 안 됨
RUN ./gradlew dependencies --no-daemon

# 소스 코드 (가장 자주 변경)
COPY src/ src/

RUN ./gradlew bootJar -x test --no-daemon
## 멀티스테이지
# ── Stage 2: runtime ────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
