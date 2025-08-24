import { apiRequest } from './index';
import { getCurrentUserId } from '../utils/jwt';

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

  return apiRequest<RankingResponse>(`/rankings/campus?${params}`);
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

  return apiRequest<RankingResponse>(`/rankings/national?${params}`);
}

// 교내 랭킹 투표
export async function voteForCampus(
  entryId: number,
  voteData: VoteRequest
): Promise<VoteResponse> {
  const userId = getCurrentUserId();
  if (!userId) {
    throw new Error('로그인이 필요합니다.');
  }

  // userID를 request body에 포함
  const requestData = {
    ...voteData,
    userId: userId
  };

  return apiRequest<VoteResponse>(`/rankings/campus/${entryId}/vote`, {
    method: 'POST',
    body: JSON.stringify(requestData),
  });
}

// 전국 랭킹 투표
export async function voteForNational(
  entryId: number,
  voteData: VoteRequest
): Promise<VoteResponse> {
  const userId = getCurrentUserId();
  if (!userId) {
    throw new Error('로그인이 필요합니다.');
  }

  // userID를 request body에 포함
  const requestData = {
    ...voteData,
    userId: userId
  };

  return apiRequest<VoteResponse>(`/rankings/national/${entryId}/vote`, {
    method: 'POST',
    body: JSON.stringify(requestData),
  });
}

// 사용자 정보 조회 (campus 정보 포함)
export async function getCurrentUser(): Promise<{
  userId: number;
  username: string;
  email: string;
  nickname: string;
  campus: string;
  totalPoints: number;
  isActive: boolean;
}> {
  // JWT 토큰에서 사용자 ID를 가져옴
  const userId = getCurrentUserId();
  if (!userId) {
    throw new Error('로그인이 필요합니다.');
  }

  return apiRequest<any>(`/users/${userId}`);
}
