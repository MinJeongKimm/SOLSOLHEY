/// <reference types="vite/client" />

import type {
  SignupRequest,
  SignupResponse,
  LoginRequest,
  LoginResponse,
  LogoutResponse,
  ErrorResponse,
  CreateMascotRequest,
  CreateMascotResponse,
  EquipItemsRequest,
  EquipItemsResponse,
  UpdateMascotRequest,
  UpdateMascotResponse,
  GetMascotResponse,
  GetItemsResponse,
  MascotCreateRequest,
  MascotApiResponse,
  Challenge,
  ChallengeJoinResponse,
  ChallengeProgressRequest,
  ChallengeProgressResponse,
  UserResponse,
  Mascot
} from '../types/api';
import { ApiError } from '../types/api';

// API 기본 설정
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// API 요청 헬퍼 함수
export async function apiRequest<T>(
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
  return apiRequest<SignupResponse>('/auth/signup', {
    method: 'POST',
    body: JSON.stringify(userData),
  });
}

// 로그인 API  
export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  });
}

// 로그아웃 API
export async function logout(): Promise<LogoutResponse> {
  return apiRequest<LogoutResponse>('/auth/logout', {
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

// 마스코트 관련 API 함수들

// 마스코트 생성
export async function createMascot(data: CreateMascotRequest): Promise<Mascot> {
  const response = await apiRequest<any>('/mascot', {
    method: 'POST',
    body: JSON.stringify(data),
  });
  
  // 백엔드 응답을 프론트엔드 타입으로 변환
  const mascotData = mascot.transformBackendResponse(response);
  if (!mascotData) {
    throw new Error('마스코트 생성에 실패했습니다.');
  }
  
  return mascotData;
}

// 마스코트 조회
export async function getMascot(): Promise<Mascot | null> {
  try {
    const response = await apiRequest<any>('/mascot', {
      method: 'GET',
    });
    
    // 백엔드 응답을 프론트엔드 타입으로 변환
    return mascot.transformBackendResponse(response);
  } catch (error) {
    // 404 에러는 마스코트가 없는 것으로 처리
    if (error instanceof ApiError && error.status === 404) {
      return null;
    }
    throw error;
  }
}

// 마스코트 아이템 장착/꾸미기
export async function equipItems(data: EquipItemsRequest): Promise<Mascot> {
  const response = await apiRequest<any>('/mascot/equip', {
    method: 'POST',
    body: JSON.stringify(data),
  });
  
  // 백엔드 응답을 프론트엔드 타입으로 변환
  const mascotData = mascot.transformBackendResponse(response);
  if (!mascotData) {
    throw new Error('아이템 장착에 실패했습니다.');
  }
  
  return mascotData;
}

// 마스코트 정보 수정
export async function updateMascot(data: UpdateMascotRequest): Promise<Mascot> {
  const response = await apiRequest<any>('/mascot', {
    method: 'PATCH',
    body: JSON.stringify(data),
  });
  
  // 백엔드 응답을 프론트엔드 타입으로 변환
  const mascotData = mascot.transformBackendResponse(response);
  if (!mascotData) {
    throw new Error('마스코트 수정에 실패했습니다.');
  }
  
  return mascotData;
}

// 아이템 목록 조회
export async function getItems(): Promise<GetItemsResponse> {
  return apiRequest<GetItemsResponse>('/items', {
    method: 'GET',
  });
}

// 마스코트 관련 유틸리티 함수들
export const mascot = {
  // 백엔드 응답을 프론트엔드 타입으로 변환
  transformBackendResponse(backendResponse: any): Mascot | null {
    if (!backendResponse || !backendResponse.data) return null;
    
    const data = backendResponse.data;
    return {
      id: data.id,
      name: data.name,
      type: data.type,
      level: data.level,
      exp: data.exp,
      equippedItem: data.equippedItem,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt
    };
  },

  // 프론트엔드 타입을 백엔드 요청 형태로 변환
  transformToBackendRequest(mascotData: Mascot): CreateMascotRequest {
    return {
      name: mascotData.name,
      type: mascotData.type,
      equippedItem: mascotData.equippedItem
    };
  },

  // evolutionStage 계산 (level 기반)
  calculateEvolutionStage(level: number): number {
    return Math.floor(level / 10);
  },

  // 다음 레벨까지 필요한 경험치 계산
  calculateExpToNextLevel(currentExp: number, currentLevel: number): number {
    const nextLevelExp = (currentLevel + 1) * 100;
    return Math.max(0, nextLevelExp - currentExp);
  }
};

// 친구 관련 API 함수들
export * from './friend';

// 공유 관련 API 함수들

// ShareType enum (백엔드와 동일)
export enum ShareType {
  CHALLENGE_INVITE = 'CHALLENGE_INVITE',  // 챌린지 초대
  FRIEND_INVITE = 'FRIEND_INVITE',        // 친구 초대
  ACHIEVEMENT = 'ACHIEVEMENT',            // 업적 공유
  GENERAL = 'GENERAL'                     // 일반 공유 (마스코트 공유 포함)
}

// ImageType enum (백엔드와 동일)
export enum ImageType {
  CHALLENGE_CARD = 'CHALLENGE_CARD',      // 챌린지 카드
  ACHIEVEMENT_BADGE = 'ACHIEVEMENT_BADGE', // 업적 배지
  MASCOT_SHARE = 'MASCOT_SHARE',          // 마스코트 공유
  CUSTOM = 'CUSTOM'                       // 커스텀 이미지
}

// 공유 링크 생성 요청 타입 (백엔드 ShareLinkCreateRequest와 동일)
export interface ShareLinkCreateRequest {
  title: string;          // 필수
  description?: string;   // 선택
  thumbnailUrl?: string;  // 선택
  targetUrl?: string;     // 선택
  shareType: ShareType;   // 필수
  expiresAt?: string;     // 선택 (ISO 날짜 문자열)
}

// 공유 이미지 생성 요청 타입 (백엔드 ShareImageCreateRequest와 동일)
export interface ShareImageCreateRequest {
  templateId?: number;        // 선택
  imageUrl: string;          // 필수
  originalFilename?: string; // 선택
  fileSize?: number;         // 선택
  width?: number;            // 선택
  height?: number;           // 선택
  imageType: ImageType;      // 필수
  isPublic?: boolean;        // 선택 (기본값 true)
}

// 공유 링크 생성
export async function createShareLink(data: ShareLinkCreateRequest): Promise<any> {
  return apiRequest<any>('/shares/links', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

// 공유 이미지 생성
export async function createShareImage(data: ShareImageCreateRequest): Promise<any> {
  return apiRequest<any>('/shares/images', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

// 사용 가능한 템플릿 목록 조회
export async function getAvailableTemplates(): Promise<any> {
  return apiRequest<any>('/shares/templates', {
    method: 'GET',
  });
}

// 타입별 템플릿 조회
export async function getTemplatesByType(templateType: string): Promise<any> {
  return apiRequest<any>(`/shares/templates/${templateType}`, {
    method: 'GET',
  });
}

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

// 챌린지 관련 API 함수들
export async function getChallenges(category?: string): Promise<Challenge[]> {
  // 백엔드 API 호출로 실제 데이터 가져오기
  const endpoint = category && category !== 'all' ? `/challenges?category=${category}` : '/challenges';
  return apiRequest<Challenge[]>(endpoint, {
    method: 'GET',
  });
}

export async function getChallengeDetail(challengeId: number): Promise<Challenge> {
  return apiRequest<Challenge>(`/challenges/${challengeId}`, {
    method: 'GET',
  });
}

export async function joinChallenge(challengeId: number): Promise<ChallengeJoinResponse> {
  return apiRequest<ChallengeJoinResponse>(`/challenges/${challengeId}/join`, {
    method: 'POST',
  });
}

export async function updateChallengeProgress(
  challengeId: number, 
  request: ChallengeProgressRequest
): Promise<ChallengeProgressResponse> {
  return apiRequest<ChallengeProgressResponse>(`/challenges/${challengeId}/progress`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export async function getMyChallenges(status?: string): Promise<Challenge[]> {
  const endpoint = status ? `/challenges/my?status=${status}` : '/challenges/my';
  return apiRequest<Challenge[]>(endpoint, {
    method: 'GET',
  });
}

// 사용자 정보 조회 (포인트 포함)
export async function getUserInfo(userId: number): Promise<UserResponse> {
  return apiRequest<UserResponse>(`/users/${userId}`, {
    method: 'GET',
  });
}




