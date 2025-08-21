# =====================================
# SOLSOLHEY Backend Dockerfile
# =====================================
# 멀티스테이지 빌드로 최적화된 Spring Boot 앱

# =====================================
# Stage 1: 빌드 스테이지
# =====================================
FROM openjdk:17-jdk-slim as build

# 작업 디렉토리 설정
WORKDIR /workspace/app

# Gradle Wrapper 복사 (캐시 최적화)
COPY gradlew .
COPY gradle gradle/
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (레이어 캐싱 최적화)
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src/

# 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew clean bootJar -x test --no-daemon

# JAR 파일 경로 확인을 위한 디버깅
RUN ls -la build/libs/

# =====================================  
# Stage 2: 런타임 스테이지
# =====================================
FROM eclipse-temurin:17-jre

# 메타데이터
LABEL maintainer="SOLSOLHEY Team"
LABEL version="1.0"
LABEL description="SOLSOLHEY Backend API Server"

# 시스템 패키지 업데이트 및 필요 도구 설치
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 애플리케이션 유저 생성 (보안 강화)
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 작업 디렉토리 생성
RUN mkdir /app && chown appuser:appuser /app
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# 권한 설정
RUN chown appuser:appuser app.jar

# 애플리케이션 유저로 실행
USER appuser

# 포트 노출
EXPOSE 8080

# JVM 옵션 설정 (메모리 최적화)
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
