import { apiRequest } from './index';
import { auth } from './index';
import type { ApiResponse, UserResponse } from '../types/api';

// 랭킹 관련 타입 정의
export interface RankingEntry {
  rank: number;
  entryId: number;
  mascotId: number;
  ownerNickname: string;
  votes: number;
  trendScore: number;
  thumbnailUrl: string;
  school?: {
    schoolId: number;
    schoolName: string;
  };
}

export interface CampusInfo {
  campusId: number;
  campusName: string;
}

export interface FilterInfo {
  region?: string;
  schoolId?: number;
}

export interface RankingResponse {
  total: number;
  page: number;
  size: number;
  period: string;
  sort: string;
  campus?: CampusInfo;
  filters?: FilterInfo;
  entries: RankingEntry[];
}

export interface VoteRequest {
  voteType: 'LIKE' | 'DISLIKE';
  comment?: string;
  userId?: number; // JWT 토큰에서 추출한 사용자 ID
}

export interface VoteResponse {
  success: boolean;
  message: string;
  newVoteCount?: number;
  userVoteStatus?: 'LIKE' | 'DISLIKE' | 'NONE';
}

// 교내 랭킹 조회
export async function getCampusRankings(
  campusId?: number,
  sort: string = 'votes_desc',
  period: string = 'weekly',
  page: number = 0,
  size: number = 20
): Promise<RankingResponse> {
  const params = new URLSearchParams({
    sort,
    period,
    page: page.toString(),
    size: size.toString()
  });
  
  if (campusId) {
    params.append('campusId', campusId.toString());
  }

  const res = await apiRequest<ApiResponse<RankingResponse>>(`/rankings/campus?${params}`);
  if (!res || (res as any).success === false) throw new Error((res as any)?.message || '랭킹 조회 실패');
  return res.data as RankingResponse;
}

// 전국 랭킹 조회
export async function getNationalRankings(
  sort: string = 'votes_desc',
  period: string = 'weekly',
  region?: string,
  schoolId?: number,
  page: number = 0,
  size: number = 20
): Promise<RankingResponse> {
  const params = new URLSearchParams({
    sort,
    period,
    page: page.toString(),
    size: size.toString()
  });
  
  if (region) {
    params.append('region', region);
  }
  
  if (schoolId) {
    params.append('schoolId', schoolId.toString());
  }

  const res = await apiRequest<ApiResponse<RankingResponse>>(`/rankings/national?${params}`);
  if (!res || (res as any).success === false) throw new Error((res as any)?.message || '랭킹 조회 실패');
  return res.data as RankingResponse;
}

// 교내 랭킹 투표
export async function voteForCampus(
  entryId: number,
  voteData: VoteRequest
): Promise<VoteResponse> {
  // 백엔드가 JWT에서 사용자 식별 → userId 전달 불필요
  return apiRequest<VoteResponse>(`/rankings/campus/${entryId}/vote`, {
    method: 'POST',
    body: JSON.stringify(voteData),
  });
}

// 전국 랭킹 투표
export async function voteForNational(
  entryId: number,
  voteData: VoteRequest
): Promise<VoteResponse> {
  return apiRequest<VoteResponse>(`/rankings/national/${entryId}/vote`, {
    method: 'POST',
    body: JSON.stringify(voteData),
  });
}

// 사용자 정보 조회 (campus 정보 포함)
export async function getCurrentUser(): Promise<UserResponse> {
  try {
    const res = await apiRequest<ApiResponse<UserResponse>>('/auth/me');
    if (!res || (res as any).success === false || !res.data) {
      throw new Error((res as any)?.message || '사용자 정보 조회 실패');
    }
    return res.data as UserResponse;
  } catch (error: any) {
    if (error?.status === 401 || error?.status === 403) {
      throw new Error('로그인이 필요합니다.');
    }
    throw error;
  }
}
