/// <reference types="vite/client" />

import type {
  SignupRequest,
  SignupResponse,
  LoginRequest,
  LoginResponse,
  LogoutResponse,
  ErrorResponse,
  CreateMascotRequest,
  UpdateMascotRequest,
  EquipItemsRequest,
  GetItemsResponse,
  MascotCreateRequest,
  MascotApiResponse,
  Challenge,
  ChallengeJoinResponse,
  ChallengeProgressRequest,
  ChallengeProgressResponse,
  UserResponse,
  Mascot,
  ShopItem,
  Gifticon,
  OrderRequest,
  OrderResponse,
  UserItem
} from '../types/api';
import { ApiError } from '../types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// 쿠키 관련 유틸리티 함수들 (안전한 URL 디코딩)
function getCookie(name: string): string | undefined {
  const raw = document.cookie
    .split('; ')
    .find((row) => row.startsWith(name + '='))
    ?.split('=')[1];
  if (!raw) return undefined;
  try { return decodeURIComponent(raw); } catch { return raw; }
}

function withCsrf(headers: HeadersInit = {}): HeadersInit {
  const xsrf = getCookie('XSRF-TOKEN');
  if (!xsrf) return headers;
  if (headers instanceof Headers) {
    headers.set('X-XSRF-TOKEN', xsrf);
    return headers;
  }
  return { ...headers, 'X-XSRF-TOKEN': xsrf };
}

function needsCsrf(method?: string) {
  return !!method && !/^(GET|HEAD|OPTIONS|TRACE)$/i.test(method);
}

// API 요청 헬퍼 함수 (쿠키 기반)
export async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const base = new Headers(options.headers as any);
  // body가 있을 때만 Content-Type 설정하되, 브라우저가 정해야 하는 타입(FormData 등)은 건드리지 않음
  const shouldSetJson =
    options.body != null &&
    !base.has('content-type') &&
    !(options.body instanceof FormData) &&
    !(options.body instanceof Blob) &&
    !(options.body instanceof ArrayBuffer) &&
    !(options.body instanceof URLSearchParams);
  if (shouldSetJson) {
    base.set('content-type', 'application/json');
  }
  const headers: HeadersInit = needsCsrf(options.method as string)
    ? withCsrf(base)
    : base;
  
  const config: RequestInit & { _retried?: boolean } = {
    ...options,
    headers,
    credentials: 'include', // 쿠키 자동 전송
  };

  const res = await fetch(url, config);
  
  // 401 에러 시 리프레시 토큰으로 재시도
  if (res.status === 401 && !config._retried) {
    const refreshed = await dedupedRefresh();
    if (refreshed) {
      return apiRequest<T>(endpoint, { ...options, _retried: true } as any);
    }
  }

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  
  if (!res.ok) {
    throw new ApiError(res.status, (data ?? { success: false, message: '요청 실패' }) as ErrorResponse);
  }
  
  return data as T;
}

// 리프레시 토큰 시도 (중복 방지)
let _refreshing: Promise<boolean> | null = null;
async function dedupedRefresh(): Promise<boolean> {
  if (_refreshing) return _refreshing;
  _refreshing = (async () => {
    try {
      const r = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: 'POST',
        credentials: 'include',
        headers: withCsrf({ 'Content-Type': 'application/json' }),
      });
      return r.ok;
    } catch {
      return false;
    } finally {
      // 약간의 지연 후 해제(동시 401 폭주 방지)
      setTimeout(() => { _refreshing = null; }, 50);
    }
  })();
  return _refreshing;
}

// 인증 관련 API 함수들 (쿠키 기반)
export async function signup(userData: SignupRequest): Promise<SignupResponse> {
  return apiRequest<SignupResponse>('/auth/signup', {
    method: 'POST',
    body: JSON.stringify(userData),
  });
}

export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  });
}

// 서버 로그아웃 호출 (재귀 방지용 별도 함수)
export async function requestServerLogout(): Promise<LogoutResponse> {
  return apiRequest<LogoutResponse>('/auth/logout', { method: 'POST' });
}
// 기존 이름 유지 (호환성)
export async function logout(): Promise<LogoutResponse> {
  return requestServerLogout();
}

// 마스코트 관련 API 함수들

export async function createMascot(data: CreateMascotRequest): Promise<Mascot> {
  const res = await apiRequest<any>('/mascot', {
    method: 'POST',
    body: JSON.stringify(data),
  });
  
  const m = mascot.transformBackendResponse(res);
  if (!m) throw new Error('마스코트 생성에 실패했습니다.');
  
  return m;
}

export async function getMascot(): Promise<Mascot | null> {
  try {
    const res = await apiRequest<any>('/mascot', {
      method: 'GET',
    });
    
    return mascot.transformBackendResponse(res);
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) return null;
    throw e;
  }
}

export async function equipItems(data: EquipItemsRequest): Promise<Mascot> {
  const res = await apiRequest<any>('/mascot/equip', {
    method: 'POST',
    body: JSON.stringify(data),
  });
  
  const m = mascot.transformBackendResponse(res);
  if (!m) throw new Error('아이템 장착에 실패했습니다.');
  
  return m;
}

export async function updateMascot(data: UpdateMascotRequest): Promise<Mascot> {
  const res = await apiRequest<any>('/mascot', {
    method: 'PATCH',
    body: JSON.stringify(data),
  });
  
  const m = mascot.transformBackendResponse(res);
  if (!m) throw new Error('마스코트 수정에 실패했습니다.');
  
  return m;
}

export async function getItems(): Promise<GetItemsResponse> {
  return apiRequest<GetItemsResponse>('/items', {
    method: 'GET',
  });
}

// 마스코트 관련 유틸리티 함수들
export const mascot = {
  transformBackendResponse(backendResponse: any): Mascot | null {
    if (!backendResponse || !backendResponse.data) return null;
    
    const d = backendResponse.data;
    return {
      id: d.id,
      name: d.name,
      type: d.type,
      level: d.level,
      exp: d.exp,
      equippedItem: d.equippedItem ?? null,
      createdAt: d.createdAt,
      updatedAt: d.updatedAt,
    };
  },

  calculateEvolutionStage(level: number): number {
    return Math.floor(level / 10);
  },

  calculateExpToNextLevel(currentExp: number, currentLevel: number): number {
    const next = (currentLevel + 1) * 100;
    return Math.max(0, next - currentExp);
  },
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
    const errs = (error.response as any)?.errors;
    if (errs) {
      const first = Object.values(errs)[0];
      return (first as string) || (error.response as any).message;
    }
    return (error.response as any).message ?? '요청 실패';
  }
  
  return '알 수 없는 오류가 발생했습니다.';
}

// 쿠키 기반 인증 상태 확인 (호환성: 동기 + 정확성: 비동기)
let _authKnown = false; // 마지막으로 확인된 로그인 상태(동기용)
let _userCache: any | null = null; // 마지막으로 가져온 유저(동기 반환용)
export const auth = {
  // 기존 시그니처 유지: 동기(boolean)
  isAuthenticated(): boolean {
    return _authKnown; // 새로고침 직후엔 false일 수 있으나 로그인/부트스트랩 후 갱신
  },
  // 정확한 판별: 서버 핑
  async isAuthenticatedAsync(): Promise<boolean> {
    const u = await this.fetchUser();
    return !!u;
  },

  // 하위호환: 동기 getUser (캐시 기반)
  getUser<T = { username: string }>(): T | null {
    return _userCache as T | null;
  },
  // 정확한 정보: 서버에서 가져오고 캐시 갱신
  async fetchUser<T = { username: string }>(): Promise<T | null> {
    try {
      const u = await apiRequest<T>('/auth/me', { method: 'GET' });
      _userCache = u;
      _authKnown = true;
      return u;
    } catch {
      _userCache = null;
      _authKnown = false;
      return null;
    }
  },

  // 로그아웃 (쿠키는 서버에서 삭제)
  async logout() {
    await requestServerLogout();
    _userCache = null;
    _authKnown = false;
  },
};

// 앱 부팅 시 한 번 정확 상태 동기화(선택/권장: 라우터 가드 진입 전 호출)
export async function bootstrapAuth(): Promise<void> {
  await auth.fetchUser();
}

// 로그인 후 즉시 캐시 반영 헬퍼 (UX 향상)
export async function loginAndBootstrap(credentials: LoginRequest): Promise<LoginResponse> {
  const res = await login(credentials);
  await bootstrapAuth();
  return res;
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

// 상점 관련 API 함수들
export async function getShopItems(type?: string): Promise<ShopItem[]> {
  const endpoint = type ? `/shop/items?type=${type}` : '/shop/items';
  return apiRequest<ShopItem[]>(endpoint, {
    method: 'GET',
  });
}

export async function getGifticons(): Promise<Gifticon[]> {
  return apiRequest<Gifticon[]>('/shop/gifticons', {
    method: 'GET',
  });
}

export async function createOrder(orderData: OrderRequest): Promise<OrderResponse> {
  return apiRequest<OrderResponse>('/shop/orders', {
    method: 'POST',
    body: JSON.stringify(orderData),
  });
}

export async function getUserItems(): Promise<UserItem[]> {
  return apiRequest<UserItem[]>('/shop/user-items', {
    method: 'GET',
  });
}




