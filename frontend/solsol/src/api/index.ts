import type {
  SignupRequest,
  SignupResponse,
  LoginRequest,
  LoginResponse,
  LogoutResponse,
  ApiError,
  ErrorResponse
} from '../types/api';

// API 기본 설정
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// API 요청 헬퍼 함수
async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const defaultHeaders: HeadersInit = {
    'Content-Type': 'application/json',
  };

  // Authorization 헤더 추가 (토큰이 있는 경우)
  const token = localStorage.getItem('token');
  if (token) {
    defaultHeaders.Authorization = `Bearer ${token}`;
  }

  const config: RequestInit = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  };

  try {
    const response = await fetch(url, config);
    const data = await response.json();

    if (!response.ok) {
      throw new ApiError(response.status, data as ErrorResponse);
    }

    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    
    // 네트워크 오류 등
    throw new ApiError(0, {
      success: false,
      message: '네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
    });
  }
}

// 회원가입 API
export async function signup(userData: SignupRequest): Promise<SignupResponse> {
  return apiRequest<SignupResponse>('/signup', {
    method: 'POST',
    body: JSON.stringify(userData),
  });
}

// 로그인 API  
export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  });
}

// 로그아웃 API
export async function logout(): Promise<LogoutResponse> {
  return apiRequest<LogoutResponse>('/logout', {
    method: 'DELETE',
  });
}

// 토큰 관련 유틸리티 함수들
export const auth = {
  // 토큰 저장
  setToken(token: string) {
    localStorage.setItem('token', token);
  },

  // 토큰 가져오기
  getToken(): string | null {
    return localStorage.getItem('token');
  },

  // 토큰 삭제
  removeToken() {
    localStorage.removeItem('token');
  },

  // 로그인 상태 확인
  isAuthenticated(): boolean {
    return !!this.getToken();
  },

  // 사용자 정보 저장
  setUser(user: { username: string; userId: string; nickname?: string }) {
    localStorage.setItem('user', JSON.stringify(user));
  },

  // 사용자 정보 가져오기
  getUser(): { username: string; userId: string; nickname?: string } | null {
    const userData = localStorage.getItem('user');
    return userData ? JSON.parse(userData) : null;
  },

  // 사용자 정보 삭제
  removeUser() {
    localStorage.removeItem('user');
  },

  // 완전 로그아웃 (토큰 + 사용자 정보 모두 삭제)
  clearAuth() {
    this.removeToken();
    this.removeUser();
  }
};

// API 에러 핸들링 헬퍼
export function handleApiError(error: unknown): string {
  if (error instanceof ApiError) {
    // 필드별 에러가 있는 경우 첫 번째 에러 메시지 반환
    if (error.response.errors) {
      const firstError = Object.values(error.response.errors)[0];
      return firstError || error.response.message;
    }
    return error.response.message;
  }
  
  return '알 수 없는 오류가 발생했습니다.';
}




