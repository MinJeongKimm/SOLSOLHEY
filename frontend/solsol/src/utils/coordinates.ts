/**
 * 상대적 좌표계 유틸리티
 * 화면 크기에 관계없이 일관된 아이템 위치를 보장하기 위한 좌표 변환 함수들
 */

// 상대적 위치 인터페이스 (0~1 범위의 비율)
export interface RelativePosition {
  x: number; // 0 = 왼쪽, 1 = 오른쪽
  y: number; // 0 = 위쪽, 1 = 아래쪽
}

// 절대적 위치 인터페이스 (픽셀 단위)
export interface AbsolutePosition {
  x: number; // 픽셀
  y: number; // 픽셀
}

// 컨테이너 크기 인터페이스
export interface ContainerSize {
  width: number;
  height: number;
}

/**
 * 절대 좌표를 상대 좌표로 변환
 * @param absolute 절대 위치 (픽셀)
 * @param containerSize 컨테이너 크기 (픽셀)
 * @returns 상대 위치 (0~1 비율)
 */
export function toRelativePosition(
  absolute: AbsolutePosition, 
  containerSize: ContainerSize
): RelativePosition {
  // 경계값 처리 - 컨테이너 크기가 0이면 기본값 반환
  if (containerSize.width <= 0 || containerSize.height <= 0) {
    console.warn('Container size is invalid:', containerSize);
    return { x: 0.5, y: 0.5 }; // 중앙 위치 기본값
  }

  const relative = {
    x: absolute.x / containerSize.width,
    y: absolute.y / containerSize.height,
  };

  // 0~1 범위로 클램핑
  return {
    x: Math.max(0, Math.min(1, relative.x)),
    y: Math.max(0, Math.min(1, relative.y)),
  };
}

/**
 * 상대 좌표를 절대 좌표로 변환
 * @param relative 상대 위치 (0~1 비율)
 * @param containerSize 컨테이너 크기 (픽셀)
 * @returns 절대 위치 (픽셀)
 */
export function toAbsolutePosition(
  relative: RelativePosition, 
  containerSize: ContainerSize
): AbsolutePosition {
  // 경계값 처리
  if (containerSize.width <= 0 || containerSize.height <= 0) {
    console.warn('Container size is invalid:', containerSize);
    return { x: 0, y: 0 }; // 기본값
  }

  return {
    x: relative.x * containerSize.width,
    y: relative.y * containerSize.height,
  };
}

/**
 * 절대 좌표가 상대 좌표로 변환 가능한지 확인
 * @param position 위치 객체
 * @returns 절대 좌표 여부
 */
export function isAbsolutePosition(position: any): position is AbsolutePosition {
  return (
    typeof position === 'object' &&
    position !== null &&
    typeof position.x === 'number' &&
    typeof position.y === 'number' &&
    // 절대 좌표는 일반적으로 1보다 큰 값을 가짐
    (position.x > 1 || position.y > 1)
  );
}

/**
 * 상대 좌표인지 확인
 * @param position 위치 객체
 * @returns 상대 좌표 여부
 */
export function isRelativePosition(position: any): position is RelativePosition {
  return (
    typeof position === 'object' &&
    position !== null &&
    typeof position.x === 'number' &&
    typeof position.y === 'number' &&
    position.x >= 0 && position.x <= 1 &&
    position.y >= 0 && position.y <= 1
  );
}

/**
 * 마스코트 캔버스 컨테이너의 크기를 가져옴
 * @param element 마스코트 캔버스 엘리먼트
 * @returns 컨테이너 크기
 */
export function getContainerSize(element: HTMLElement | null): ContainerSize {
  if (!element) {
    console.warn('Container element is null');
    return { width: 400, height: 300 }; // 기본값
  }

  const rect = element.getBoundingClientRect();
  return {
    width: rect.width,
    height: rect.height,
  };
}

/**
 * 아이템의 기본 상대 위치를 타입별로 반환
 * @param itemType 아이템 타입
 * @returns 기본 상대 위치
 */
export function getDefaultRelativePosition(itemType: string): RelativePosition {
  const defaultPositions: Record<string, RelativePosition> = {
    head: { x: 0.5, y: 0.25 },        // 상단 중앙
    clothing: { x: 0.5, y: 0.55 },   // 중앙
    accessory: { x: 0.7, y: 0.3 },   // 우측 상단
    background: { x: 0.5, y: 0.5 },  // 정중앙
  };
  
  return defaultPositions[itemType] || { x: 0.5, y: 0.5 };
}

/**
 * 드래그 제약을 위한 상대 좌표 범위 확인
 * @param relative 상대 위치
 * @param itemSize 아이템 크기 (픽셀)
 * @param containerSize 컨테이너 크기
 * @returns 제약된 상대 위치
 */
export function constrainRelativePosition(
  relative: RelativePosition,
  itemSize: { width: number; height: number },
  containerSize: ContainerSize
): RelativePosition {
  // 아이템이 컨테이너를 벗어나지 않도록 제약
  const halfWidthRatio = (itemSize.width / 2) / containerSize.width;
  const halfHeightRatio = (itemSize.height / 2) / containerSize.height;
  
  return {
    x: Math.max(halfWidthRatio, Math.min(1 - halfWidthRatio, relative.x)),
    y: Math.max(halfHeightRatio, Math.min(1 - halfHeightRatio, relative.y)),
  };
}

// ===========================================
// 마스코트 기반 좌표 변환 함수들
// ===========================================

/**
 * 마스코트 기준 상대 좌표(0~1)를 절대 픽셀 좌표로 변환합니다.
 * 이 함수는 마스코트의 실제 DOM bounding box를 기준으로 계산합니다.
 * @param relativePos 마스코트 기준 상대 좌표 (0~1)
 * @param mascotRect 마스코트의 실제 DOM bounding box
 * @returns 절대 픽셀 좌표
 */
export function toAbsoluteFromMascot(relativePos: RelativePosition, mascotRect: DOMRect): AbsolutePosition {
  if (!mascotRect) {
    console.warn('Mascot rect is null, cannot convert to absolute position from mascot.');
    return { x: 0, y: 0 };
  }
  
  return {
    x: mascotRect.left + (relativePos.x * mascotRect.width),
    y: mascotRect.top + (relativePos.y * mascotRect.height),
  };
}

/**
 * 절대 픽셀 좌표를 마스코트 기준 상대 좌표(0~1)로 변환합니다.
 * 이 함수는 마스코트의 실제 DOM bounding box를 기준으로 계산합니다.
 * @param absolutePos 절대 픽셀 좌표
 * @param mascotRect 마스코트의 실제 DOM bounding box
 * @returns 마스코트 기준 상대 좌표 (0~1)
 */
export function toRelativeToMascot(absolutePos: AbsolutePosition, mascotRect: DOMRect): RelativePosition {
  if (!mascotRect || mascotRect.width === 0 || mascotRect.height === 0) {
    console.warn('Mascot rect is invalid, cannot convert to relative position to mascot.');
    return { x: 0, y: 0 };
  }
  
  return {
    x: (absolutePos.x - mascotRect.left) / mascotRect.width,
    y: (absolutePos.y - mascotRect.top) / mascotRect.height,
  };
}

/**
 * 마스코트 기준 기본 상대 위치를 반환합니다.
 * 기존 getDefaultRelativePosition과 달리 마스코트를 기준으로 한 위치입니다.
 * @param itemType 아이템 타입
 * @returns 마스코트 기준 기본 상대 위치 (0~1)
 */
export function getDefaultMascotRelativePosition(itemType: string): RelativePosition {
  // 마스코트 자체를 기준으로 한 상대 위치 (마스코트의 어느 부분에 아이템이 위치할지)
  const defaultMascotPositions: Record<string, RelativePosition> = {
    head: { x: 0.5, y: 0.2 },        // 마스코트 머리 부분 (상단 중앙)
    clothing: { x: 0.5, y: 0.6 },    // 마스코트 몸통 부분 (중앙)
    accessory: { x: 0.7, y: 0.3 },   // 마스코트 얼굴 옆 (액세서리)
    background: { x: 0.5, y: 0.5 },  // 마스코트 중앙 (배경)
  };
  
  return defaultMascotPositions[itemType] || { x: 0.5, y: 0.5 };
}

/**
 * 마스코트 기준으로 드래그 제약을 적용합니다.
 * 아이템이 마스코트 영역을 벗어나지 않도록 제한합니다.
 * @param relative 마스코트 기준 상대 위치
 * @param itemSize 아이템 크기 (픽셀)
 * @param mascotRect 마스코트의 bounding box
 * @returns 제약된 마스코트 기준 상대 위치
 */
export function constrainMascotRelativePosition(
  relative: RelativePosition,
  itemSize: { width: number; height: number },
  mascotRect: DOMRect
): RelativePosition {
  if (!mascotRect || mascotRect.width === 0 || mascotRect.height === 0) {
    return relative;
  }
  
  // 아이템이 마스코트 영역을 벗어나지 않도록 제약
  const halfWidthRatio = (itemSize.width / 2) / mascotRect.width;
  const halfHeightRatio = (itemSize.height / 2) / mascotRect.height;
  
  return {
    x: Math.max(halfWidthRatio, Math.min(1 - halfWidthRatio, relative.x)),
    y: Math.max(halfHeightRatio, Math.min(1 - halfHeightRatio, relative.y)),
  };
}

/**
 * 느슨한 제약: 자유도를 높이기 위해 완전 가시 대신 부분 가시/가장자리 밖 일부 허용
 * - scaleDown: 반지름 비율을 축소(0..1). 1이면 엄격, 0.6이면 40% 더 자유
 * - outside  : 가장자리 밖으로 허용할 비율(0..0.5 권장)
 */
export function constrainMascotRelativePositionLoose(
  relative: RelativePosition,
  itemSize: { width: number; height: number },
  mascotRect: DOMRect,
  opts?: { scaleDown?: number; outside?: number }
): RelativePosition {
  if (!mascotRect || mascotRect.width === 0 || mascotRect.height === 0) return relative;

  const scaleDown = opts?.scaleDown ?? 0.6;
  const outside = opts?.outside ?? 0.1;

  const halfWR = (itemSize.width / 2) / mascotRect.width;
  const halfHR = (itemSize.height / 2) / mascotRect.height;

  const minX = Math.max(-outside, halfWR * scaleDown);
  const maxX = Math.min(1 + outside, 1 - halfWR * scaleDown);
  const minY = Math.max(-outside, halfHR * scaleDown);
  const maxY = Math.min(1 + outside, 1 - halfHR * scaleDown);

  return {
    x: Math.max(minX, Math.min(maxX, relative.x)),
    y: Math.max(minY, Math.min(maxY, relative.y)),
  };
}
