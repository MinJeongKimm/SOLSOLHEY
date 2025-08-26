// API 공통 응답 타입
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
  errors?: Record<string, string>;
}

// 회원가입 관련 타입
export type Campus = {
  id: number;
  name: string;
};

export type SignupRequest = {
  userId: string;
  password: string;
  nickname: string;
  campus: string;
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
  data?: {
    token: string;
    username: string;
  };
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
  userId: number;
  nickname: string;
}

// 마스코트 관련 타입 (백엔드 응답 구조에 맞춤)
export interface MascotCreateRequest {
  name: string;
  type: string;
}

export interface MascotResponse {
  id: number;
  userId: number;
  name: string;
  type: string;
  equippedItem?: string;
  exp: number;
  level: number;
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

// 마스코트 관련 타입 (백엔드 응답 구조에 맞춤)
export interface Mascot {
  id: number;
  name: string;
  type: string; // 마스코트 종류 (ex: 'soll', 'ray', 'rino', 'pli' 등)
  level: number;
  exp: number; // 백엔드의 exp 필드와 일치
  equippedItem?: string; // 백엔드의 equippedItem 필드와 일치 (단순 문자열)
  createdAt?: string;
  updatedAt?: string;
}

// evolutionStage는 level 기반으로 계산 (프론트엔드에서 처리)
export interface EquippedItems {
  clothing?: Item;
  background?: Item;
  accessory?: Item;
  head?: Item;
}

export interface Item {
  id: number;
  name: string;
  type: 'clothing' | 'background' | 'accessory' | 'head';
  description: string;
  price: number;
  imageUrl?: string;
  rarity: 'common' | 'rare' | 'epic' | 'legendary';
  isOwned?: boolean;
}

// 마스코트 생성 요청/응답 (백엔드 구조에 맞춤)
export interface CreateMascotRequest {
  name: string;
  type: string;
  equippedItem?: string; // 백엔드의 equippedItem 필드와 일치
}

export interface CreateMascotResponse {
  success: boolean;
  message: string;
  mascot?: Mascot;
  errors?: Record<string, string>;
}

// 마스코트 꾸미기 요청/응답 (백엔드 구조에 맞춤)
export interface EquipItemsRequest {
  equippedItem: string; // 백엔드의 equippedItem 필드와 일치
}

export interface EquipItemsResponse {
  success: boolean;
  message: string;
  mascot?: Mascot;
  errors?: Record<string, string>;
}

// 마스코트 수정 요청/응답 (백엔드 구조에 맞춤)
export interface UpdateMascotRequest {
  name?: string;
  equippedItem?: string; // 백엔드의 equippedItem 필드와 일치
  expToAdd?: number; // 백엔드의 expToAdd 필드와 일치
}

export interface UpdateMascotResponse {
  success: boolean;
  message: string;
  mascot?: Mascot;
  errors?: Record<string, string>;
}

// 마스코트 조회 응답
export interface GetMascotResponse {
  success: boolean;
  message: string;
  mascot?: Mascot;
  errors?: Record<string, string>;
}

// 아이템 목록 조회 응답
export interface GetItemsResponse {
  success: boolean;
  message: string;
  items?: Item[];
  errors?: Record<string, string>;
}

// 챌린지 관련 타입 정의 (백엔드 ChallengeListResponseDto에 맞춤)
export interface Challenge {
  challengeId: number;
  challengeName: string;
  description: string;
  rewardPoints: number;
  rewardExp: number;
  challengeType: string;
  difficulty: string;
  categoryName: string;
  categoryDisplayName: string;
  startDate?: string;
  endDate?: string;
  targetCount: number;
  isActive: boolean;
  isAvailable: boolean;
  participantCount?: number;
  completedCount?: number;
  isJoined: boolean;
  userStatus: string;
}

export interface ChallengeListResponse {
  success: boolean;
  message: string;
  data?: Challenge[];
  errors?: Record<string, string>;
}

export interface ChallengeDetailResponse {
  success: boolean;
  message: string;
  data?: Challenge;
  errors?: Record<string, string>;
}

export interface ChallengeJoinRequest {
  challengeId: number;
}

export interface ChallengeJoinResponse {
  success: boolean;
  message: string;
  data?: {
    challengeId: number;
    userId: number;
    status: string;
  };
  errors?: Record<string, string>;
}

export interface ChallengeProgressRequest {
  step: number;
  payload?: string;
}

export interface ChallengeProgressResponse {
  success: boolean;
  message: string;
  userChallenge?: {
    userChallengeId: number;
    status: string;
    statusDisplayName: string;
    progressCount: number;
    targetCount: number;
    progressRate: number;
    startedAt: string;
    completedAt?: string;
    progressData?: string;
  };
  isCompleted?: boolean;
  rewardPoints?: number;
  rewardExp?: number;
  errors?: Record<string, string>;
}

// 사용자 관련 타입 정의
export interface UserResponse {
  userId: number;
  username: string;
  email: string;
  nickname: string;
  campus?: string;
  totalPoints: number;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// 상점 관련 타입 정의
export interface ShopItem {
  id: number;
  name: string;
  description: string;
  price: number;
  type: string;
  imageUrl: string;
  isActive: boolean;
}

export interface Gifticon {
  id: number;
  name: string;
  imageUrl: string;
  price: number;
  description: string;
  sku: string;
}

export interface OrderRequest {
  type: 'ITEM' | 'GIFTICON';
  itemId?: number;
  quantity?: number;
  sku?: string;
}

export interface OrderResponse {
  id: number;
  userId: number;
  type: string;
  totalPrice: number;
  status: string;
  message: string;
}

export interface UserItem {
  id: number;
  userId: number;
  itemId: number;
  quantity: number;
  item: ShopItem;
}




