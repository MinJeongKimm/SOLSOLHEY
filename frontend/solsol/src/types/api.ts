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

// 마스코트 관련 타입
export interface Mascot {
  id: number;
  name: string;
  type: string; // 마스코트 종류 (ex: '곰', '호랑이', '독수리' 등)
  level: number;
  experiencePoint: number;
  evolutionStage: number;
  equippedItems: EquippedItems;
  createdAt?: string;
  updatedAt?: string;
}

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

// 마스코트 생성 요청/응답
export interface CreateMascotRequest {
  name: string;
  type: string;
  equippedItems?: {
    clothingId?: number;
    backgroundId?: number;
    accessoryId?: number;
    headId?: number;
  };
}

export interface CreateMascotResponse {
  success: boolean;
  message: string;
  mascot?: Mascot;
  errors?: Record<string, string>;
}

// 마스코트 꾸미기 요청/응답
export interface EquipItemsRequest {
  equipItems: {
    clothingId?: number;
    backgroundId?: number;
    accessoryId?: number;
    headId?: number;
  };
}

export interface EquipItemsResponse {
  success: boolean;
  message: string;
  mascot?: Mascot;
  errors?: Record<string, string>;
}

// 마스코트 수정 요청/응답
export interface UpdateMascotRequest {
  name?: string;
  equipItems?: {
    clothingId?: number;
    backgroundId?: number;
    accessoryId?: number;
    headId?: number;
  };
  experiencePoint?: number;
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




