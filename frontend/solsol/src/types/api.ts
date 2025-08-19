// API 공통 응답 타입
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
  errors?: Record<string, string>;
}

// 회원가입 관련 타입
export interface SignupRequest {
  userId: string;
  password: string;
  nickname: string;
}

export interface SignupResponse {
  success: boolean;
  message: string;
  username?: number;
  errors?: Record<string, string>;
}

// 로그인 관련 타입
export interface LoginRequest {
  userId: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  token?: string;
  username?: string;
  errors?: Record<string, string>;
}

// 로그아웃 관련 타입
export interface LogoutResponse {
  success: boolean;
  message: string;
}

// 에러 응답 타입
export interface ErrorResponse {
  success: false;
  message: string;
  errors?: Record<string, string>;
}

// 사용자 정보 타입
export interface User {
  username: string;
  userId: string;
  nickname: string;
}

// API 에러 클래스
export class ApiError extends Error {
  constructor(
    public status: number,
    public response: ErrorResponse,
    message?: string
  ) {
    super(message || response.message);
    this.name = 'ApiError';
  }
}




