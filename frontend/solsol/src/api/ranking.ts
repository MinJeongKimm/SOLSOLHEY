import { apiRequest } from './index';
import { auth } from './index';
import type { ApiResponse, UserResponse } from '../types/api';

// 랭킹 관련 타입 정의 (마스코트 기반)
export interface RankingEntry {
  entryId: number;
  rank: number;
  mascotId: number;
  mascotSnapshotId: number;
  ownerNickname: string;
  mascotName: string;
  entryTitle: string;
  votes: number;
  backgroundId: string;
  imageUrl: string;
  entryImageUrl: string;
  school?: {
    id: number | null;
    name: string;
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
  weight?: number;
  idempotencyKey?: string;
  campusId?: number;
}

export interface VoteResponse {
  success: boolean;
  message: string;
  mascotId?: number;
  newVotes?: number;
  votedAt?: string;
}

// 타입 정의
export interface CreateEntryRequest {
  mascotSnapshotId: number;
  title: string;
  description?: string;
}

export interface EntryResponse {
  entryId: number;
  userId: number;
  mascotSnapshotId: number;
  title: string;
  description?: string;
  createdAt: string;
  imageUrl?: string; // 등록 시 업로드한 이미지 URL
  mascotId?: number; // 마스코트 ID
  rankingType?: string; // 랭킹 타입 (CAMPUS/NATIONAL)
}

export interface LeaderboardResponse {
  content: LeaderboardEntry[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}

export interface LeaderboardEntry {
  entryId: number;
  userId: number;
  mascotSnapshotId: number;
  title: string;
  description?: string;
  createdAt: string;
  rank?: number;
  voteCount?: number;
}

// 마스코트 스냅샷 관련 타입
export interface MascotSnapshot {
  id: number;
  userId: number;
  mascotId: number;
  imageUrl: string;
  createdAt: string;
}

// 마스코트 정보 타입
export interface Mascot {
  id: number;
  userId: number;
  name: string;
  backgroundId?: string;
  createdAt: string;
}

// 랭킹 참가 등록
export async function createRankingEntry(request: CreateEntryRequest): Promise<EntryResponse> {
  const response = await apiRequest<ApiResponse<EntryResponse>>('http://localhost:8080/api/ranking/entries', {
    method: 'POST',
    body: JSON.stringify(request),
  });
  if (!response.data) throw new Error('응답 데이터가 없습니다.');
  return response.data;
}

// 쿠키에서 값을 가져오는 헬퍼 함수
function getCookie(name: string): string | null {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
  return null;
}

// 랭킹 참가 등록 (이미지 포함)
export async function createRankingEntryWithImage(
  title: string, 
  description: string, 
  mascotImageBlob: Blob,
  rankingType: string
): Promise<EntryResponse> {
  try {
    // FormData 생성
    const formData = new FormData();
    formData.append('title', title);
    if (description) {
      formData.append('description', description);
    }
    formData.append('mascotImage', mascotImageBlob, 'mascot_ranking.png');
    formData.append('rankingType', rankingType);

    // CSRF 토큰 가져오기
    const csrfToken = getCookie('XSRF-TOKEN');
    
    // 이미지 업로드 API 호출
    const response = await fetch('http://localhost:8080/api/ranking/entries/with-image', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'X-XSRF-TOKEN': csrfToken || '',
      },
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || '랭킹 참가 등록에 실패했습니다.');
    }

    const result = await response.json();
    if (!result.data) throw new Error('응답 데이터가 없습니다.');
    
    return result.data;
  } catch (error) {
    console.error('랭킹 참가 등록 실패:', error);
    throw error;
  }
}

// 사용자 참가 목록 조회
export async function getUserEntries(): Promise<EntryResponse[]> {
  const response = await apiRequest<ApiResponse<EntryResponse[]>>('http://localhost:8080/api/ranking/entries/user');
  if (!response.data) throw new Error('응답 데이터가 없습니다.');
  return response.data;
}

// 사용자 특정 타입 참가 목록 조회
export async function getUserEntriesByType(rankingType: string): Promise<EntryResponse[]> {
  const response = await apiRequest<ApiResponse<EntryResponse[]>>(`http://localhost:8080/api/ranking/entries/user/type/${rankingType}`);
  if (!response.data) throw new Error('응답 데이터가 없습니다.');
  return response.data;
}

// 참가 취소 (삭제)
export async function deleteRankingEntry(entryId: number): Promise<void> {
  await apiRequest<ApiResponse<void>>(`http://localhost:8080/api/ranking/entries/${entryId}`, {
    method: 'DELETE',
  });
}

// 전체 랭킹 보드 조회
export async function getLeaderboard(page: number = 0, size: number = 20): Promise<LeaderboardResponse> {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString()
  });
  const response = await apiRequest<ApiResponse<LeaderboardResponse>>(`http://localhost:8080/api/ranking/entries/leaderboard?${params}`);
  if (!response.data) throw new Error('응답 데이터가 없습니다.');
  return response.data;
}

// 사용자 참가 개수 조회
export async function getUserEntryCount(): Promise<number> {
  const response = await apiRequest<ApiResponse<number>>('http://localhost:8080/api/ranking/entries/user/count');
  if (!response.data) throw new Error('응답 데이터가 없습니다.');
  return response.data;
}

// 사용자의 마스코트 스냅샷 조회 (가장 최근 것)
export async function getCurrentUserMascotSnapshot(): Promise<MascotSnapshot | null> {
  try {
    console.log('마스코트 스냅샷 조회 시작...');
    
    // 백엔드의 실제 경로: /api/v1/mascot/snapshots
    const response = await apiRequest<any>('http://localhost:8080/api/v1/mascot/snapshots');
    console.log('마스코트 스냅샷 응답:', response);
    
    if (response.result !== 'SUCCESS' || !response.data || response.data.length === 0) {
      console.log('마스코트 스냅샷 데이터 없음:', response);
      return null;
    }
    
    // 가장 최근 스냅샷 반환 (createdAt 기준으로 정렬)
    const sortedSnapshots = response.data.sort((a: any, b: any) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
    console.log('정렬된 스냅샷:', sortedSnapshots);
    
    return sortedSnapshots[0];
  } catch (error) {
    console.error('마스코트 스냅샷 조회 실패:', error);
    return null;
  }
}

// 마스코트 타입에 따른 이미지 URL 생성 (이미지 공유와 동일한 로직)
function getMascotImageUrl(type: string): string {
  if (!type) return '/mascot/soll.png';
  
  const mascotTypes = ['ray', 'rino', 'pli', 'soll'];
  if (mascotTypes.includes(type)) {
    return `/mascot/${type}.png`;
  }
  
  return '/mascot/soll.png'; // 기본값
}

// 실시간 마스코트 이미지 합성 (이미지 공유 기능과 동일한 로직)
export async function composeMascotImage(
  mascot: any, 
  customization: any, 
  shopItems: any[]
): Promise<string> {
  try {
    console.log('마스코트 이미지 합성 시작...');
    console.log('마스코트 타입:', mascot?.type);
    
    // 캔버스 설정
    const DPR = Math.max(1, Math.min(3, Math.floor(window.devicePixelRatio || 1)));
    const canvasSize = 400; // 랭킹용으로 크기 조정
    const canvas = document.createElement('canvas');
    canvas.width = canvasSize * DPR;
    canvas.height = canvasSize * DPR;
    const ctx = canvas.getContext('2d');
    if (!ctx) throw new Error('Canvas context unavailable');
    ctx.scale(DPR, DPR);
    ctx.imageSmoothingEnabled = true;

    // 이미지 로드 헬퍼 함수
    const loadImage = (src: string): Promise<HTMLImageElement> => {
      return new Promise((resolve, reject) => {
        const img = new Image();
        img.crossOrigin = 'anonymous';
        img.onload = () => resolve(img);
        img.onerror = () => reject(new Error(`이미지 로드 실패: ${src}`));
        img.src = src;
      });
    };

    // 1. 배경 생성 (꾸미기 화면과 동일한 방식)
    console.log('마스코트 배경 정보:', {
      backgroundColor: mascot?.backgroundColor,
      backgroundPattern: mascot?.backgroundPattern
    });
    
    // 배경색 설정
    const bgColor = mascot?.backgroundColor || '#ffffff';
    ctx.fillStyle = bgColor;
    ctx.fillRect(0, 0, canvasSize, canvasSize);
    
    // 배경 패턴 그리기
    if (mascot?.backgroundPattern === 'dots') {
      ctx.fillStyle = 'rgba(0,0,0,0.12)';
      for (let x = 6; x < canvasSize; x += 12) {
        for (let y = 6; y < canvasSize; y += 12) {
          ctx.beginPath();
          ctx.arc(x, y, 1, 0, 2 * Math.PI);
          ctx.fill();
        }
      }
    } else if (mascot?.backgroundPattern === 'stripes') {
      ctx.fillStyle = 'rgba(0,0,0,0.08)';
      for (let i = 0; i < canvasSize; i += 20) {
        ctx.fillRect(i, 0, 10, canvasSize);
      }
    }

    // 2. 마스코트 기본 이미지 로드 및 그리기 (이미지 공유와 동일한 로직 사용)
    const mascotUrl = getMascotImageUrl(mascot?.type);
    console.log('마스코트 이미지 URL:', mascotUrl);
    const mascotImg = await loadImage(mascotUrl);
    const mascotBoxSize = Math.floor(canvasSize * 0.5); // 중앙 50%
    const mascotX = (canvasSize - mascotBoxSize) / 2;
    const mascotY = (canvasSize - mascotBoxSize) / 2;
    ctx.drawImage(mascotImg, mascotX, mascotY, mascotBoxSize, mascotBoxSize);

    // 3. 아이템들(커스터마이징) 실시간 합성
    if (customization && customization.equippedItems?.length) {
      const byId = new Map<number, any>(shopItems.map(s => [s.id, s]));
      const UI_MASCOT_PX = 128;
      const BASE_ITEM_SIZE = 120; // 기본 아이템 크기
      const baseItemSize = (BASE_ITEM_SIZE / UI_MASCOT_PX) * mascotBoxSize;

      for (const e of customization.equippedItems) {
        const si = byId.get(e.itemId);
        if (!si) continue;
        
        try {
          const img = await loadImage(si.imageUrl);
          const centerX = mascotX + (e.relativePosition.x * mascotBoxSize);
          const centerY = mascotY + (e.relativePosition.y * mascotBoxSize);
          const size = Math.max(12, baseItemSize * (e.scale ?? 1));
          const rot = (((e.rotation ?? 0) % 360) + 360) % 360;

          ctx.save();
          ctx.translate(centerX, centerY);
          ctx.rotate((rot * Math.PI) / 180);
          ctx.drawImage(img, -size / 2, -size / 2, size, size);
          ctx.restore();
        } catch (error) {
          console.warn('아이템 이미지 로드 실패:', e.itemId, error);
        }
      }
    }

    // Canvas를 Data URL로 변환
    const dataUrl = canvas.toDataURL('image/png');
    console.log('마스코트 이미지 합성 완료');
    return dataUrl;
    
  } catch (error) {
    console.error('마스코트 이미지 합성 실패:', error);
    // 실패 시에도 getMascotImageUrl 함수 사용하여 올바른 이미지 반환
    return getMascotImageUrl(mascot?.type);
  }
}

// 사용자의 마스코트 정보 조회
export async function getCurrentUserMascot(): Promise<Mascot | null> {
  try {
    console.log('마스코트 정보 조회 시작...');
    
    // 백엔드의 실제 경로: /api/v1/mascot (마스코트 기본 정보)
    const response = await apiRequest<any>('http://localhost:8080/api/v1/mascot');
    console.log('마스코트 기본 정보 응답:', response);
    
    if (response.result !== 'SUCCESS' || !response.data) {
      console.log('마스코트 기본 정보 데이터 없음:', response);
      return null;
    }
    
    // MascotResponse에서 마스코트 정보 추출
    const mascotData = response.data;
    console.log('마스코트 데이터:', mascotData);
    
    if (!mascotData) {
      console.log('마스코트 데이터가 없음');
      return null;
    }
    
    const result = {
      id: mascotData.id,
      userId: mascotData.userId,
      name: mascotData.name,
      type: mascotData.type, // 마스코트 타입 추가!
      backgroundId: mascotData.backgroundId,
      backgroundColor: mascotData.backgroundColor,
      backgroundPattern: mascotData.backgroundPattern,
      createdAt: mascotData.createdAt
    };
    
    console.log('반환할 마스코트 정보:', result);
    console.log('마스코트 타입 확인:', result.type);
    return result;
  } catch (error) {
    console.error('마스코트 정보 조회 실패:', error);
    return null;
  }
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
  if (!res || (res as any).success === false) throw new Error((res as any)?.message || '전국 랭킹 조회 실패');
  return res.data as RankingResponse;
}

// 교내 랭킹 투표 (마스코트 기반)
export async function voteForCampus(
  mascotId: number,
  voteData: VoteRequest
): Promise<VoteResponse> {
  const res = await apiRequest<ApiResponse<VoteResponse>>(`/rankings/campus/${mascotId}/vote`, {
    method: 'POST',
    body: JSON.stringify(voteData),
  });
  
  if (!res || (res as any).success === false) {
    throw new Error((res as any)?.message || '투표 실패');
  }
  
  return res.data as VoteResponse;
}

// 전국 랭킹 투표 (마스코트 기반)
export async function voteForNational(
  mascotId: number,
  voteData: VoteRequest
): Promise<VoteResponse> {
  const res = await apiRequest<ApiResponse<VoteResponse>>(`/rankings/national/${mascotId}/vote`, {
    method: 'POST',
    body: JSON.stringify(voteData),
  });
  
  if (!res || (res as any).success === false) {
    throw new Error((res as any)?.message || '투표 실패');
  }
  
  return res.data as VoteResponse;
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

// 사용자 투표 히스토리 조회 (투표한 마스코트 ID 목록)
export async function getUserVotedMascotIds(): Promise<number[]> {
  try {
    const res = await apiRequest<ApiResponse<number[]>>('/rankings/my-votes');
    if (!res || (res as any).success === false || !res.data) {
      throw new Error((res as any)?.message || '투표 히스토리 조회 실패');
    }
    return res.data as number[];
  } catch (error: any) {
    if (error?.status === 401 || error?.status === 403) {
      throw new Error('로그인이 필요합니다.');
    }
    throw error;
  }
}

// 사용자 교내 랭킹 투표 히스토리 조회 (투표한 마스코트 ID 목록)
export async function getUserCampusVotedMascotIds(): Promise<number[]> {
  try {
    const res = await apiRequest<ApiResponse<number[]>>('/rankings/campus/my-votes');
    if (!res || (res as any).success === false || !res.data) {
      throw new Error((res as any)?.message || '교내 랭킹 투표 히스토리 조회 실패');
    }
    return res.data as number[];
  } catch (error: any) {
    if (error?.status === 401 || error?.status === 403) {
      throw new Error('로그인이 필요합니다.');
    }
    throw error;
  }
}

// 사용자 전국 랭킹 투표 히스토리 조회 (투표한 마스코트 ID 목록)
export async function getUserNationalVotedMascotIds(): Promise<number[]> {
  try {
    const res = await apiRequest<ApiResponse<number[]>>('/rankings/national/my-votes');
    if (!res || (res as any).success === false || !res.data) {
      throw new Error((res as any)?.message || '전국 랭킹 투표 히스토리 조회 실패');
    }
    return res.data as number[];
  } catch (error: any) {
    if (error?.status === 401 || error?.status === 403) {
      throw new Error('로그인이 필요합니다.');
    }
    throw error;
  }
}

// 사용자 교내 랭킹 투표 히스토리 조회 (투표한 엔트리 ID 목록)
export async function getUserCampusVotedEntryIds(): Promise<number[]> {
  try {
    const res = await apiRequest<ApiResponse<number[]>>('/api/ranking/campus/voted-entries', {
      method: 'GET',
    });
    if (!res || (res as any).success === false || !res.data) {
      throw new Error((res as any)?.message || '교내 랭킹 투표한 엔트리 ID 조회 실패');
    }
    return res.data as number[];
  } catch (error: any) {
    if (error?.status === 401 || error?.status === 403) {
      throw new Error('로그인이 필요합니다.');
    }
    throw error;
  }
}

// 사용자 전국 랭킹 투표 히스토리 조회 (투표한 엔트리 ID 목록)
export async function getUserNationalVotedEntryIds(): Promise<number[]> {
  try {
    const res = await apiRequest<ApiResponse<number[]>>('/api/ranking/national/voted-entries', {
      method: 'GET',
    });
    if (!res || (res as any).success === false || !res.data) {
      throw new Error((res as any)?.message || '전국 랭킹 투표한 엔트리 ID 조회 실패');
    }
    return res.data as number[];
  } catch (error: any) {
    if (error?.status === 401 || error?.status === 403) {
      throw new Error('로그인이 필요합니다.');
    }
    throw error;
  }
}

// 교내 랭킹 엔트리들에 대한 투표 가능 여부 조회
export async function getCampusVoteableStatus(entryIds: number[]): Promise<Record<number, boolean>> {
  try {
    const res = await apiRequest<ApiResponse<Record<number, boolean>>>('/rankings/campus/voteable-status', {
      method: 'POST',
      body: JSON.stringify(entryIds),
    });
    if (!res || (res as any).success === false || !res.data) {
      throw new Error((res as any)?.message || '교내 랭킹 투표 가능 여부 조회 실패');
    }
    return res.data as Record<number, boolean>;
  } catch (error: any) {
    if (error?.status === 401 || error?.status === 403) {
      throw new Error('로그인이 필요합니다.');
    }
    throw error;
  }
}

// 전국 랭킹 엔트리들에 대한 투표 가능 여부 조회
export async function getNationalVoteableStatus(entryIds: number[]): Promise<Record<number, boolean>> {
  try {
    const res = await apiRequest<ApiResponse<Record<number, boolean>>>('/rankings/national/voteable-status', {
      method: 'POST',
      body: JSON.stringify(entryIds),
    });
    if (!res || (res as any).success === false || !res.data) {
      throw new Error((res as any)?.message || '전국 랭킹 투표 가능 여부 조회 실료');
    }
    return res.data as Record<number, boolean>;
  } catch (error: any) {
    if (error?.status === 401 || error?.status === 403) {
      throw new Error('로그인이 필요합니다.');
    }
    throw error;
  }
}
