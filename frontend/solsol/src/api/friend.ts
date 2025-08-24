import { apiRequest } from './index';

export interface Friend {
  friendId: number;
  nickname?: string;
  campus?: string;
  totalPoints?: number;
  username?: string;
}

export interface User {
  userId: number;
  username: string;
  nickname?: string;
  campus?: string;
  totalPoints?: number;
}

export interface FriendRequest {
  friendUserId: number;
}

export interface PendingFriendRequest {
  requestId: number;
  userId: number;
  username: string;
  nickname?: string;
  campus?: string;
  totalPoints?: number;
  createdAt: string;
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

// 친구 목록 조회
export const getFriendList = async (): Promise<Friend[]> => {
  try {
    const response = await apiRequest<FriendListResponse>('/friends');
    return response.friends;
  } catch (error) {
    console.error('친구 목록 조회 실패:', error);
    throw error;
  }
};

// 친구 요청 목록 조회
export const getFriendRequests = async (): Promise<PendingFriendRequest[]> => {
  try {
    const response = await apiRequest<FriendRequestListResponse>('/friends/requests');
    return response.requests;
  } catch (error) {
    console.error('친구 요청 목록 조회 실패:', error);
    throw error;
  }
};

// 사용자 검색
export const searchUsers = async (query: string): Promise<User[]> => {
  try {
    const response = await apiRequest<SearchUserResponse>(`/users/search?q=${encodeURIComponent(query)}`);
    return response.users;
  } catch (error) {
    console.error('사용자 검색 실패:', error);
    throw error;
  }
};

// 친구 요청 전송
export const sendFriendRequest = async (friendUserId: number): Promise<void> => {
  try {
    await apiRequest<void>('/friends/request', {
      method: 'POST',
      body: JSON.stringify({ friendUserId }),
    });
  } catch (error) {
    console.error('친구 요청 실패:', error);
    throw error;
  }
};

// 친구 요청 수락
export const acceptFriendRequest = async (friendUserId: number): Promise<void> => {
  try {
    await apiRequest<void>(`/friends/accept/${friendUserId}`, {
      method: 'POST',
    });
  } catch (error) {
    console.error('친구 요청 수락 실패:', error);
    throw error;
  }
};

// 친구 요청 거절
export const rejectFriendRequest = async (friendUserId: number): Promise<void> => {
  try {
    await apiRequest<void>(`/friends/reject/${friendUserId}`, {
      method: 'POST',
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
