<template>
  <div
    ref="itemElement"
    class="absolute cursor-move select-none"
    :style="itemStyle"
    @mousedown="handleMouseDown"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove"
    @touchend="handleTouchEnd"
  >
    <img
      :src="item.imageUrl"
      :alt="item.name"
      class="w-full h-full object-contain pointer-events-none"
      :style="{ filter: isDragging ? 'brightness(1.1)' : 'none' }"
    />
    
    <!-- 선택된 아이템 표시 -->
    <div 
      v-if="isSelected"
      class="absolute inset-0 border-2 border-blue-400 rounded-lg bg-blue-50 bg-opacity-20"
    >
      <!-- 크기 조절 핸들 (웹용) -->
      <div 
        v-if="!isMobile"
        class="absolute -bottom-2 -right-2 w-4 h-4 bg-blue-500 rounded-full cursor-se-resize border-2 border-white"
        @mousedown.stop="handleResizeStart"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue';
import type { Item } from '../types/api';

interface Props {
  item: Item;
  position: { x: number; y: number };
  scale: number;
  isSelected: boolean;
  containerBounds: DOMRect | null;
}

interface Emits {
  (e: 'update:position', position: { x: number; y: number }): void;
  (e: 'update:scale', scale: number): void;
  (e: 'select'): void;
  (e: 'deselect'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const itemElement = ref<HTMLElement>();
const isDragging = ref(false);
const isResizing = ref(false);
const isMobile = ref(/Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent));

// 터치 관련 상태
const touchStartPos = ref<{ x: number; y: number } | null>(null);
const lastTouchDistance = ref<number | null>(null);
const touchCenter = ref<{ x: number; y: number } | null>(null);
const isMultiTouch = ref(false);
const touchStartTime = ref<number>(0);
const minimumMovement = 5; // 최소 이동 거리 (픽셀)

// 마우스 드래그 관련 상태
const dragStartPos = ref<{ x: number; y: number } | null>(null);
const dragStartPosition = ref<{ x: number; y: number } | null>(null);

// 리사이즈 관련 상태
const resizeStartPos = ref<{ x: number; y: number } | null>(null);
const resizeStartScale = ref<number>(1);

// 아이템 스타일 계산
const itemStyle = computed(() => {
  const baseSize = 120; // 기본 크기
  const size = baseSize * props.scale;
  
  return {
    left: `${props.position.x}px`,
    top: `${props.position.y}px`,
    width: `${size}px`,
    height: `${size}px`,
    zIndex: props.isSelected ? 1000 : getZIndex(props.item.type),
    transform: isDragging.value ? 'scale(1.05)' : 'scale(1)',
    transition: isDragging.value ? 'none' : 'transform 0.2s ease',
  };
});

// 아이템 타입별 Z-인덱스
function getZIndex(type: string): number {
  const zIndexMap: Record<string, number> = {
    background: 1,
    clothing: 5,
    head: 10,
    accessory: 15,
  };
  return zIndexMap[type] || 5;
}

// 마우스 드래그 시작
function handleMouseDown(e: MouseEvent) {
  if (isMobile.value) return;
  
  e.preventDefault();
  e.stopPropagation();
  
  emit('select');
  isDragging.value = true;
  
  dragStartPos.value = { x: e.clientX, y: e.clientY };
  dragStartPosition.value = { ...props.position };
  
  document.addEventListener('mousemove', handleMouseMove);
  document.addEventListener('mouseup', handleMouseUp);
}

// 마우스 드래그 중
function handleMouseMove(e: MouseEvent) {
  if (!isDragging.value || !dragStartPos.value || !dragStartPosition.value) return;
  
  const deltaX = e.clientX - dragStartPos.value.x;
  const deltaY = e.clientY - dragStartPos.value.y;
  
  const newPosition = {
    x: dragStartPosition.value.x + deltaX,
    y: dragStartPosition.value.y + deltaY,
  };
  
  // 경계 체크
  const boundedPosition = constrainPosition(newPosition);
  emit('update:position', boundedPosition);
}

// 마우스 드래그 종료
function handleMouseUp() {
  isDragging.value = false;
  dragStartPos.value = null;
  dragStartPosition.value = null;
  
  document.removeEventListener('mousemove', handleMouseMove);
  document.removeEventListener('mouseup', handleMouseUp);
}

// 터치 시작
function handleTouchStart(e: TouchEvent) {
  e.preventDefault();
  e.stopPropagation();
  
  touchStartTime.value = Date.now();
  
  if (e.touches.length === 1) {
    // 단일 터치 - 드래그 준비
    const touch = e.touches[0];
    touchStartPos.value = { x: touch.clientX, y: touch.clientY };
    dragStartPosition.value = { ...props.position };
    isMultiTouch.value = false;
    
    // 즉시 드래그 모드로 들어가지 않고 최소 이동 거리 체크 후 결정
    isDragging.value = false;
    
    emit('select');
  } else if (e.touches.length === 2) {
    // 두 손가락 터치 - 핀치 시작
    isDragging.value = false;
    isMultiTouch.value = true;
    setupPinchGesture(e);
    
    // 아이템이 선택되지 않은 경우에만 선택
    if (!props.isSelected) {
      emit('select');
    }
  }
}

// 터치 이동
function handleTouchMove(e: TouchEvent) {
  e.preventDefault();
  
  if (e.touches.length === 1 && !isMultiTouch.value && touchStartPos.value && dragStartPosition.value) {
    // 단일 터치 처리
    const touch = e.touches[0];
    const deltaX = touch.clientX - touchStartPos.value.x;
    const deltaY = touch.clientY - touchStartPos.value.y;
    const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    
    // 최소 이동 거리를 넘었을 때만 드래그 모드 활성화
    if (!isDragging.value && distance > minimumMovement) {
      isDragging.value = true;
    }
    
    // 드래그 모드일 때만 위치 업데이트
    if (isDragging.value) {
      const newPosition = {
        x: dragStartPosition.value.x + deltaX,
        y: dragStartPosition.value.y + deltaY,
      };
      
      const boundedPosition = constrainPosition(newPosition);
      emit('update:position', boundedPosition);
    }
  } else if (e.touches.length === 2 && isMultiTouch.value) {
    // 핀치 제스처
    handlePinchGesture(e);
  }
}

// 터치 종료
function handleTouchEnd(e: TouchEvent) {
  const touchDuration = Date.now() - touchStartTime.value;
  
  if (e.touches.length === 0) {
    // 모든 터치 종료
    isDragging.value = false;
    isMultiTouch.value = false;
    touchStartPos.value = null;
    dragStartPosition.value = null;
    lastTouchDistance.value = null;
    touchCenter.value = null;
    
    // 짧은 터치(탭)는 선택으로 처리
    if (touchDuration < 200 && !isDragging.value) {
      emit('select');
    }
  } else if (e.touches.length === 1 && isMultiTouch.value) {
    // 핀치에서 단일 터치로 변경
    lastTouchDistance.value = null;
    touchCenter.value = null;
    isMultiTouch.value = false;
    
    // 새로운 단일 터치 드래그 준비
    const touch = e.touches[0];
    touchStartPos.value = { x: touch.clientX, y: touch.clientY };
    dragStartPosition.value = { ...props.position };
    isDragging.value = false; // 최소 이동 거리 체크를 위해 false로 시작
    touchStartTime.value = Date.now();
  }
}

// 핀치 제스처 설정
function setupPinchGesture(e: TouchEvent) {
  const touch1 = e.touches[0];
  const touch2 = e.touches[1];
  
  lastTouchDistance.value = getDistance(touch1, touch2);
  touchCenter.value = {
    x: (touch1.clientX + touch2.clientX) / 2,
    y: (touch1.clientY + touch2.clientY) / 2,
  };
}

// 핀치 제스처 처리
function handlePinchGesture(e: TouchEvent) {
  if (e.touches.length !== 2 || !lastTouchDistance.value) return;
  
  const touch1 = e.touches[0];
  const touch2 = e.touches[1];
  const currentDistance = getDistance(touch1, touch2);
  
  // 스케일 계산 (더 부드러운 변화를 위해 조정)
  const scaleChange = currentDistance / lastTouchDistance.value;
  
  // 급격한 변화 방지
  const dampedScaleChange = 1 + (scaleChange - 1) * 0.5;
  const newScale = Math.max(0.3, Math.min(4, props.scale * dampedScaleChange));
  
  // 최소 변화량 체크 (불필요한 업데이트 방지)
  const scaleThreshold = 0.02;
  if (Math.abs(newScale - props.scale) > scaleThreshold) {
    emit('update:scale', newScale);
  }
  
  lastTouchDistance.value = currentDistance;
}

// 두 터치 포인트 간 거리 계산
function getDistance(touch1: Touch, touch2: Touch): number {
  const dx = touch1.clientX - touch2.clientX;
  const dy = touch1.clientY - touch2.clientY;
  return Math.sqrt(dx * dx + dy * dy);
}

// 리사이즈 시작 (웹용)
function handleResizeStart(e: MouseEvent) {
  e.preventDefault();
  e.stopPropagation();
  
  isResizing.value = true;
  resizeStartPos.value = { x: e.clientX, y: e.clientY };
  resizeStartScale.value = props.scale;
  
  document.addEventListener('mousemove', handleResizeMove);
  document.addEventListener('mouseup', handleResizeEnd);
}

// 리사이즈 중 (웹용)
function handleResizeMove(e: MouseEvent) {
  if (!isResizing.value || !resizeStartPos.value) return;
  
  const deltaX = e.clientX - resizeStartPos.value.x;
  const deltaY = e.clientY - resizeStartPos.value.y;
  const avgDelta = (deltaX + deltaY) / 2;
  
  const scaleChange = avgDelta / 100; // 100px 이동당 1배율 변경
  const newScale = Math.max(0.5, Math.min(3, resizeStartScale.value + scaleChange));
  
  emit('update:scale', newScale);
}

// 리사이즈 종료 (웹용)
function handleResizeEnd() {
  isResizing.value = false;
  resizeStartPos.value = null;
  
  document.removeEventListener('mousemove', handleResizeMove);
  document.removeEventListener('mouseup', handleResizeEnd);
}

// 위치 제한 (경계 내에 유지)
function constrainPosition(position: { x: number; y: number }) {
  if (!props.containerBounds) return position;
  
  const itemSize = 120 * props.scale;
  const maxX = props.containerBounds.width - itemSize;
  const maxY = props.containerBounds.height - itemSize;
  
  return {
    x: Math.max(0, Math.min(maxX, position.x)),
    y: Math.max(0, Math.min(maxY, position.y)),
  };
}
</script>
