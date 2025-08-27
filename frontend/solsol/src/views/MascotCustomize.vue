<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <!-- 커스터마이즈 카드 -->
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <!-- 상단 헤더 -->
      <div class="flex justify-between items-center mb-6">
        <!-- 좌측: 뒤로가기 + 제목 -->
        <div class="flex items-center space-x-3">
          <button 
            @click="goBack"
            class="w-8 h-8 flex items-center justify-center hover:bg-gray-100 rounded-full transition-colors"
          >
            <img src="/icons/icon_back.png" alt="뒤로가기" class="w-5 h-5" />
          </button>
          <h1 class="text-xl font-bold text-gray-800">Customize</h1>
        </div>
        
        <!-- 우측: 포인트 -->
        <div class="flex items-center space-x-2">
          <img src="/icons/icon_point.png" alt="포인트" class="w-5 h-5" />
          <span class="font-bold text-orange-600">{{ userCoins }}P</span>
        </div>
      </div>

      <!-- 마스코트 미리보기 영역 -->
      <div class="bg-gradient-to-br from-blue-50 to-purple-50 rounded-2xl mb-6 w-full">
        <!-- 배경 커스터마이징 UI -->
        <div v-if="showBgPanel" class="mb-4 bg-white bg-opacity-80 rounded-lg p-3 flex items-center gap-4">
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-700">배경색</span>
            <input type="color" v-model="bgColor" class="w-8 h-8 rounded cursor-pointer border" />
          </div>
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-700">패턴</span>
            <button 
              class="px-2 py-1 rounded text-xs border"
              :class="bgPattern === 'none' ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-gray-700'"
              @click="bgPattern = 'none'">없음</button>
            <button 
              class="px-2 py-1 rounded text-xs border"
              :class="bgPattern === 'dots' ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-gray-700'"
              @click="bgPattern = 'dots'">도트</button>
            <button 
              class="px-2 py-1 rounded text-xs border"
              :class="bgPattern === 'stripes' ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-gray-700'"
              @click="bgPattern = 'stripes'">스트라이프</button>
          </div>
          <!-- 저장 버튼 제거: 메인 저장에 통합됨 -->
        </div>
        <!-- 모바일 도움말 제거 -->
        
        <div 
          class="relative w-full h-80 rounded-xl overflow-hidden flex items-center justify-center"
          :style="previewBackgroundStyle"
        >
          <!-- 배경 커스터마이징 토글 아이콘 (좌하단) -->
          <button
            class="absolute bottom-2 left-2 z-50 w-9 h-9 rounded-full shadow bg-white/90 hover:bg-white flex items-center justify-center border border-gray-200 pointer-events-auto"
            title="배경 커스터마이징"
            @click.stop="toggleBgPanel"
          >
            <!-- 팔레트 아이콘 -->
            <span class="text-lg">🎨</span>
          </button>
          <!-- 커스터마이즈 배경 -->
          <!--08.27 임시 삭제 코드 by 민정-->
          <!-- <img 
            src="/backgrounds/base/bg_blue.png" 
            alt="꾸미기 배경" 
            class="absolute inset-0 w-full h-full object-cover"
          />
           -->
          <!-- 마스코트 + 장착된 아이템들 -->
          <div 
            ref="mascotCanvas"
            class="mascot-canvas absolute inset-0 flex items-center justify-center"
            @click="handleCanvasClick"
          >
            <!-- 중앙 고정 마스코트 이미지 -->
            <div 
              ref="mascotRef"
              class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-32 h-32 z-10"
            >
              <img 
                :src="currentMascot ? getMascotImageUrl(currentMascot.type) : '/mascot/soll.png'" 
                :alt="currentMascot?.name || '마스코트'" 
                class="w-full h-full object-contain drop-shadow-lg"
                @load="handleMascotImageLoad"
                @error="handleMascotImageError"
              />
            </div>
            
            <!-- 레이어 1: 배경 아이템 (마스코트 뒤, 캔버스 전체 채움) -->
            <div class="absolute inset-0 z-0 overflow-hidden">
              <img
                v-for="bg in backgroundEquippedItems"
                :key="bg.id"
                :src="bg.item.imageUrl"
                :alt="bg.item.name"
                class="absolute inset-0 w-full h-full object-cover pointer-events-none"
              />
            </div>

            <!-- 레이어 2: 마스코트 (중간) -->
            <!-- 이미 위에서 마스코트 이미지를 렌더링 중이므로 이 블록은 유지 -->

            <!-- 레이어 3: 전경 아이템 (마스코트 앞) -->
            <div class="absolute inset-0 z-20" ref="foregroundLayer">
              <DraggableItem
                v-for="fg in foregroundEquippedItems"
                :key="fg.id"
                :item="fg.item"
                :position="getAbsolutePosition(fg)"
                :scale="fg.scale"
                :rotation="fg.rotation"
                :is-selected="selectedItemId === fg.id"
                :container-bounds="foregroundBounds || canvasBounds"
                @update:position="updateItemPosition(fg.id, $event)"
                @update:scale="updateItemScale(fg.id, $event)"
                @update:rotation="updateItemRotation(fg.id, $event)"
                @select="selectItem(fg.id)"
              />
            </div>
          </div>
          
          <!-- 마스코트 이름 -->
          <div class="absolute bottom-4 left-1/2 transform -translate-x-1/2">
            <div class="bg-white bg-opacity-90 px-3 py-1 rounded-full">
              <span class="text-sm font-medium text-gray-800">{{ currentMascot?.name || '쏠' }}</span>
            </div>
          </div>
          
          <!-- 선택된 아이템 정보 (모바일) -->
          <div 
            v-if="isMobileDevice && selectedItemInfo"
            class="absolute top-2 right-2 bg-white bg-opacity-95 p-2 rounded-lg shadow-lg text-xs max-w-32"
          >
            <div class="font-medium text-gray-800 mb-1">{{ selectedItemInfo.name }}</div>
            <div class="text-gray-600 space-y-1">
              <div>위치: {{ Math.round(selectedItemInfo.relativePosition.x * 100) }}%, {{ Math.round(selectedItemInfo.relativePosition.y * 100) }}%</div>
              <div>크기: {{ Math.round(selectedItemInfo.scale * 100) }}%</div>
              <div>회전: {{ Math.round(selectedItemInfo.rotation) }}°</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 마스코트 조작 패널 제거됨 (마스코트는 중앙에 고정) -->
      
      
      
      <!-- 카테고리 탭들 -->
      <div class="mb-6">
        <div class="flex space-x-2 overflow-x-auto">
          <button 
            v-for="category in itemCategories" 
            :key="category.id"
            @click="selectedCategory = category.id as 'head' | 'clothing' | 'accessory' | 'background'"
            :class="[
              'flex-shrink-0 flex flex-col items-center p-3 rounded-xl transition-all min-w-[80px]',
              selectedCategory === category.id 
                ? 'bg-purple-500 text-white shadow-lg' 
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            <div class="w-10 h-10 rounded-full flex items-center justify-center mb-2" 
                 :class="selectedCategory === category.id ? 'bg-white bg-opacity-20' : 'bg-white'">
              <span class="text-xl">{{ category.icon }}</span>
            </div>
            <span class="text-xs font-medium">{{ category.name }}</span>
          </button>
        </div>
      </div>
      
      <!-- 아이템 목록 -->
      <div class="space-y-4">
        <h3 class="text-lg font-bold text-gray-800">{{ getCategoryName(selectedCategory) }}</h3>
        
        <div class="grid grid-cols-2 gap-4">
          <div 
            v-for="item in filteredItems" 
            :key="item.id"
            :class="[
              'relative border-2 rounded-xl p-4 transition-all',
              isEquipped(item) 
                ? 'border-purple-500 bg-purple-50' 
                : 'border-gray-200 hover:border-gray-300',
              canEquipMoreItems || isEquipped(item)
                ? 'cursor-pointer hover:shadow-md'
                : 'cursor-not-allowed opacity-60'
            ]"
            @click="handleItemClick(item)"
          >
            <!-- 아이템 이미지 -->
            <div class="w-full h-20 bg-gray-100 rounded-lg mb-3 flex items-center justify-center overflow-hidden">
              <img 
                :src="item.imageUrl" 
                :alt="item.name"
                class="w-full h-full object-contain"
                @error="handleImageError"
              />
            </div>
            
            <!-- 아이템 정보 -->
            <div>
              <h4 class="font-medium text-sm text-gray-800 mb-1 flex items-center">
                {{ item.name }}
                <span v-if="isEquipped(item)" class="text-purple-600 text-xs ml-1">
                  ✓{{ getEquippedCount(item) > 1 ? ` (${getEquippedCount(item)})` : '' }}
                </span>
              </h4>
              <p class="text-xs text-gray-600 mb-2 line-clamp-2">{{ item.description }}</p>
              
              <!-- 착용 상태 표시 -->
              <div class="text-center">
                <span 
                  :class="[
                    'text-xs font-medium px-3 py-1 rounded-full',
                    isEquipped(item) 
                      ? 'bg-purple-500 text-white' 
                      : canEquipMoreItems 
                        ? 'bg-gray-200 text-gray-600' 
                        : 'bg-red-100 text-red-600'
                  ]"
                >
                  {{ isEquipped(item) 
                    ? `착용중 (${getEquippedCount(item)})` 
                    : canEquipMoreItems
                      ? '착용하기' 
                      : '장착 불가' }}
                </span>
              </div>
            </div>

            <!-- 장착 아이콘 -->
            <div 
              v-if="isEquipped(item)"
              class="absolute top-2 right-2 w-6 h-6 bg-purple-500 rounded-full flex items-center justify-center"
            >
              <span class="text-white text-xs">✓</span>
            </div>
          </div>
        </div>
        
        <!-- 아이템이 없는 경우 -->
        <div v-if="filteredItems.length === 0" class="text-center py-8">
          <div class="text-4xl mb-2 opacity-50">📦</div>
          <p class="text-gray-500">해당 카테고리에 아이템이 없습니다.</p>
        </div>
      </div>
      
      <!-- 장착된 아이템 목록 -->
      <div v-if="equippedItemsList.length > 0" class="mb-6">
        <h3 :class="[
          'text-lg font-bold mb-3 flex items-center space-x-2',
          equippedItemsList.length >= maxEquippedItems ? 'text-red-600' : 'text-gray-800'
        ]">
          <span>장착된 아이템 ({{ equippedItemsList.length }}/{{ maxEquippedItems }})</span>
          <span v-if="equippedItemsList.length >= maxEquippedItems" class="text-red-500 text-sm">⚠️ 최대</span>
        </h3>
        
        <div class="space-y-2 max-h-32 overflow-y-auto">
          <div 
            v-for="equippedItem in equippedItemsList" 
            :key="equippedItem.id"
            :class="[
              'flex items-center justify-between p-2 rounded-lg border transition-all cursor-pointer',
              selectedItemId === equippedItem.id 
                ? 'border-blue-400 bg-blue-50' 
                : 'border-gray-200 bg-white hover:border-gray-300'
            ]"
            @click="selectItem(equippedItem.id)"
          >
            <div class="flex items-center space-x-2">
              <img 
                :src="equippedItem.item.imageUrl" 
                :alt="equippedItem.item.name"
                class="w-8 h-8 object-contain bg-gray-100 rounded"
                @error="handleImageError"
              />
              <div>
                <div class="text-sm font-medium text-gray-800">{{ equippedItem.item.name }}</div>
                <div class="text-xs text-gray-500">
                  {{ Math.round(equippedItem.scale * 100) }}% | {{ Math.round(equippedItem.rotation) }}°
                </div>
              </div>
            </div>
            
            <button 
              @click.stop="removeEquippedItem(equippedItem.id)"
              class="w-6 h-6 bg-red-100 hover:bg-red-200 text-red-600 rounded-full flex items-center justify-center text-xs transition-colors"
              title="아이템 제거"
            >
              ×
            </button>
          </div>
        </div>
      </div>
      
      <!-- 전체 조작 버튼들 -->
      <div class="flex space-x-3 mt-6">
        <button 
          @click="resetAllItems"
          class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 py-3 px-4 rounded-lg font-medium transition-colors flex items-center justify-center space-x-2"
        >
          <span>🔄</span>
          <span>전체 초기화</span>
        </button>
        <button 
          @click="saveItemPositions"
          class="flex-1 bg-purple-500 hover:bg-purple-600 text-white py-3 px-4 rounded-lg font-medium transition-colors flex items-center justify-center space-x-2"
        >
          <span>💾</span>
          <span>저장하기</span>
        </button>
      </div>
    </div>
    
    <!-- 알림 토스트 -->
    <div 
      v-if="showToast" 
      class="fixed bottom-4 left-1/2 transform -translate-x-1/2 bg-gray-900 text-white px-6 py-3 rounded-lg shadow-lg z-50 transition-all"
    >
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, onActivated, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getMascot, getMascotCustomization, getShopItems, handleApiError, saveMascotCustomization, updateMascotBackground } from '../api/index';
import DraggableItem from '../components/DraggableItem.vue';
import { mascotTypes } from '../data/mockData';
import type { Item, Mascot } from '../types/api';
import {
  constrainMascotRelativePositionLoose,
  getContainerSize,
  getDefaultMascotRelativePosition,
  toAbsoluteFromMascot,
  toRelativeToMascot,
  type RelativePosition
} from '../utils/coordinates';

// 아이템 상태 인터페이스 (다중 아이템 지원)
interface EquippedItemState {
  id: string; // 고유 ID (item.id + 장착 순서)
  item: Item;
  relativePosition: RelativePosition; // 상대 좌표 (0~1 비율)
  scale: number;
  rotation: number; // 회전 각도 (degrees)
  equippedAt: number; // 장착 시간 (타임스탬프)
}

// 마스코트 기반 좌표계 완성 - 모든 아이템이 마스코트를 기준으로 배치됨

const router = useRouter();

// 반응형 데이터
const currentMascot = ref<Mascot | null>(null);
const items = ref<any[]>([]);
const userCoins = ref(15000);
const selectedCategory = ref<'head' | 'clothing' | 'accessory' | 'background'>('head');

// 드래그 관련 상태
const mascotCanvas = ref<HTMLElement>();
const canvasBounds = ref<DOMRect | null>(null);
const foregroundLayer = ref<HTMLElement>();
const foregroundBounds = ref<DOMRect | null>(null);
const selectedItemId = ref<string | null>(null); // 고유 ID로 변경
const equippedItemStates = ref<Map<string, EquippedItemState>>(new Map()); // 다중 아이템 지원
const isMobileDevice = ref(/Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent));

// 다중 아이템 관리
const equippedItemsList = ref<EquippedItemState[]>([]); // 장착된 아이템 목록
const maxEquippedItems = 10; // 최대 장착 가능 아이템 수

// 마스코트는 중앙에 고정 (드래그 불가)
const mascotRef = ref<HTMLElement>();
const mascotRect = ref<DOMRect | null>(null);

// 토스트 알림
const showToast = ref(false);
const toastMessage = ref('');
const showBgPanel = ref(false);

// 배경 커스터마이징 상태
const bgColor = ref<string>('#ffffff');
const bgPattern = ref<'dots' | 'stripes' | 'none'>('none');

const previewBackgroundStyle = computed(() => {
  const style: Record<string, string> = {
    backgroundColor: bgColor.value || '#ffffff',
  };
  if (bgPattern.value === 'dots') {
    style.backgroundImage = 'radial-gradient(circle, rgba(0,0,0,0.12) 1px, transparent 1px)';
    style.backgroundSize = '12px 12px';
  } else if (bgPattern.value === 'stripes') {
    style.backgroundImage = 'repeating-linear-gradient(45deg, rgba(0,0,0,0.08) 0 10px, transparent 10px 20px)';
  } else {
    style.backgroundImage = 'none';
  }
  return style;
});

// 표준화 유틸: 서버에서 오는 타입/카테고리의 대소문자 혼동을 방지
function normalizeType(val: unknown): string {
  return (val ?? '').toString().toLowerCase();
}

async function saveBackground() {
  try {
    await updateMascotBackground({ backgroundColor: bgColor.value, patternType: bgPattern.value });
    showToastMessage('배경 설정이 저장되었습니다!');
  } catch (e) {
    const msg = handleApiError(e);
    showToastMessage(`배경 저장 실패: ${msg}`);
  }
}

function toggleBgPanel() {
  showBgPanel.value = !showBgPanel.value;
}

// 아이템 카테고리
const itemCategories = [
  { id: 'head', name: 'Head', icon: '👕' },
  { id: 'clothing', name: 'Clothing', icon: '👖' },
  { id: 'accessory', name: 'Accessory', icon: '👓' },
  { id: 'background', name: 'Background', icon: '🖼️' }
];

// 버튼 패널 제거: 드래그/핀치/회전 제스처만 제공

// 필터링된 아이템 목록 (보유한 아이템만)
// ShopController.getItemsWithOwnership → ItemResponse(category, owned) 기준으로 필터링
const filteredItems = computed(() => {
  const target = selectedCategory.value; // 'head' | 'clothing' | 'accessory' | 'background'
  return items.value.filter((item: any) => {
    const cat = normalizeType(item.category);
    const typ = normalizeType(item.type); // EQUIP/BACKGROUND 등
    const isOwned = item.owned === true || item.isOwned === true;
    if (!isOwned) return false;
    if (target === 'background') {
      // 일부 응답이 category 대신 type으로 BACKGROUND만 내려오는 경우도 허용
      return cat === 'background' || typ === 'background';
    }
    return cat === target;
  });
});

// 장착된 아이템들의 상태 목록 (다중 아이템 지원)
const equippedItems = computed(() => {
  // 새로운 다중 아이템 시스템에서는 equippedItemsList를 직접 사용
  return equippedItemsList.value;
});

// 배경 아이템/전경 아이템 분리 (레이어링 제어)
const backgroundEquippedItems = computed(() =>
  equippedItemsList.value.filter(e => normalizeType(e.item?.type) === 'background')
);
const foregroundEquippedItems = computed(() =>
  equippedItemsList.value.filter(e => normalizeType(e.item?.type) !== 'background')
);

// 더 많은 아이템을 장착할 수 있는지 확인
const canEquipMoreItems = computed(() => {
  return equippedItemsList.value.length < maxEquippedItems;
});

// 마스코트는 CSS로 중앙에 고정됨

// 기존 마스코트 데이터에서 아이템 로드 (호환성을 위함)
function loadEquippedItemsFromMascot() {
  if (!currentMascot.value?.equippedItem) return;
  
  console.log('마스코트에서 장착 아이템 로드:', currentMascot.value.equippedItem);
  
  // 기존 단일 아이템 시스템과의 호환성
  ['head', 'clothing', 'accessory', 'background'].forEach(type => {
    const item = items.value.find(item => 
      item.type === type && 
      currentMascot.value!.equippedItem!.includes(item.name)
    );
    
    if (item) {
      // 이미 장착된 아이템인지 확인
      const existingItem = equippedItemsList.value.find(equipped => 
        equipped.item.id === item.id
      );
      
      if (!existingItem) {
        // addEquippedItem 대신 직접 추가하여 무한 루프 방지
        const id = generateItemId(item);
        const newEquippedItem: EquippedItemState = {
          id,
          item,
          relativePosition: getDefaultPosition(item.type),
          scale: 1,
          rotation: 0,
          equippedAt: Date.now(),
        };
        
        equippedItemsList.value.push(newEquippedItem);
        equippedItemStates.value.set(id, newEquippedItem);
        
        console.log(`마스코트에서 아이템 로드: ${item.name}`);
      }
    }
  });
}

// 선택된 아이템의 정보
const selectedItemInfo = computed(() => {
  if (!selectedItemId.value) return null;
  
  const state = equippedItemStates.value.get(selectedItemId.value);
  if (!state) return null;
  
  return {
    name: state.item.name,
    relativePosition: state.relativePosition,
    scale: state.scale,
    rotation: state.rotation,
  };
});

// 사용자 보유 아이템 로드
async function loadUserItems() {
  try {
    const shopItems = await getShopItems();
    items.value = shopItems;
    console.log('사용자 보유 아이템 로드됨:', shopItems);
  } catch (error) {
    console.error('아이템 로드 실패:', error);
    showToastMessage('아이템 로드에 실패했습니다.');
  }
}

// 스냅샷 합성: 배경 → 마스코트 → 아이템(위치/스케일/회전)
async function composeSnapshotDataUrl(): Promise<string> {
  const DPR = Math.max(1, Math.min(3, Math.floor(window.devicePixelRatio || 1)));
  const canvasSize = 800;
  const canvas = document.createElement('canvas');
  canvas.width = canvasSize * DPR;
  canvas.height = canvasSize * DPR;
  const ctx = canvas.getContext('2d');
  if (!ctx) return '';
  ctx.scale(DPR, DPR);
  ctx.imageSmoothingEnabled = true;

  const loadImage = (src: string) => new Promise<HTMLImageElement>((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = reject;
    img.src = src;
  });

  // 배경
  const bgImg = await loadImage('/backgrounds/base/bg_blue.png');
  ctx.drawImage(bgImg, 0, 0, canvasSize, canvasSize);

  // 마스코트
  const mascotUrl = currentMascot.value ? getMascotImageUrl(currentMascot.value.type) : '/mascot/soll.png';
  const mascotImg = await loadImage(mascotUrl);
  const mascotBoxSize = Math.floor(canvasSize * 0.5); // 중앙 50%
  const mascotX = (canvasSize - mascotBoxSize) / 2;
  const mascotY = (canvasSize - mascotBoxSize) / 2;
  ctx.drawImage(mascotImg, mascotX, mascotY, mascotBoxSize, mascotBoxSize);

  // 아이템들
  const UI_MASCOT_PX = 128;
  const baseItemSize = (120 /* BASE_ITEM_SIZE */ / UI_MASCOT_PX) * mascotBoxSize;
  for (const e of equippedItemsList.value) {
    try {
      const img = await loadImage(e.item.imageUrl || '');
      const centerX = mascotX + (e.relativePosition.x * mascotBoxSize);
      const centerY = mascotY + (e.relativePosition.y * mascotBoxSize);
      const size = Math.max(12, baseItemSize * (e.scale ?? 1));
      const rot = (((e.rotation ?? 0) % 360) + 360) % 360;
      ctx.save();
      ctx.translate(centerX, centerY);
      ctx.rotate((rot * Math.PI) / 180);
      ctx.drawImage(img, -size / 2, -size / 2, size, size);
      ctx.restore();
    } catch {}
  }

  // Data URL 반환 (용량을 위해 기본 PNG)
  try {
    return canvas.toDataURL('image/png');
  } catch {
    return '';
  }
}

// 유틸리티 함수들
// 마스코트 이미지 URL 캐시 (리렌더링 최적화)
const mascotImageUrlCache = new Map<string, string>();

function getMascotImageUrl(type: string): string {
  // 캐시에서 확인
  if (mascotImageUrlCache.has(type)) {
    return mascotImageUrlCache.get(type)!;
  }
  
  console.log('🖼️ 마스코트 이미지 URL 계산:', { type });
  const typeObj = mascotTypes.find(t => t.id === type);
  const imageUrl = typeObj ? typeObj.imageUrl : '/mascot/soll.png';
  
  // 캐시에 저장
  mascotImageUrlCache.set(type, imageUrl);
  console.log('✅ 마스코트 이미지 URL 캐시됨:', imageUrl);
  
  return imageUrl;
}

function getCategoryName(category: 'head' | 'clothing' | 'accessory' | 'background'): string {
  const categoryMap: Record<string, string> = {
    head: '머리',
    clothing: '의상', 
    accessory: '액세서리',
    background: '배경'
  };
  return categoryMap[category] || category;
}

// 아이템 타입별 기본 상대 위치 설정
function getDefaultPosition(itemType: string): RelativePosition {
  return getDefaultMascotRelativePosition(itemType);
}

// 다중 아이템 관리 함수들
function generateItemId(item: Item): string {
  return `${item.id}_${Date.now()}`;
}

function addEquippedItem(item: Item): boolean {
  // 최대 개수 체크
  if (equippedItemsList.value.length >= maxEquippedItems) {
    showToastMessage(`최대 ${maxEquippedItems}개까지만 장착할 수 있습니다!`);
    return false;
  }
  
  const id = generateItemId(item);
  const newEquippedItem: EquippedItemState = {
    id,
    item,
    // 새 아이템은 마스코트 중앙에 자동 배치
    relativePosition: { x: 0.5, y: 0.5 },
    scale: 1,
    rotation: 0,
    equippedAt: Date.now(),
  };
  
  equippedItemsList.value.push(newEquippedItem);
  equippedItemStates.value.set(id, newEquippedItem);
  
  // currentMascot.equippedItem 필드 동기화
  updateMascotEquippedItems();
  
  showToastMessage(`${item.name}을(를) 장착했습니다!`);
  return true;
}

function removeEquippedItem(itemId: string): boolean {
  const index = equippedItemsList.value.findIndex(item => item.id === itemId);
  if (index === -1) return false;
  
  const removedItem = equippedItemsList.value[index];
  equippedItemsList.value.splice(index, 1);
  equippedItemStates.value.delete(itemId);
  
  // 선택된 아이템이 제거되면 선택 해제
  if (selectedItemId.value === itemId) {
    selectedItemId.value = null;
  }
  
  // currentMascot.equippedItem 필드 동기화
  updateMascotEquippedItems();
  
  showToastMessage(`${removedItem.item.name}을(를) 해제했습니다!`);
  return true;
}

function isItemEquipped(item: Item): boolean {
  return equippedItemsList.value.some(equipped => equipped.item.id === item.id);
}

function getEquippedCount(item: Item): number {
  return equippedItemsList.value.filter(equipped => equipped.item.id === item.id).length;
}

// currentMascot.equippedItem 필드를 현재 장착된 아이템들과 동기화
function updateMascotEquippedItems() {
  if (!currentMascot.value) return;
  
  // 장착된 아이템들의 이름을 수집 (중복 제거)
  const equippedItemNames = [...new Set(
    equippedItemsList.value.map(equipped => equipped.item.name)
  )];
  
  // 문자열로 연결 (기존 방식과 호환)
  currentMascot.value.equippedItem = equippedItemNames.join(',');
  
  console.log('마스코트 장착 아이템 동기화:', {
    equippedItems: equippedItemsList.value.map(e => e.item.name),
    equippedItemString: currentMascot.value.equippedItem
  });
}

// 안정적인 캔버스 bounding box 캐시
let stableCanvasRect: DOMRect | null = null;
let lastCanvasUpdateTime = 0;
// 레이아웃 스냅샷(같은 프레임의 canvas/mascot rect 동시 측정)
const layoutSnapshot = ref<{ canvasRect: DOMRect; mascotRect: DOMRect; ts: number } | null>(null);

function captureLayoutSnapshot(immediate = false) {
  const doCapture = () => {
    if (!mascotCanvas.value || !mascotRef.value) return;
    const c = mascotCanvas.value.getBoundingClientRect();
    const m = mascotRef.value.getBoundingClientRect();
    layoutSnapshot.value = { canvasRect: c, mascotRect: m, ts: performance.now() };
    stableCanvasRect = c;
    lastCanvasUpdateTime = Date.now();
  };
  if (immediate) doCapture(); else requestAnimationFrame(doCapture);
}

// 위치 계산 결과 캐시 (무한 루프 방지)
const positionCache = new Map<string, { x: number; y: number; timestamp: number }>();
const POSITION_CACHE_DURATION = 250; // 250ms 캐시로 미세한 레이아웃 변동 흡수

// 저장 중 렌더 안정화를 위한 최종 렌더 좌표 캐시(아이템별)
const lastRenderedPosition = new Map<string, { x: number; y: number }>();
const isSaving = ref(false);
const BASE_ITEM_SIZE = 120; // DraggableItem의 기본 사이즈와 일치시킴

// 마스코트 기준 상대 좌표를 절대 좌표로 변환하여 반환
function getAbsolutePosition(equippedItem: EquippedItemState): { x: number; y: number } {
  if (!mascotRect.value || !mascotCanvas.value || mascotRect.value.width <= 0 || mascotRect.value.height <= 0) {
    // 저장 중에는 마지막 렌더 좌표를 그대로 사용하여 튐 방지
    if (isSaving.value) {
      const last = lastRenderedPosition.get(equippedItem.id);
      if (last) return last;
    }
    // 안전한 폴백: 캔버스 중심에 아이템(스케일 반영) 배치
    const canvasRect = mascotCanvas.value?.getBoundingClientRect();
    if (canvasRect) {
      const itemSize = BASE_ITEM_SIZE * (equippedItem.scale || 1);
      const x = (canvasRect.width - itemSize) / 2;
      const y = (canvasRect.height - itemSize) / 2;
      return { x, y };
    }
    console.warn('⚠️ 마스코트/캔버스 좌표 미가용: 좌상단(0,0) 폴백');
    return { x: 0, y: 0 };
  }
  
  // 위치 계산 결과 캐시 확인 (무한 루프 방지)
  const cacheKey = `${equippedItem.id}_${equippedItem.relativePosition.x}_${equippedItem.relativePosition.y}_${equippedItem.scale}`;
  const now = Date.now();
  const cachedPosition = positionCache.get(cacheKey);
  
  if (cachedPosition && (now - cachedPosition.timestamp < POSITION_CACHE_DURATION)) {
    return { x: cachedPosition.x, y: cachedPosition.y };
  }
  
  // 캔버스 위치를 안정적으로 계산 (캐시 사용)
  const shouldUpdateCache = !stableCanvasRect || (now - lastCanvasUpdateTime > 300); // 300ms 캐시 (더 긴 캐시)
  
  if (shouldUpdateCache) {
    const newCanvasRect = mascotCanvas.value.getBoundingClientRect();
    
    // 실제로 위치가 변경되었는지 확인 (불필요한 업데이트 방지)
    // 5px 이상의 의미있는 변화만 감지 (선택 패널 fixed positioning으로 더 안정)
    const hasSignificantChange = !stableCanvasRect || 
      Math.abs(newCanvasRect.x - stableCanvasRect.x) > 5 ||
      Math.abs(newCanvasRect.y - stableCanvasRect.y) > 5 ||
      Math.abs(newCanvasRect.width - stableCanvasRect.width) > 5 ||
      Math.abs(newCanvasRect.height - stableCanvasRect.height) > 5;
    
    if (hasSignificantChange) {
      stableCanvasRect = newCanvasRect;
      lastCanvasUpdateTime = now;
      console.log('📍 캔버스 위치 실제 변경으로 캐시 업데이트:', stableCanvasRect);
    }
  }
  
  // 마스코트를 기준으로 한 브라우저 전체 화면 절대 좌표 계산
  // 마스코트 기준 상대좌표로부터 브라우저 절대 좌표(아이템 중심)를 계산
  const snap = layoutSnapshot.value;
  const usedMascotRect = snap?.mascotRect || mascotRect.value;
  const browserAbsoluteCenter = toAbsoluteFromMascot(equippedItem.relativePosition, usedMascotRect);

  // 항상 '안정화된' 캔버스 위치를 우선 사용 (레이아웃 변동 시 튐 방지)
  const canvasRect = (layoutSnapshot.value?.canvasRect) || stableCanvasRect || mascotCanvas.value.getBoundingClientRect();

  // 아이템의 사이즈(스케일 반영)를 고려하여 중심 -> 좌상단으로 보정
  const itemSize = BASE_ITEM_SIZE * (equippedItem.scale || 1);
  const half = itemSize / 2;
  let containerRelativePos = {
    x: browserAbsoluteCenter.x - canvasRect.left - half,
    y: browserAbsoluteCenter.y - canvasRect.top - half
  };

  // 화면 밖 일부 허용(자유도↑): 아이템 크기의 10% 만큼 바깥 허용
  const allowOutsidePx = 0.1 * itemSize;
  const maxX = Math.max(-allowOutsidePx, canvasRect.width - itemSize + allowOutsidePx);
  const maxY = Math.max(-allowOutsidePx, canvasRect.height - itemSize + allowOutsidePx);
  containerRelativePos.x = Math.max(-allowOutsidePx, Math.min(maxX, containerRelativePos.x));
  containerRelativePos.y = Math.max(-allowOutsidePx, Math.min(maxY, containerRelativePos.y));
  
  // 계산 결과를 캐시에 저장
  positionCache.set(cacheKey, {
    x: containerRelativePos.x,
    y: containerRelativePos.y,
    timestamp: now
  });
  // 최종 렌더 좌표 캐시 업데이트(저장 중 플리커 방지)
  lastRenderedPosition.set(equippedItem.id, { x: containerRelativePos.x, y: containerRelativePos.y });

  return containerRelativePos;
}

// 마스코트는 항상 캔버스 중앙에 고정
function getMascotCenterPosition(): { x: number; y: number } {
  if (!mascotCanvas.value) {
    return { x: 0, y: 0 };
  }
  
  const containerSize = getContainerSize(mascotCanvas.value);
  return {
    x: containerSize.width / 2,
    y: containerSize.height / 2,
  };
}

// 마스코트 bounding box 업데이트
function updateMascotRect() {
  if (mascotRef.value) {
    mascotRect.value = mascotRef.value.getBoundingClientRect();
    console.log('마스코트 bounding box 업데이트됨:', mascotRect.value);
    
    // 마스코트 위치 변경 시 캔버스 캐시 무효화
    stableCanvasRect = null;
    lastCanvasUpdateTime = 0;
    positionCache.clear();
    updateItemPositionDebounce.clear();
  }
}

// 마스코트 드래그 관련 함수들 제거됨 (마스코트는 고정)

// 드래그 관련 메소드들
function updateCanvasBounds() {
  if (mascotCanvas.value) {
    const newBounds = mascotCanvas.value.getBoundingClientRect();
    
    // 캔버스 크기가 실제로 변경되었는지 확인
    const oldBounds = canvasBounds.value;
    const sizeChanged = !oldBounds ||
      Math.abs(oldBounds.width - newBounds.width) > 2 ||
      Math.abs(oldBounds.height - newBounds.height) > 2;
    const positionChanged = !oldBounds ||
      Math.abs(oldBounds.x - newBounds.x) > 8 ||
      Math.abs(oldBounds.y - newBounds.y) > 8;

    canvasBounds.value = newBounds;
    // 전경 레이어(z-20)의 경계도 갱신 (없으면 캔버스 경계를 사용)
    if (foregroundLayer.value) {
      foregroundBounds.value = foregroundLayer.value.getBoundingClientRect();
    } else {
      foregroundBounds.value = newBounds;
    }

    if (sizeChanged || positionChanged) {
      // 의미있는 변경에만 캐시 무효화
      stableCanvasRect = null;
      lastCanvasUpdateTime = 0;
      if (!isSaving.value) {
        positionCache.clear();
        updateItemPositionDebounce.clear();
      }
      // 마스코트 bounding box도 함께 업데이트
      updateMascotRect();
    }
    
    // 크기 변경 시 상대 좌표 기반으로 아이템 위치 재계산
    if (sizeChanged && oldBounds) {
      console.log('화면 크기 변경 감지:', { 
        old: { width: oldBounds.width, height: oldBounds.height },
        new: { width: newBounds.width, height: newBounds.height }
      });
      
      // 모든 아이템의 절대 위치는 getAbsolutePosition에서 상대 좌표 기준으로 재계산됨
    }
  }
}

// 위치 업데이트 디바운스 (무한 루프 방지)
const updateItemPositionDebounce = new Map<string, number>();
// 드래그 시 더 부드러운 업데이트를 위해 간격을 축소 (약 60fps 수준)
const POSITION_UPDATE_DEBOUNCE = 16;

function updateItemPosition(itemId: string, position: { x: number; y: number }) {
  const state = equippedItemStates.value.get(itemId);
  if (!state || !mascotRect.value) return;
  
  // 디바운스 처리
  const now = Date.now();
  const lastUpdate = updateItemPositionDebounce.get(itemId) || 0;
  
  if (now - lastUpdate < POSITION_UPDATE_DEBOUNCE) {
    // 디바운스로 업데이트 차단 (로깅 최소화)
    return;
  }
  
  updateItemPositionDebounce.set(itemId, now);
  
  // 캔버스 상대 좌표(position: 좌상단 기준)를 브라우저 절대 좌표의 '아이템 중심'으로 변환
  // 항상 최신 캔버스 위치를 측정하여 사용 (저장 직후 레이아웃 변화 대응)
  const snap = layoutSnapshot.value;
  const canvasRect = snap?.canvasRect || mascotCanvas.value?.getBoundingClientRect();
  const usedMascotRect = snap?.mascotRect || mascotRect.value;
  if (!canvasRect || !usedMascotRect) return;
  const itemSize = BASE_ITEM_SIZE * (state.scale || 1);
  const half = itemSize / 2;
  const absoluteCenter = {
    x: canvasRect.left + position.x + half,
    y: canvasRect.top + position.y + half,
  };

  // 브라우저 절대 좌표(중심)를 마스코트 기준 상대 좌표로 변환
  let newRelativePosition = toRelativeToMascot(absoluteCenter, usedMascotRect);

  // 마스코트 기준으로 아이템이 영역을 과도하게 벗어나지 않도록 느슨한 제약 적용 (전 타입 공통 옵션)
  const itemSizePx = { width: BASE_ITEM_SIZE * (state.scale || 1), height: BASE_ITEM_SIZE * (state.scale || 1) };
  const looseOpts = { scaleDown: 0.05, outside: 0.4};
  newRelativePosition = constrainMascotRelativePositionLoose(newRelativePosition, itemSizePx, usedMascotRect, looseOpts);
  
  // 위치 변경이 실제로 있는지 확인 (미세한 변화 무시)
  const oldPos = state.relativePosition;
  // 기존 0.1은 변화 폭이 지나치게 커서 세밀한 드래그가 어렵던 원인
  const POSITION_EPS = 0.005; // 0.5% 단위까지 반영
  const positionChanged = Math.abs(oldPos.x - newRelativePosition.x) > POSITION_EPS || 
                         Math.abs(oldPos.y - newRelativePosition.y) > POSITION_EPS;
  
  if (!positionChanged) {
    // 의미있는 위치 변경 없음 (로깅 최소화)
    return;
  }
  
  state.relativePosition = newRelativePosition;
  equippedItemStates.value.set(itemId, state);
  
  // 위치 캐시 무효화
  positionCache.clear();
  
  console.log(`✅ 아이템 ${itemId} 위치 업데이트:`, {
    absolutePosition: position,
    relativeToMascot: state.relativePosition
  });
}

function updateItemScale(itemId: string, scale: number) {
  const state = equippedItemStates.value.get(itemId);
  if (state) {
    state.scale = scale;
    equippedItemStates.value.set(itemId, state);
  }
}

function updateItemRotation(itemId: string, rotation: number) {
  const state = equippedItemStates.value.get(itemId);
  if (state) {
    state.rotation = rotation;
    equippedItemStates.value.set(itemId, state);
  }
}

function selectItem(itemId: string) {
  selectedItemId.value = itemId;
  
  // 선택 시 캐시는 유지 (레이아웃 변화가 없으므로)
  // positionCache.clear(); // 제거: 불필요한 캐시 정리 방지
  // updateItemPositionDebounce.clear(); // 제거: 안정성 유지
  
  console.log('🎯 아이템 선택:', itemId);
}

function handleCanvasClick(e: Event) {
  // 캔버스 영역 클릭 시(아이템이 아닌 경우) 선택 해제
  // DraggableItem 루트에서는 @click.stop 처리되어 이 핸들러가 호출되지 않음
  selectedItemId.value = null;
}

// UI 개선 메소드들
// 버튼 패널 제거에 따라 버튼 기반 조작 함수 제거

// 선택된 아이템 제거
function removeSelectedItem() {
  if (selectedItemId.value) {
    removeEquippedItem(selectedItemId.value);
  }
}

// 마스코트 위치 조작 함수들 제거됨 (마스코트는 중앙 고정)

// 아이템 클릭 처리 (제한 체크 포함)
function handleItemClick(item: Item) {
  const isCurrentlyEquipped = isItemEquipped(item);
  
  // 장착 해제는 항상 가능
  if (isCurrentlyEquipped) {
    toggleEquipItem(item);
    return;
  }
  
  // 새로 장착할 때는 제한 체크
  if (!canEquipMoreItems.value) {
    showToastMessage(`최대 ${maxEquippedItems}개까지만 장착할 수 있습니다! 먼저 다른 아이템을 제거해주세요.`);
    return;
  }
  
  toggleEquipItem(item);
}

// 전체 조작 메소드들
function resetAllItems() {
  // 확인 다이얼로그 (간단한 confirm 사용)
  if (confirm('모든 아이템의 위치, 크기, 회전을 초기화하시겠습니까?')) {
    equippedItemStates.value.clear();
    equippedItemsList.value = []; // 다중 아이템 목록도 초기화
    selectedItemId.value = null;
    
    // currentMascot.equippedItem 필드 동기화
    updateMascotEquippedItems();
    
    // 다음 프레임에서 다시 기본값으로 설정되도록 함
    setTimeout(() => {
      showToastMessage('모든 아이템이 초기화되었습니다! 🔄');
    }, 100);
  }
}

async function saveItemPositions() {
  try {
    // 서버 저장용 데이터 구성
    const payload = {
      version: 'v1' as const,
      equippedItems: equippedItemsList.value.map(item => ({
        itemId: item.item.id,
        relativePosition: { x: item.relativePosition.x, y: item.relativePosition.y },
        scale: item.scale,
        rotation: item.rotation,
      })),
    };

    console.log('커스터마이징 저장 요청:', payload);

          try {
        isSaving.value = true;
        // 저장 직전 스냅샷 생성(Data URL)
        const snapshot = await composeSnapshotDataUrl();
        await saveMascotCustomization({ ...payload, snapshotImageDataUrl: snapshot });
        // 배경도 함께 저장하여 일관된 저장 UX 제공
        await updateMascotBackground({ backgroundColor: bgColor.value, patternType: bgPattern.value });
        showToastMessage('마스코트 커스터마이징이 서버에 저장되었습니다! 🎯✨');

      // 저장 직후 한 프레임 뒤에 캔버스 바운드를 갱신하여 좌표 캐시를 안정화
      await nextTick();
      updateCanvasBounds();
    } catch (backendError) {
      console.error('백엔드 저장 실패:', backendError);
      const errorMessage = handleApiError(backendError);
      showToastMessage(`서버 저장 실패: ${errorMessage}`);
    } finally {
      // 저장 플래그 해제 및 안정화된 rect 고정
      await nextTick();
      isSaving.value = false;
    }
    
  } catch (error) {
    console.error('저장 프로세스 실패:', error);
    showToastMessage('저장에 실패했습니다. 다시 시도해주세요.');
  }
}

// 저장된 위치 불러오기 (완전한 마이그레이션 포함)
function loadItemPositions() {
  try {
    // 서버에서 커스터마이징 레이아웃을 불러옵니다.
    getMascotCustomization()
      .then((data) => {
        if (!data || !data.equippedItems) {
          console.log('서버 커스터마이징 없음: 빈 상태로 시작');
          return;
        }

        // 서버 데이터 → 뷰 상태로 적용
        equippedItemsList.value = data.equippedItems.map((entry) => {
          const item = items.value.find(i => i.id === entry.itemId);
          if (!item) return null;
          const id = generateItemId(item);
          const state: EquippedItemState = {
            id,
            item,
            relativePosition: { x: entry.relativePosition.x, y: entry.relativePosition.y },
            scale: entry.scale,
            rotation: entry.rotation,
            equippedAt: Date.now(),
          };
          equippedItemStates.value.set(id, state);
          return state;
        }).filter(Boolean) as EquippedItemState[];

        console.log('서버 커스터마이징 로드됨:', equippedItemsList.value);
      })
      .catch((err) => {
        console.error('서버 커스터마이징 로드 실패:', err);
        const message = handleApiError(err);
        showToastMessage(`커스터마이징 로드 실패: ${message}`);
      });
  } catch (error) {
    console.error('위치 불러오기 실패:', error);
  }
}

// 장착된 아이템의 이미지 URL 가져오기
function getEquippedItemImage(itemType: 'head' | 'clothing' | 'accessory' | 'background'): string | undefined {
  if (!currentMascot.value?.equippedItem) return undefined;
  
  // equippedItem 문자열에서 해당 타입의 아이템 찾기
  const equippedItem = items.value.find(item => 
    normalizeType(item.type) === itemType && 
    currentMascot.value!.equippedItem!.includes(item.name)
  );
  
  return equippedItem?.imageUrl;
}

// 장착된 아이템의 이름 가져오기
function getEquippedItemName(itemType: 'head' | 'clothing' | 'accessory' | 'background'): string | undefined {
  if (!currentMascot.value?.equippedItem) return undefined;
  
  // equippedItem 문자열에서 해당 타입의 아이템 찾기
  const equippedItem = items.value.find(item => 
    normalizeType(item.type) === itemType && 
    currentMascot.value!.equippedItem!.includes(item.name)
  );
  
  return equippedItem?.name;
}

function isEquipped(item: Item): boolean {
  return isItemEquipped(item);
}

// 뒤로가기
function goBack() {
  // 백엔드와 실시간 동기화되므로 변경사항 확인 불필요
  // 바로 메인 페이지로 이동
  router.push('/mascot');
}

// 아이템 장착/해제 토글 (다중 아이템 지원)
async function toggleEquipItem(item: Item) {
  if (!currentMascot.value) return;
  
  try {
    const isCurrentlyEquipped = isItemEquipped(item);
    
    if (isCurrentlyEquipped) {
      // 해제: 해당 아이템 중 하나 제거
      const equippedItem = equippedItemsList.value.find(equipped => equipped.item.id === item.id);
      if (equippedItem) {
        removeEquippedItem(equippedItem.id);
      }
    } else {
      // 장착: 새 아이템 추가
      addEquippedItem(item);
    }
    
    // TODO: 나중에 백엔드 API 호출 추가
    // const updatedMascot = await equipItems({ equippedItems: equippedItemsList.value });
    
    console.log('다중 아이템 변경 완료:', equippedItemsList.value);
  } catch (error) {
    console.error('아이템 장착/해제 실패:', error);
    
    // 에러 메시지 표시
    const errorMessage = handleApiError(error);
    showToastMessage(`아이템 변경 실패: ${errorMessage}`);
  }
}

// 마스코트 이미지 로드 완료 처리
function handleMascotImageLoad(event: Event) {
  console.log('마스코트 이미지 로드 완료');
  // 이미지 로드 후 마스코트 bounding box 업데이트
  nextTick(() => {
    updateMascotRect();
    console.log('마스코트 이미지 로드 후 bounding box 업데이트 완료');
  });
}

// 마스코트 이미지 에러 처리
function handleMascotImageError(event: Event) {
  const img = event.target as HTMLImageElement;
  img.src = '/mascot/soll.png'; // 기본 마스코트 이미지로 대체
  console.error('마스코트 이미지 로드 실패:', img.src);
}

// 아이템 이미지 에러 처리
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement;
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjRjNGNEY2Ii8+Cjx0ZXh0IHg9IjIwIiB5PSIyNCIgZmlsbD0iIzlDQTNBRiIgZm9udC1zaXplPSIyNCIgdGV4dC1hbmNob3I9Im1pZGRsZSI+📦PC90ZXh0Pgo8L3N2Zz4K';
}

// 토스트 메시지 표시
function showToastMessage(message: string) {
  toastMessage.value = message;
  showToast.value = true;
  
  setTimeout(() => {
    showToast.value = false;
  }, 2000);
}

// 마스코트 데이터 로드
async function loadMascotData() {
  try {
    console.log('백엔드에서 마스코트 데이터를 로드합니다...');
    
    const mascotData = await getMascot();
    if (mascotData) {
      currentMascot.value = mascotData;
      console.log('마스코트 데이터 로드됨:', mascotData);
      // 배경 커스터마이징 초기값 적용
      if (mascotData.backgroundColor) {
        bgColor.value = mascotData.backgroundColor as string;
      }
      if (mascotData.backgroundPattern) {
        bgPattern.value = mascotData.backgroundPattern as any;
      }
      
      // 기존 장착된 아이템들 로드 (호환성)
      loadEquippedItemsFromMascot();
    } else {
      console.error('마스코트 데이터를 찾을 수 없습니다.');
      // 마스코트가 없으면 메인 페이지로 이동
      router.push('/mascot');
    }
  } catch (error) {
    console.error('마스코트 데이터 로드 실패:', error);
    
    // 에러 메시지 표시
    const errorMessage = handleApiError(error);
    showToastMessage(`마스코트 로드 실패: ${errorMessage}`);
    
    // 에러 발생 시 메인 페이지로 이동
    setTimeout(() => {
      router.push('/mascot');
    }, 2000);
  }
}

// 화면 크기 변경 감지를 위한 ResizeObserver
let resizeObserver: ResizeObserver | null = null;
let mascotResizeObserver: ResizeObserver | null = null;

// keep-alive로 복귀했을 때 보유 아이템 최신화
onActivated(async () => {
  try {
    await loadUserItems();
  } catch {}
});

// 컴포넌트 마운트
onMounted(async () => {
  console.log('마스코트 꾸미기 페이지 로드됨');
  await loadMascotData();
  
  // 캔버스 바운드 및 마스코트 bounding box 업데이트
  await nextTick();
  // 초기 스냅샷 캡처(같은 프레임 측정)
  captureLayoutSnapshot(true);
  
  // 사용자 보유 아이템 로드 추가
  await loadUserItems();
  
  // 저장된 아이템 위치 불러오기
  await nextTick();
  loadItemPositions();
  

  
  // 윈도우 리사이즈 이벤트 리스너 추가
  window.addEventListener('resize', () => captureLayoutSnapshot());
  
  // 마스코트 캔버스의 크기 변경을 정확하게 감지하기 위해 ResizeObserver 사용
  if (mascotCanvas.value && 'ResizeObserver' in window) {
    resizeObserver = new ResizeObserver(() => {
      // 다음 프레임에서 동기 스냅샷 캡처
      nextTick(() => captureLayoutSnapshot());
    });
    resizeObserver.observe(mascotCanvas.value);
    console.log('ResizeObserver가 마스코트 캔버스를 감시하기 시작했습니다.');
  }
  
  // 마스코트 요소 자체의 크기 변경을 감지하기 위한 ResizeObserver
  if (mascotRef.value && 'ResizeObserver' in window) {
    mascotResizeObserver = new ResizeObserver(() => {
      nextTick(() => {
        captureLayoutSnapshot();
        console.log('마스코트 크기 변경 감지 - 스냅샷 업데이트됨');
      });
    });
    mascotResizeObserver.observe(mascotRef.value);
    console.log('마스코트 ResizeObserver가 마스코트 요소를 감시하기 시작했습니다.');
  }
  
  console.log('사용 가능한 아이템들:', items.value);
});

// 컴포넌트 언마운트 시 이벤트 리스너 제거
onUnmounted(() => {
  window.removeEventListener('resize', updateCanvasBounds);
  
  // Canvas ResizeObserver 정리
  if (resizeObserver) {
    resizeObserver.disconnect();
    resizeObserver = null;
    console.log('Canvas ResizeObserver 정리 완료');
  }
  
  // Mascot ResizeObserver 정리
  if (mascotResizeObserver) {
    mascotResizeObserver.disconnect();
    mascotResizeObserver = null;
    console.log('Mascot ResizeObserver 정리 완료');
  }
});
</script>

<style scoped>
/* 라인 클램프 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  overflow: hidden;
}

/* 스크롤바 스타일링 */
.overflow-x-auto::-webkit-scrollbar {
  height: 4px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.overflow-x-auto::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 스무스 전환 */
.transition-all {
  transition: all 0.3s ease;
}

/* 아이템별 기본 스타일 */
.item-head {
  width: 60%;
  height: 60%;
  top: -15%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  object-fit: contain;
}

.item-accessory {
  width: 30%;
  height: 30%;
  top: 25%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  object-fit: contain;
}

.item-clothing {
  width: 80%;
  height: 80%;
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
  object-fit: contain;
}
</style>
