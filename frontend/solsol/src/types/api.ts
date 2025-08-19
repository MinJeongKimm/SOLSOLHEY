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

// 마스코트 관련 타입 (백엔드 Mascot 엔티티 기반)
export interface MascotCreateRequest {
  name: string;
  mascotType: string;
}

export interface MascotResponse {
  mascotId: number;
  userId: number;
  name: string;
  mascotType: string;
  experiencePoint: number;
  level: number;
  evolutionStage: number;
  expToNextLevel: number;
  createdAt: string;
  updatedAt: string;
}

export interface MascotApiResponse {
  success: boolean;
  message: string;
  data?: MascotResponse;
  errors?: Record<string, string>;
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




