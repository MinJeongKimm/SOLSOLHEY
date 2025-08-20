# 🐳 SOLSOLHEY Docker 가이드

SOLSOLHEY 백엔드를 Docker로 쉽게 관리하고 실행하는 방법을 안내합니다.

## 📋 목차

1. [사전 요구사항](#사전-요구사항)
2. [빠른 시작](#빠른-시작)
3. [환경별 실행](#환경별-실행)
4. [서비스 접속 정보](#서비스-접속-정보)
5. [환경 변수 설정](#환경-변수-설정)
6. [유용한 명령어](#유용한-명령어)
7. [문제 해결](#문제-해결)

## 🔧 사전 요구사항

- **Docker Desktop**: [설치 링크](https://docs.docker.com/get-docker/)
- **Docker Compose**: Docker Desktop에 포함됨
- **최소 시스템 요구사항**:
  - RAM: 4GB 이상 권장
  - 디스크 공간: 2GB 이상

## 🚀 빠른 시작

### 1. 개발환경 실행

```bash
# backend 디렉토리로 이동
cd backend

# 실행 스크립트를 사용한 방법 (권장)
./docker-run.sh up dev

# 또는 직접 docker-compose 명령 사용
docker-compose -f docker-compose.yml -f docker-compose.dev.yml --env-file docker.env.dev up -d
```

### 2. 서비스 접속

개발환경이 시작되면 다음 서비스에 접속할 수 있습니다:

- 🔗 **Backend API**: http://localhost:8080/api/v1
- 📚 **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- 🗄️ **pgAdmin**: http://localhost:5050
- 💾 **Redis Commander**: http://localhost:8081

## 🌍 환경별 실행

### 개발환경 (Development)

```bash
# backend 디렉토리에서 실행
cd backend

# 개발환경 시작
./docker-run.sh up dev

# 로그 실시간 보기
./docker-run.sh logs dev

# 개발환경 중지
./docker-run.sh down dev
```

**개발환경 특징:**
- PostgreSQL 개발 데이터베이스
- Swagger UI 활성화
- 디버그 로깅 활성화
- pgAdmin, Redis Commander 포함
- 핫 리로드 지원 (향후 추가 예정)

### 운영환경 (Production)

```bash
# backend 디렉토리에서 실행
cd backend

# 먼저 환경변수 파일 설정
cp docker.env.prod.example docker.env.prod
nano docker.env.prod  # 실제 값으로 수정

# 운영환경 시작
./docker-run.sh up prod

# 운영환경 중지
./docker-run.sh down prod
```

**운영환경 특징:**
- 성능 최적화된 설정
- 보안 강화 (Swagger UI 비활성화)
- 모니터링 도구 포함 (Prometheus, Grafana)
- Nginx 리버스 프록시
- SSL/TLS 지원

## 🔗 서비스 접속 정보

### 개발환경

| 서비스 | URL | 계정 정보 |
|--------|-----|-----------|
| Backend API | http://localhost:8080/api/v1 | - |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | - |
| pgAdmin | http://localhost:5050 | admin@solsolhey.com / [환경변수에서 설정] |
| PostgreSQL | localhost:5432 | solsol / [환경변수에서 설정] |
| Redis Commander | http://localhost:8081 | - |
| Redis | localhost:6379 | [환경변수에서 설정] |

### 운영환경

| 서비스 | URL | 계정 정보 |
|--------|-----|-----------|
| Backend API | http://localhost:8080/api/v1 | - |
| Nginx | http://localhost:80, https://localhost:443 | - |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin / [환경변수] |

## ⚙️ 환경 변수 설정

### 개발환경 (`docker.env.dev`)

개발환경용 환경변수는 기본값이 제공됩니다. 필요에 따라 수정하세요.

### 운영환경 (`docker.env.prod`)

```bash
# 템플릿 복사
cp docker.env.prod.example docker.env.prod

# 반드시 변경해야 할 값들:
# - DB_PASSWORD: 강력한 데이터베이스 비밀번호
# - JWT_SECRET_KEY: JWT 서명용 비밀키 (openssl rand -hex 32)
# - REDIS_PASSWORD: Redis 비밀번호
# - CORS_ALLOWED_ORIGINS: 실제 프론트엔드 도메인
```

**보안 체크리스트:**
- ✅ 기본 비밀번호들을 모두 변경했는지 확인
- ✅ JWT Secret Key를 강력한 랜덤 키로 생성했는지 확인
- ✅ CORS 설정을 실제 도메인으로 제한했는지 확인
- ✅ SSL 인증서를 설정했는지 확인 (HTTPS 사용 시)

## 🛠️ 유용한 명령어

### 실행 스크립트 사용

```bash
# 컨테이너 상태 확인
./docker-run.sh status

# 특정 환경 재시작
./docker-run.sh restart dev

# 모든 리소스 정리 (주의!)
./docker-run.sh clean
```

### Docker Compose 직접 사용

```bash
# 개발환경 백그라운드 실행
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# 특정 서비스만 재시작
docker-compose -f docker-compose.yml -f docker-compose.dev.yml restart backend

# 특정 서비스 로그 보기
docker-compose -f docker-compose.yml -f docker-compose.dev.yml logs -f backend

# 볼륨 포함해서 완전히 제거
docker-compose -f docker-compose.yml -f docker-compose.dev.yml down -v
```

### 일반 Docker 명령어

```bash
# 실행 중인 컨테이너 확인
docker ps

# 컨테이너에 직접 접속
docker exec -it solsol-backend bash
docker exec -it solsol-postgres psql -U solsol -d solsol_dev

# 로그 보기
docker logs solsol-backend
docker logs -f solsol-postgres

# 이미지 목록
docker images | grep solsol

# 볼륨 목록
docker volume ls | grep solsol
```

## 🔧 개발 도구

### 데이터베이스 관리

**pgAdmin 사용:**
1. http://localhost:5050 접속
2. admin@solsolhey.com / [환경변수에서 설정한 비밀번호]로 로그인
3. 새 서버 추가:
   - Host: postgres
   - Port: 5432
   - Username: solsol
   - Password: [환경변수에서 설정한 비밀번호]

**터미널에서 직접 접속:**
```bash
docker exec -it solsol-postgres psql -U solsol -d solsol_dev
```

### Redis 관리

**Redis Commander 사용:**
- http://localhost:8081 접속

**터미널에서 직접 접속:**
```bash
docker exec -it solsol-redis redis-cli
AUTH [환경변수에서 설정한 비밀번호]
```

## 🐛 문제 해결

### 포트 충돌 해결

```bash
# 포트 사용 중인 프로세스 확인
lsof -i :8080
lsof -i :5432

# 프로세스 종료
kill -9 <PID>
```

### 권한 문제 해결

```bash
# 스크립트 실행 권한 부여
chmod +x docker-run.sh

# 볼륨 권한 문제 시
sudo chown -R $USER:$USER ./logs
```

### 컨테이너 재빌드

```bash
# 캐시 없이 다시 빌드
docker-compose build --no-cache backend

# 이미지 삭제 후 재빌드
docker rmi $(docker images | grep solsol | awk '{print $3}')
./docker-run.sh up dev
```

### 데이터베이스 초기화

```bash
# PostgreSQL 데이터 완전 삭제
docker volume rm solsolhey-dev_postgres_dev_data
./docker-run.sh up dev
```

### 로그 확인

```bash
# 모든 서비스 로그
./docker-run.sh logs dev

# 특정 서비스 로그
docker logs solsol-backend
docker logs solsol-postgres

# 실시간 로그 추적
docker logs -f solsol-backend
```

## 🚀 배포 참고사항

### 로컬에서 운영환경 테스트

```bash
# 운영환경 로컬 테스트
cp docker.env.prod.example docker.env.prod
# docker.env.prod 파일 수정 (테스트용 값으로)
./docker-run.sh up prod
```

### CI/CD 연동

```yaml
# GitHub Actions 예시
- name: Deploy with Docker
  run: |
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 📚 추가 자료

- [Docker 공식 문서](https://docs.docker.com/)
- [Docker Compose 문서](https://docs.docker.com/compose/)
- [Spring Boot Docker 가이드](https://spring.io/guides/topicals/spring-boot-docker/)

---

## 🆘 도움이 필요하다면

문제가 발생하면 다음 정보와 함께 문의해주세요:

```bash
# 시스템 정보
./docker-run.sh status
docker version
docker-compose version

# 로그 정보
./docker-run.sh logs dev > logs.txt
```
