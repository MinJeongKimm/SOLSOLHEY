import { apiRequest } from './index';

export interface Friend {
  friendId: number;
  userId?: number;
  nickname?: string;
  campus?: string;
  totalPoints?: number;
  email?: string;
}

export interface User {
  userId: number;
  nickname?: string;
  campus?: string;
  totalPoints?: number;
  isAlreadyFriend?: boolean;
  hasPendingRequest?: boolean;
  email?: string;
}

export interface FriendRequest {
  friendUserId: number;
}

export interface PendingFriendRequest {
  requestId: number;
  userId: number;
  nickname?: string;
  campus?: string;
  totalPoints?: number;
  createdAt: string;
  email?: string;
}

// 백엔드에서 반환되는 FriendResponse 구조
export interface FriendResponse {
  friendId: number;
  userId: number;
  nickname: string;
  email: string;
  campus: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

// 친구 홈 응답 DTO(백엔드 FriendHomeResponse와 매핑)
export interface FriendHomeResponse {
  ownerId: number;
  nickname: string;
  level: number;
  likeCount: number; // 누적 합
  likeSentToday: number;
  likeReceivedToday: number;
  likeAllowedMax: number;
  likeRemainingToday: number;
  canLikeNow: boolean;
  viewMode: 'SELF' | 'OTHER';
  permissions: { editAllowed?: boolean; [k: string]: any };
  mascot: { name: string; type: string; backgroundId?: string; equippedItem?: string };
  isFriend: boolean;
  isOwner: boolean;
}

// 받은 상호작용 응답 DTO(백엔드 FriendInteractionResponse와 매핑)
export interface FriendInteraction {
  interactionId: number;
  fromUserId: number;
  fromUserNickname: string;
  toUserId: number;
  toUserNickname: string;
  interactionType: 'LIKE' | 'CHEER' | 'POKE' | 'MESSAGE' | 'FRIEND_REQUEST';
  message?: string;
  referenceId?: number; // 서버 referenceId: 친구요청 등 원본 엔티티 ID
  isRead: boolean;
  createdAt: string;
}

// 백엔드에서 반환되는 Page 구조
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface SearchUserResponse {
  users: User[];
}

export interface FriendListResponse {
  friends: Friend[];
}

export interface FriendRequestListResponse {
  requests: PendingFriendRequest[];
}

// 친구 홈 데이터 조회
export const getFriendHome = async (friendId: number): Promise<FriendHomeResponse> => {
  const res = await apiRequest<{ data: FriendHomeResponse }>(`/friends/${friendId}/home`, { method: 'GET' });
  if (!res || !res.data) throw new Error('친구 홈 데이터를 가져오지 못했습니다.');
  return res.data;
};

// 친구에게 좋아요 전송(핑퐁 제한은 서버에서 검증)
export const sendLike = async (toUserId: number, message?: string): Promise<void> => {
  await apiRequest<void>('/friends/interactions', {
    method: 'POST',
    body: JSON.stringify({ toUserId, interactionType: 'LIKE', message }),
  });
};

// 상호작용(알림) 목록 조회 (우선 LIKE만 필터링은 클라이언트에서 처리)
export const getInteractions = async (page = 0, size = 20): Promise<PageResponse<FriendInteraction>> => {
  const res = await apiRequest<{ data: PageResponse<FriendInteraction> }>(`/friends/interactions?page=${page}&size=${size}`, { method: 'GET' });
  return res.data as PageResponse<FriendInteraction>;
};

// 상호작용 읽음 처리
export const markInteractionRead = async (interactionId: number): Promise<void> => {
  await apiRequest<void>(`/friends/interactions/${interactionId}/read`, { method: 'PUT' });
};

// 받은 상호작용 모두 읽음 처리
export const markAllInteractionsRead = async (): Promise<void> => {
  await apiRequest<void>(`/friends/interactions/read-all`, { method: 'PUT' });
};

// 미읽음 상호작용 카운트
export const getUnreadInteractionCount = async (): Promise<number> => {
  const res = await apiRequest<{ data: number }>(`/friends/interactions/unread-count`, { method: 'GET' });
  return (res && (res as any).data) as number;
};

// 친구 목록 조회
export const getFriendList = async (): Promise<Friend[]> => {
  try {
    console.log('친구 목록 조회 API 호출 시작: /friends');
    const response = await apiRequest<{ data: PageResponse<FriendResponse> }>('/friends');
    console.log('친구 목록 API 응답:', response);
    
    // 백엔드에서 Page<FriendResponse> 형태로 반환되므로 data.content에서 추출
    // FriendResponse를 Friend 형태로 변환
    const result = (response.data?.content || []).map(friend => ({
      friendId: friend.friendId,
      userId: friend.userId,
      nickname: friend.nickname,
      campus: friend.campus,
      email: friend.email,
      totalPoints: 0 // 기본값 설정
    }));
    
    console.log('변환된 친구 목록:', result);
    return result;
  } catch (error) {
    console.error('친구 목록 조회 실패:', error);
    throw error;
  }
};

// 친구 요청 목록 조회
export const getFriendRequests = async (): Promise<PendingFriendRequest[]> => {
  try {
    console.log('API 호출 시작: /friends/requests/received');
    const response = await apiRequest<{ data: PageResponse<FriendResponse> }>('/friends/requests/received');
    console.log('API 응답:', response);
    
    // 백엔드에서 Page<FriendResponse> 형태로 반환되므로 data.content에서 추출
    // FriendResponse를 PendingFriendRequest 형태로 변환
    const result = (response.data?.content || []).map(friend => ({
      requestId: friend.friendId,
      userId: friend.userId,
      nickname: friend.nickname,
      campus: friend.campus,
      email: friend.email,
      totalPoints: 0, // 기본값 설정
      createdAt: friend.createdAt
    }));
    
    console.log('변환된 결과:', result);
    return result;
  } catch (error) {
    console.error('친구 요청 목록 조회 실패:', error);
    throw error;
  }
};

// 사용자 검색
export async function searchUsers(query: string): Promise<any[]> {
  try {
    const response = await apiRequest<{ data: any[] }>(`/friends/search?keyword=${encodeURIComponent(query)}`);
    return response.data || [];
  } catch (error) {
    console.error('사용자 검색 실패:', error);
    throw error;
  }
}

// 친구 요청 전송
export async function sendFriendRequest(friendUserId: number): Promise<void> {
  try {
    await apiRequest<void>('/friends/requests', {
      method: 'POST',
      body: JSON.stringify({ friendUserId }),
    });
  } catch (error) {
    console.error('친구 요청 실패:', error);
    throw error;
  }
}

// 친구 요청 수락
export const acceptFriendRequest = async (friendUserId: number): Promise<void> => {
  try {
    await apiRequest<void>(`/friends/requests/${friendUserId}/accept`, {
      method: 'PUT',
    });
  } catch (error) {
    console.error('친구 요청 수락 실패:', error);
    throw error;
  }
};

// 친구 요청 거절
export const rejectFriendRequest = async (friendUserId: number): Promise<void> => {
  try {
    await apiRequest<void>(`/friends/requests/${friendUserId}/reject`, {
      method: 'PUT',
    });
  } catch (error) {
    console.error('친구 요청 거절 실패:', error);
    throw error;
  }
};

// 친구 삭제
export const deleteFriend = async (friendUserId: number): Promise<void> => {
  try {
    await apiRequest<void>(`/friends/${friendUserId}`, {
      method: 'DELETE',
    });
  } catch (error) {
    console.error('친구 삭제 실패:', error);
    throw error;
  }
};
