/**
 * JWT 토큰 관련 유틸리티 함수들
 */

/**
 * JWT 토큰을 디코드하여 payload를 반환
 */
export function decodeJwtToken(token: string): any {
  try {
    // JWT 토큰은 3부분으로 구성: header.payload.signature
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('JWT 토큰 디코드 실패:', error);
    return null;
  }
}

/**
 * JWT 토큰에서 사용자 ID를 추출
 */
export function getUserIdFromToken(token: string): number | null {
  try {
    const payload = decodeJwtToken(token);
    if (!payload || !payload.userId) {
      return null;
    }
    
    // userId를 숫자로 변환
    const userId = Number(payload.userId);
    return isNaN(userId) ? null : userId;
  } catch (error) {
    console.error('토큰에서 사용자 ID 추출 실패:', error);
    return null;
  }
}

/**
 * JWT 토큰에서 사용자명을 추출
 */
export function getUsernameFromToken(token: string): string | null {
  try {
    const payload = decodeJwtToken(token);
    return payload?.username || null;
  } catch (error) {
    console.error('토큰에서 사용자명 추출 실패:', error);
    return null;
  }
}

/**
 * JWT 토큰의 만료 시간을 확인
 */
export function isTokenExpired(token: string): boolean {
  try {
    const payload = decodeJwtToken(token);
    if (!payload || !payload.exp) {
      return true;
    }
    
    const currentTime = Math.floor(Date.now() / 1000);
    return payload.exp < currentTime;
  } catch (error) {
    console.error('토큰 만료 시간 확인 실패:', error);
    return true;
  }
}

/**
 * JWT 토큰의 유효성을 검증
 */
export function isTokenValid(token: string): boolean {
  if (!token) return false;
  
  try {
    const payload = decodeJwtToken(token);
    if (!payload) return false;
    
    // 필수 필드 확인
    if (!payload.userId || !payload.username) return false;
    
    // 만료 시간 확인
    if (isTokenExpired(token)) return false;
    
    return true;
  } catch (error) {
    return false;
  }
}

/**
 * 현재 저장된 토큰에서 사용자 ID를 가져오기
 */
export function getCurrentUserId(): number | null {
  try {
    const token = localStorage.getItem('token');
    if (!token) return null;
    
    return getUserIdFromToken(token);
  } catch (error) {
    console.error('현재 사용자 ID 가져오기 실패:', error);
    return null;
  }
}

/**
 * 현재 저장된 토큰에서 사용자명을 가져오기
 */
export function getCurrentUsername(): string | null {
  try {
    const token = localStorage.getItem('token');
    if (!token) return null;
    
    return getUsernameFromToken(token);
  } catch (error) {
    console.error('현재 사용자명 가져오기 실패:', error);
    return null;
  }
}
