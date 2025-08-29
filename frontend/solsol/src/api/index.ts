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
  UserItem,
  ApiResponse,
  MascotBackgroundUpdateRequest
} from '../types/api';
import { ApiError } from '../types/api';
import router from '../router';
import { useToastStore } from '../stores/toast';

const API_BASE = (import.meta as any).env?.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// Helper: infer user-friendly EXP toast label by category
function inferExpLabel(category: string): string {
  if (!category) return '경험치 획득!';
  if (category.includes('ATTEND')) return '출석 완료!';
  if (category.includes('FINANCE') || category.includes('EXCHANGE') || category.includes('CHALLENGE')) return '금융 챌린지 참여!';
  if (category.includes('FRIEND')) return '친구 상호작용!';
  if (category.includes('SHOP') || category.includes('PURCHASE')) return '쇼핑 활동!';
  return '경험치 획득!';
}

function getApiOrigin(): string {
  try {
    const url = new URL(API_BASE);
    return `${url.protocol}//${url.host}`;
  } catch {
    // fallback: assume same origin
    return '';
  }
}

// CSRF 토큰을 쿠키에서 읽어오는 함수
function getCookie(name: string): string | undefined {
  const m = document.cookie.split('; ').find(r => r.startsWith(name + '='));
  return m ? m.split('=')[1] : undefined;
}

// CSRF 쿠키 시드 보장
async function ensureCsrfCookie(): Promise<void> {
  if (!getCookie('XSRF-TOKEN')) {
    const origin = getApiOrigin();
    await fetch(`${origin}/health`, { credentials: 'include' });
  }
}

// API 요청 헬퍼 함수
class HttpError extends Error {
  constructor(public status: number, public body?: any, message?: string) {
    super(message || body?.message || `HTTP ${status}`);
    this.name = 'HttpError';
  }
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const method = (init.method || 'GET').toUpperCase();
  const headers = new Headers(init.headers || {});

  if (init.body != null && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (method !== 'GET' && method !== 'HEAD') {
    await ensureCsrfCookie();
    const xsrf = getCookie('XSRF-TOKEN');
    if (!xsrf) throw new Error('Missing XSRF-TOKEN');
    headers.set('X-XSRF-TOKEN', xsrf);
  }

  const doFetch = async () => {
    // 절대 URL인지 확인 (http:// 또는 https://로 시작하는지)
    const isAbsoluteUrl = path.startsWith('http://') || path.startsWith('https://');
    const fullUrl = isAbsoluteUrl ? path : `${API_BASE}${path}`;
    
    const res = await fetch(fullUrl, {
      credentials: 'include',
      ...init,
      headers,
    });
    const text = await res.text();
    const data = text ? JSON.parse(text) : undefined;
    if (!res.ok) throw new HttpError(res.status, data);
    try {
      const payload: any = (data as any) ?? {};
      const exp = payload?.data?.expAwarded ?? payload?.expAwarded;
      if (exp && typeof exp.amount === 'number') {
        const toast = useToastStore();
        const amount = exp?.amount as number;
        const level = exp?.level as number | undefined;
        const category = String(exp?.category || '').toUpperCase();
        const label = inferExpLabel(category);
        const levelTag = level ? ` · Lv.${level}` : '';
        toast.show(`${label} +${amount}exp${levelTag}`, 'success');
      }
    } catch {
      // ignore toast parse/store errors
    }
    return data as T;
  };

  try {
    return await doFetch();
  } catch (e) {
    if (e instanceof HttpError && e.status === 401) {
      // 로그인 시도에 대한 401은 토큰 갱신을 시도하지 않고, 서버 메시지를 그대로 노출한다.
      const isLoginEndpoint = typeof path === 'string' && path.includes('/auth/login');
      if (!isLoginEndpoint) {
        try {
          await refreshToken();
          return await doFetch();
        } catch {
          // 갱신 실패 시 원래의 401 오류를 그대로 던져 사용자에게 명확한 메시지를 전달한다.
          auth.clearAuth();
          throw e;
        }
      }
    }
    throw e;
  }
}

// 토큰 갱신 함수
async function refreshToken(): Promise<void> {
  const response = await fetch(`${API_BASE}/auth/refresh`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  if (!response.ok) {
    throw new Error('토큰 갱신 실패');
  }
}

// 기존 함수들 복원 (호환성을 위해)
export async function apiRequestWithCsrf<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  return apiRequest<T>(endpoint, options);
}

// 리프레시 토큰 관련 함수들 (기존 코드와의 호환성을 위해)
export async function dedupedRefresh(): Promise<boolean> {
  // 기존 리프레시 로직 유지
  return false;
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
  } catch (e: any) {
    // Treat 404 as "no mascot yet" for both our HttpError and legacy ApiError types
    const status = (e && typeof e === 'object' && 'status' in e) ? (e as any).status : undefined;
    if (status === 404) return null;
    if (e instanceof ApiError && e.status === 404) return null;
    throw e;
  }
}

// 공개 마스코트 조회 (로그인하지 않은 사용자용)
export async function getPublicMascot(ownerId: string): Promise<any> {
  try {
    const res = await apiRequest<any>(`/mascot/view/public?ownerId=${ownerId}`, {
      method: 'GET',
    });
    return res;
  } catch (e: any) {
    console.error('공개 마스코트 조회 실패:', e);
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

// 마스코트 커스터마이징 저장 (아이템 위치/스케일/회전 등)
// 백엔드의 equippedItem 문자열 필드에 JSON 문자열로 저장합니다.
export interface MascotCustomizationRequest {
  equippedItems: Array<{
    itemId: number;
    relativePosition: { x: number; y: number };
    scale: number;
    rotation: number;
  }>;
}

export async function customizeMascot(data: MascotCustomizationRequest): Promise<Mascot> {
  // 백엔드는 MascotUpdateRequest.equippedItem(최대 100자)만 허용하므로,
  // 커스터마이징 정보를 요약 문자열로 변환해서 보냅니다.
  const ids = data.equippedItems.map(e => e.itemId).join(',');
  let summary = ids ? `custom:${ids}` : 'custom:none';
  if (summary.length > 100) summary = summary.slice(0, 97) + '...';

  const payload: UpdateMascotRequest = { equippedItem: summary };

  const res = await apiRequest<any>('/mascot', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });

  const m = mascot.transformBackendResponse(res);
  if (!m) throw new Error('마스코트 커스터마이징에 실패했습니다.');
  return m;
}

// 신규: 서버 저장용 커스터마이징 전체 포맷
export interface MascotCustomization {
  version: 'v1';
  equippedItems: Array<{
    itemId: number;
    relativePosition: { x: number; y: number };
    scale: number;
    rotation: number;
  }>;
  // 선택: 저장 시 스냅샷(Data URL) 포함
  snapshotImageDataUrl?: string;
}

export async function getMascotCustomization(): Promise<MascotCustomization | null> {
  const res = await apiRequest<any>('/mascot/customization', { method: 'GET' });
  const data = res && res.data ? (res.data as MascotCustomization) : null;
  return data;
}

export async function saveMascotCustomization(payload: MascotCustomization): Promise<MascotCustomization> {
  // 좌표/스케일을 소수점 3자리로 제한하여 전송 (페이로드 최적화)
  const compact: MascotCustomization = {
    version: 'v1',
    equippedItems: payload.equippedItems.map(e => ({
      itemId: e.itemId,
      relativePosition: {
        x: Math.round(e.relativePosition.x * 1000) / 1000,
        y: Math.round(e.relativePosition.y * 1000) / 1000,
      },
      scale: Math.round(e.scale * 1000) / 1000,
      rotation: Math.round((e.rotation % 360 + 360) % 360),
    })),
    snapshotImageDataUrl: payload.snapshotImageDataUrl,
  };

  const res = await apiRequest<any>('/mascot/customization', {
    method: 'PUT',
    body: JSON.stringify(compact),
  });
  return (res && res.data) ? (res.data as MascotCustomization) : compact;
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

// 배경 커스터마이징 저장
export async function updateMascotBackground(data: MascotBackgroundUpdateRequest): Promise<Mascot> {
  const res = await apiRequest<any>('/mascot/background', {
    method: 'PUT',
    body: JSON.stringify(data),
  });

  const m = mascot.transformBackendResponse(res);
  if (!m) throw new Error('배경 설정 저장에 실패했습니다.');
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
      backgroundColor: d.backgroundColor ?? undefined,
      backgroundPattern: d.backgroundPattern ?? undefined,
      createdAt: d.createdAt,
      updatedAt: d.updatedAt,
      snapshotImage: d.snapshotImage,
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
  thumbnailUrl?: string;  // 선택 (백엔드 URL 검증 에러 방지)
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
  // 우리 HttpError(from apiRequest)
  if (error instanceof HttpError) {
    const body = (error as HttpError).body as any | undefined;
    // 서버가 표준 래핑 응답을 보낸 경우
    if (body) {
      const errs = body.errors;
      if (errs) {
        const first = Object.values(errs)[0];
        if (first) return String(first);
      }
      if (body.message) return String(body.message);
    }
    // 상태 코드에 따른 기본 메시지
    switch ((error as HttpError).status) {
      case 404:
        return '없는 유저 입니다.';
      case 401:
        return '인증이 필요합니다. 다시 로그인해 주세요.';
      case 400:
        return '잘못된 요청입니다.';
      case 500:
        return '서버 오류가 발생했습니다.';
      default:
        return `요청 실패 (HTTP ${(error as HttpError).status})`;
    }
  }
  // 기존 ApiError (타 타입 호환성)
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
let _authKnown = false; // 마지막으로 확인된 로그인 상태
let _userCache: any | null = null; // 마지막으로 가져온 유저
let _fetching: Promise<any | null> | null = null; // fetchUser 중복 호출 방지
export const auth = {
  // 정확한 판별: 서버 핑
  async isAuthenticatedAsync(): Promise<boolean> {
    const u = await this.fetchUser();
    return !!u;
  },

  // 정확한 정보: 서버에서 가져오고 캐시 갱신
  async fetchUser<T = { userId?: number; nickname?: string; email?: string; campus?: string; totalPoints?: number }>() : Promise<T | null> {
    if (_fetching) return _fetching as Promise<T | null>;
    
    _fetching = (async () => {
      for (let i = 0; i < 2; i++) {
        try {
          const res = await apiRequest<{ success: boolean; data?: any; message?: string }>('/auth/me', { method: 'GET' });

          const u = (res && (res as any).data) ? (res as any).data as T : null;
          if (!u) throw new Error((res as any)?.message || '사용자 정보를 가져오지 못했습니다.');
          _userCache = u;
          _authKnown = true;
          return u as T;
        } catch {
          await new Promise(r => setTimeout(r, 100));
        }
      }
      _userCache = null;
      _authKnown = false;
      return null;
    })();

    try { return await _fetching as Promise<T | null>; }
    finally { _fetching = null; }
  },

  // JWT 토큰 가져오기 (쿠키에서)
  getToken(): string | null {
    return getCookie('ACCESS_TOKEN') || null;
  },

  // 인증 상태 클리어
  clearAuth(): void {
    _userCache = null;
    _authKnown = false;
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
// Deprecated: getUserInfo(userId) was duplicating /auth/me for current user.
// Note: 로그인 사용자 정보는 auth.fetchUser() 결과를 직접 사용하세요.

// 상점 관련 API 함수들
export async function getShopItems(): Promise<any> {
  try {
    const res = await apiRequest<any>('/shop/items', {
      method: 'GET',
    });
    console.log('🛍️ 상품 목록 조회 성공:', res);
    // 백엔드 응답 구조에 맞게 data 필드 추출
    return res && res.data ? res.data : [];
  } catch (e: any) {
    console.error('❌ 상품 목록 조회 실패:', e);
    // 에러가 발생해도 빈 배열 반환하여 앱이 중단되지 않도록 함
    return [];
  }
}

export async function getPublicShopItems(): Promise<any> {
  try {
    const res = await apiRequest<any>('/shop/items/public', {
      method: 'GET',
    });
    console.log('🛍️ 공개 상품 목록 조회 성공:', res);
    // 백엔드 응답 구조에 맞게 data 필드 추출
    return res && res.data ? res.data : [];
  } catch (e: any) {
    console.error('❌ 공개 상품 목록 조회 실패:', e);
    // 에러가 발생해도 빈 배열 반환하여 앱이 중단되지 않도록 함
    return [];
  }
}

// 친구 관련 API 함수들
export async function sendFriendRequest(friendUserId: number): Promise<any> {
  try {
    const res = await apiRequest<any>('/friends/requests', {
      method: 'POST',
      body: JSON.stringify({ friendUserId }),
    });
    console.log('👥 친구 요청 성공:', res);
    return res;
  } catch (e: any) {
    console.error('❌ 친구 요청 실패:', e);
    throw e;
  }
}

// 좋아요(응원) 관련 API 함수들
export async function sendInteraction(toUserId: number, interactionType: 'LIKE' | 'CHEER' = 'LIKE', message?: string): Promise<any> {
  try {
    const res = await apiRequest<any>('/friends/interactions', {
      method: 'POST',
      body: JSON.stringify({ 
        toUserId, 
        interactionType, 
        message: message || (interactionType === 'LIKE' ? '좋아요!' : '응원합니다!') 
      }),
    });
    console.log('❤️ 상호작용 전송 성공:', res);
    return res;
  } catch (e: any) {
    console.error('❌ 상호작용 전송 실패:', e);
    throw e;
  }
}

// 친구 관계 확인 API 함수
export async function checkFriendship(targetUserId: number): Promise<any> {
  try {
    const res = await apiRequest<any>(`/friends/check/${targetUserId}`, {
      method: 'GET',
    });
    console.log('👥 친구 관계 확인 성공:', res);
    return res;
  } catch (e: any) {
    console.error('❌ 친구 관계 확인 실패:', e);
    throw e;
  }
}

export async function getGifticons(): Promise<Gifticon[]> {
  const res = await apiRequest<any>('/shop/gifticons', { method: 'GET' });
  return (res && res.data) ? (res.data as Gifticon[]) : [];
}

export async function createOrder(orderData: OrderRequest): Promise<OrderResponse> {
  const res = await apiRequest<any>('/shop/orders', {
    method: 'POST',
    body: JSON.stringify(orderData),
  });
  return (res && res.data) ? (res.data as OrderResponse) : ({} as OrderResponse);
}

export async function getUserItems(): Promise<UserItem[]> {
  return apiRequest<UserItem[]>('/shop/user-items', {
    method: 'GET',
  });
}

// 보관함 기프티콘 API
import type { PurchasedGifticon, PurchasedGifticonDetail } from '../types/api';

export async function getPurchasedGifticons(): Promise<PurchasedGifticon[]> {
  const res = await apiRequest<any>('/shop/gifticons/purchased', { method: 'GET' });
  return (res && res.data) ? (res.data as PurchasedGifticon[]) : [];
}

export async function getPurchasedGifticonDetail(id: number): Promise<PurchasedGifticonDetail> {
  const res = await apiRequest<any>(`/shop/gifticons/purchased/${id}`, { method: 'GET' });
  return (res && res.data) ? (res.data as PurchasedGifticonDetail) : ({} as PurchasedGifticonDetail);
}

export async function redeemPurchasedGifticon(id: number): Promise<string> {
  const res = await apiRequest<any>(`/shop/gifticons/${id}/redeem`, { method: 'POST' });
  return (res && res.message) ? (res.message as string) : '사용 처리되었습니다.';
}

// 금융 관련 API 함수들
// 환율 전체 조회
export async function getAllExchangeRates(): Promise<any> {
  return apiRequest<any>('/finance/exchange-rates', {
    method: 'GET',
  });
}

// 환율 단건 조회
export async function getExchangeRate(currency: string): Promise<any> {
  const query = encodeURIComponent(currency);
  return apiRequest<any>(`/finance/exchange-rate?currency=${query}`, {
    method: 'GET',
  });
}

// 환전 예상 금액 조회
export async function estimateExchange(data: { currency: string; exchangeCurrency: string; amount: string | number; }): Promise<any> {
  const payload = {
    currency: data.currency,
    exchangeCurrency: data.exchangeCurrency,
    amount: String(data.amount),
  };
  return apiRequest<any>('/finance/exchange/estimate', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

// 계좌 거래내역 조회
export async function getTransactionHistory(data: {
  userKey: string;
  accountNo: string;
  startDate: string; // YYYYMMDD
  endDate: string;   // YYYYMMDD
  transactionType: 'M' | 'D' | 'A';
  orderByType?: 'ASC' | 'DESC';
}): Promise<any> {
  return apiRequest<any>('/finance/accounts/transactions', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

// JWT 토큰에서 payload 추출하는 유틸리티 함수
export function parseJwtPayload(token: string): any {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('JWT 파싱 실패:', error);
    return null;
  }
}

// AI 말풍선: 메시지 생성 요청
export async function getAiSpeech(): Promise<{ success: boolean; message: string; expiresInSec?: number }>
{
  return apiRequest<{ success: boolean; message: string; expiresInSec?: number }>(`/ai/speech`, {
    method: 'POST',
  });
}
