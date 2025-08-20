#!/bin/bash

# =====================================
# SOLSOLHEY Docker 실행 스크립트
# =====================================
# 환경별로 손쉽게 도커 컨테이너를 실행하는 스크립트

set -e  # 에러 발생 시 스크립트 중단

# =====================================
# 색상 정의
# =====================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# =====================================
# 함수 정의
# =====================================
print_banner() {
    echo -e "${CYAN}"
    echo "╔═══════════════════════════════════════╗"
    echo "║           🐳 SOLSOLHEY DOCKER         ║"
    echo "║        Container Management Tool       ║"
    echo "╚═══════════════════════════════════════╝"
    echo -e "${NC}"
}

print_usage() {
    echo -e "${YELLOW}사용법:${NC}"
    echo "  $0 [COMMAND] [ENVIRONMENT]"
    echo ""
    echo -e "${YELLOW}Commands:${NC}"
    echo "  up      - 컨테이너 시작"
    echo "  down    - 컨테이너 중지 및 제거"  
    echo "  restart - 컨테이너 재시작"
    echo "  logs    - 로그 보기"
    echo "  status  - 컨테이너 상태 확인"
    echo "  clean   - 모든 컨테이너, 이미지, 볼륨 정리"
    echo ""
    echo -e "${YELLOW}Environment:${NC}"
    echo "  dev     - 개발환경 (기본값)"
    echo "  prod    - 운영환경"
    echo ""
    echo -e "${YELLOW}예시:${NC}"
    echo "  $0 up dev          # 개발환경 시작"
    echo "  $0 logs prod       # 운영환경 로그 보기" 
    echo "  $0 clean           # 모든 리소스 정리"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker가 설치되어 있지 않습니다.${NC}"
        echo "Docker를 먼저 설치해주세요: https://docs.docker.com/get-docker/"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}❌ Docker Compose가 설치되어 있지 않습니다.${NC}"
        echo "Docker Compose를 먼저 설치해주세요."
        exit 1
    fi

    if ! docker info &> /dev/null; then
        echo -e "${RED}❌ Docker 데몬이 실행되고 있지 않습니다.${NC}"
        echo "Docker Desktop을 시작해주세요."
        exit 1
    fi
}

check_env_file() {
    local env=$1
    
    if [ "$env" = "prod" ]; then
        echo -e "${YELLOW}⚠️  운영환경 실행 전 .env 파일을 확인해주세요.${NC}"
        echo "필요한 환경변수들이 설정되어 있는지 확인하세요."
    fi
}

# =====================================
# 메인 로직
# =====================================
COMMAND=${1:-""}
ENVIRONMENT=${2:-"dev"}

print_banner

case $COMMAND in
    "up")
        echo -e "${GREEN}🚀 $ENVIRONMENT 환경 시작 중...${NC}"
        check_docker
        check_env_file $ENVIRONMENT
        
        if [ "$ENVIRONMENT" = "dev" ]; then
            docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
        elif [ "$ENVIRONMENT" = "prod" ]; then
            docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
        fi
        
        echo -e "${GREEN}✅ 컨테이너가 성공적으로 시작되었습니다!${NC}"
        
        if [ "$ENVIRONMENT" = "dev" ]; then
            echo -e "${CYAN}📱 서비스 접속 정보:${NC}"
            echo "  • Backend API: http://localhost:8080/api/v1"
            echo "  • Swagger UI: http://localhost:8080/swagger-ui/index.html"
              echo "  • PostgreSQL: localhost:5432 (solsol_dev/solsol/[환경변수에서 설정])"
  echo "  • pgAdmin: http://localhost:5050 (admin@solsolhey.com/[환경변수에서 설정])"
  echo "  • Redis: localhost:6379 ([환경변수에서 설정])"
            echo "  • Redis Commander: http://localhost:8081"
        fi
        ;;
        
    "down")
        echo -e "${YELLOW}🛑 $ENVIRONMENT 환경 중지 중...${NC}"
        check_docker
        
        if [ "$ENVIRONMENT" = "dev" ]; then
            docker-compose -f docker-compose.yml -f docker-compose.dev.yml down
        elif [ "$ENVIRONMENT" = "prod" ]; then
            docker-compose -f docker-compose.yml -f docker-compose.prod.yml down
        fi
        
        echo -e "${GREEN}✅ 컨테이너가 성공적으로 중지되었습니다!${NC}"
        ;;
        
    "restart")
        echo -e "${BLUE}🔄 $ENVIRONMENT 환경 재시작 중...${NC}"
        $0 down $ENVIRONMENT
        sleep 2
        $0 up $ENVIRONMENT
        ;;
        
    "logs")
        echo -e "${CYAN}📋 $ENVIRONMENT 환경 로그 보기...${NC}"
        check_docker
        
        if [ "$ENVIRONMENT" = "dev" ]; then
            docker-compose -f docker-compose.yml -f docker-compose.dev.yml logs -f
        elif [ "$ENVIRONMENT" = "prod" ]; then
            docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f
        fi
        ;;
        
    "status")
        echo -e "${CYAN}📊 컨테이너 상태 확인...${NC}"
        check_docker
        docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        ;;
        
    "clean")
        echo -e "${RED}🗑️  모든 Docker 리소스 정리 중...${NC}"
        echo -e "${YELLOW}주의: 모든 컨테이너, 이미지, 볼륨이 삭제됩니다!${NC}"
        read -p "계속하시겠습니까? (y/N): " confirm
        
        if [[ $confirm =~ ^[Yy]$ ]]; then
            check_docker
            docker-compose -f docker-compose.yml -f docker-compose.dev.yml down -v --rmi all 2>/dev/null || true
            docker-compose -f docker-compose.yml -f docker-compose.prod.yml down -v --rmi all 2>/dev/null || true
            docker system prune -a -f --volumes
            echo -e "${GREEN}✅ 모든 리소스가 정리되었습니다!${NC}"
        else
            echo -e "${YELLOW}❌ 작업이 취소되었습니다.${NC}"
        fi
        ;;
        
    *)
        echo -e "${RED}❌ 올바르지 않은 명령입니다.${NC}"
        print_usage
        exit 1
        ;;
esac
