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
      <div class="bg-gradient-to-br from-blue-50 to-purple-50 rounded-2xl p-8 mb-6">
        <!-- 모바일 도움말 -->
        <div v-if="isMobileDevice" class="mb-4 p-3 bg-blue-100 rounded-lg text-sm text-blue-800">
          <div class="flex items-center space-x-2 mb-1">
            <span>📱</span>
            <span class="font-medium">터치 조작법</span>
          </div>
          <div class="text-xs space-y-1">
            <div>• 한 손가락으로 드래그하여 이동</div>
            <div>• 두 손가락으로 핀치하여 크기 조절</div>
            <div>• 두 손가락으로 비틀어서 회전</div>
            <div>• 짧게 탭하여 아이템 선택</div>
            <div>• 같은 아이템 중복 장착 가능 (최대 10개)</div>
          </div>
        </div>
        
        <div 
          class="relative h-64 rounded-xl overflow-hidden flex items-center justify-center"
          style="background: linear-gradient(135deg, #bfdbfe 0%, #ddd6fe 100%)"
        >
          <!-- 커스터마이즈 배경 -->
          <img 
            src="/backgrounds/bg_customize.png" 
            alt="꾸미기 배경" 
            class="absolute inset-0 w-full h-full object-cover"
          />
          
          <!-- 마스코트 + 장착된 아이템들 -->
          <div 
            ref="mascotCanvas"
            class="absolute inset-0 flex items-center justify-center"
            @click="handleCanvasClick"
          >
            <!-- 마스코트 이미지 (중앙 고정) -->
            <div class="relative">
              <img 
                :src="currentMascot ? getMascotImageUrl(currentMascot.type) : '/mascot/soll.png'" 
                :alt="currentMascot?.name || '마스코트'" 
                class="w-32 h-32 object-contain"
                @error="handleMascotImageError"
              />
            </div>
            
            <!-- 드래그 가능한 장착된 아이템들 -->
            <DraggableItem
              v-for="equippedItem in equippedItems"
              :key="equippedItem.id"
              :item="equippedItem.item"
              :position="equippedItem.position"
              :scale="equippedItem.scale"
              :rotation="equippedItem.rotation"
              :is-selected="selectedItemId === equippedItem.id"
              :container-bounds="canvasBounds"
              @update:position="updateItemPosition(equippedItem.id, $event)"
              @update:scale="updateItemScale(equippedItem.id, $event)"
              @update:rotation="updateItemRotation(equippedItem.id, $event)"
              @select="selectItem(equippedItem.id)"
            />
          </div>
          
          <!-- 마스코트 이름 -->
          <div class="absolute bottom-4 left-1/2 transform -translate-x-1/2">
            <div class="bg-white bg-opacity-90 px-3 py-1 rounded-full">
              <span class="text-sm font-medium text-gray-800">{{ currentMascot?.name || '쏠' }}</span>
            </div>
          </div>
          
          <!-- 선택된 아이템 정보 (모바일) -->
          <div 
            v-if="isMobileDevice && selectedItemId && selectedItemInfo"
            class="absolute top-2 right-2 bg-white bg-opacity-95 p-2 rounded-lg shadow-lg text-xs max-w-32"
          >
            <div class="font-medium text-gray-800 mb-1">{{ selectedItemInfo.name }}</div>
            <div class="text-gray-600 space-y-1">
              <div>위치: {{ Math.round(selectedItemInfo.position.x) }}, {{ Math.round(selectedItemInfo.position.y) }}</div>
              <div>크기: {{ Math.round(selectedItemInfo.scale * 100) }}%</div>
              <div>회전: {{ Math.round(selectedItemInfo.rotation) }}°</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 선택된 아이템 조작 패널 -->
      <div v-if="selectedItemId" class="mb-6 p-4 bg-blue-50 rounded-xl border border-blue-200">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center space-x-2">
            <span class="text-blue-600 font-medium">{{ selectedItemInfo?.name }}</span>
            <span class="text-xs text-blue-500">(선택됨)</span>
          </div>
          <div class="flex space-x-2">
            <button 
              @click="resetItemPosition(selectedItemId!)"
              class="text-xs bg-blue-100 hover:bg-blue-200 text-blue-700 px-2 py-1 rounded transition-colors"
            >
              초기화
            </button>
            <button 
              @click="removeSelectedItem()"
              class="text-xs bg-red-100 hover:bg-red-200 text-red-700 px-2 py-1 rounded transition-colors"
              title="아이템 제거"
            >
              🗑️ 제거
            </button>
          </div>
        </div>
        
        <div class="grid grid-cols-3 gap-3">
          <!-- 크기 조절 버튼들 -->
          <div class="space-y-2">
            <div class="text-xs text-gray-600 font-medium">크기</div>
            <div class="flex space-x-1">
              <button 
                @click="adjustItemScale(selectedItemId, -0.1)"
                class="w-6 h-6 bg-gray-100 hover:bg-gray-200 rounded text-xs transition-colors"
              >
                -
              </button>
              <div class="flex-1 bg-gray-100 rounded px-1 py-1 text-center text-xs">
                {{ Math.round((selectedItemInfo?.scale || 1) * 100) }}%
              </div>
              <button 
                @click="adjustItemScale(selectedItemId, 0.1)"
                class="w-6 h-6 bg-gray-100 hover:bg-gray-200 rounded text-xs transition-colors"
              >
                +
              </button>
            </div>
          </div>
          
          <!-- 회전 조절 버튼들 -->
          <div class="space-y-2">
            <div class="text-xs text-gray-600 font-medium">회전</div>
            <div class="flex space-x-1">
              <button 
                @click="adjustItemRotation(selectedItemId, -15)"
                class="w-6 h-6 bg-gray-100 hover:bg-gray-200 rounded text-xs transition-colors"
                title="반시계 방향"
              >
                ↺
              </button>
              <div class="flex-1 bg-gray-100 rounded px-1 py-1 text-center text-xs">
                {{ Math.round(selectedItemInfo?.rotation || 0) }}°
              </div>
              <button 
                @click="adjustItemRotation(selectedItemId, 15)"
                class="w-6 h-6 bg-gray-100 hover:bg-gray-200 rounded text-xs transition-colors"
                title="시계 방향"
              >
                ↻
              </button>
            </div>
          </div>
          
          <!-- 퀵 포지션 버튼들 -->
          <div class="space-y-2">
            <div class="text-xs text-gray-600 font-medium">위치</div>
            <div class="grid grid-cols-3 gap-1">
              <button 
                v-for="position in quickPositions" 
                :key="position.name"
                @click="setItemQuickPosition(selectedItemId, position)"
                class="text-xs bg-gray-100 hover:bg-gray-200 p-1 rounded transition-colors"
                :title="position.name"
              >
                {{ position.icon }}
              </button>
            </div>
          </div>
        </div>
        
        <!-- 퀵 회전 버튼들 -->
        <div class="mt-3 p-2 bg-gray-50 rounded-lg">
          <div class="text-xs text-gray-600 font-medium mb-2">퀵 회전</div>
          <div class="grid grid-cols-4 gap-2">
            <button 
              v-for="angle in quickRotations" 
              :key="angle"
              @click="setItemQuickRotation(selectedItemId, angle)"
              class="text-xs bg-white hover:bg-gray-100 py-1 px-2 rounded border transition-colors"
            >
              {{ angle }}°
            </button>
          </div>
        </div>
      </div>
      
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
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { equipItems, getMascot, handleApiError } from '../api/index';
import DraggableItem from '../components/DraggableItem.vue';
import { mascotTypes, realItems } from '../data/mockData';
import type { Item, Mascot } from '../types/api';

// 아이템 상태 인터페이스 (다중 아이템 지원)
interface EquippedItemState {
  id: string; // 고유 ID (item.id + 장착 순서)
  item: Item;
  position: { x: number; y: number };
  scale: number;
  rotation: number; // 회전 각도 (degrees)
  equippedAt: number; // 장착 시간 (타임스탬프)
}

const router = useRouter();

// 반응형 데이터
const currentMascot = ref<Mascot | null>(null);
const items = ref<Item[]>(realItems);
const userCoins = ref(15000);
const selectedCategory = ref<'head' | 'clothing' | 'accessory' | 'background'>('head');

// 드래그 관련 상태
const mascotCanvas = ref<HTMLElement>();
const canvasBounds = ref<DOMRect | null>(null);
const selectedItemId = ref<string | null>(null); // 고유 ID로 변경
const equippedItemStates = ref<Map<string, EquippedItemState>>(new Map()); // 다중 아이템 지원
const isMobileDevice = ref(/Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent));

// 다중 아이템 관리
const equippedItemsList = ref<EquippedItemState[]>([]); // 장착된 아이템 목록
const maxEquippedItems = 10; // 최대 장착 가능 아이템 수

// 토스트 알림
const showToast = ref(false);
const toastMessage = ref('');

// 아이템 카테고리
const itemCategories = [
  { id: 'head', name: 'Head', icon: '👕' },
  { id: 'clothing', name: 'Clothing', icon: '👖' },
  { id: 'accessory', name: 'Accessory', icon: '👓' },
  { id: 'background', name: 'Background', icon: '🖼️' }
];

// 퀵 포지션 옵션
const quickPositions = [
  { name: '좌상', icon: '↖', position: { x: 20, y: 20 } },
  { name: '상단', icon: '↑', position: { x: 120, y: 20 } },
  { name: '우상', icon: '↗', position: { x: 200, y: 20 } },
  { name: '좌측', icon: '←', position: { x: 20, y: 120 } },
  { name: '중앙', icon: '⊙', position: { x: 120, y: 120 } },
  { name: '우측', icon: '→', position: { x: 200, y: 120 } },
];

// 퀵 회전 옵션
const quickRotations = [0, 90, 180, 270];

// 필터링된 아이템 목록 (보유한 아이템만)
const filteredItems = computed(() => {
  return items.value.filter(item => 
    item.type === selectedCategory.value && item.isOwned
  );
});

// 장착된 아이템들의 상태 목록 (다중 아이템 지원)
const equippedItems = computed(() => {
  // 새로운 다중 아이템 시스템에서는 equippedItemsList를 직접 사용
  return equippedItemsList.value;
});

// 더 많은 아이템을 장착할 수 있는지 확인
const canEquipMoreItems = computed(() => {
  return equippedItemsList.value.length < maxEquippedItems;
});

// 기존 마스코트 데이터에서 아이템 로드 (호환성을 위함)
function loadEquippedItemsFromMascot() {
  if (!currentMascot.value?.equippedItem) return;
  
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
        addEquippedItem(item);
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
    position: state.position,
    scale: state.scale,
    rotation: state.rotation,
  };
});

// 유틸리티 함수들
function getMascotImageUrl(type: string): string {
  console.log('꾸미기 화면에서 getMascotImageUrl 호출됨:', { type });
  const typeObj = mascotTypes.find(t => t.id === type);
  const imageUrl = typeObj ? typeObj.imageUrl : '/mascot/soll.png';
  console.log('꾸미기 화면에서 결정된 이미지 URL:', imageUrl);
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

// 아이템 타입별 기본 위치 설정
function getDefaultPosition(itemType: string): { x: number; y: number } {
  const canvasCenter = { x: 120, y: 120 }; // 캔버스 중앙 기준
  
  const defaultPositions: Record<string, { x: number; y: number }> = {
    head: { x: canvasCenter.x - 60, y: canvasCenter.y - 80 },
    clothing: { x: canvasCenter.x - 60, y: canvasCenter.y - 40 },
    accessory: { x: canvasCenter.x - 60, y: canvasCenter.y - 20 },
    background: { x: canvasCenter.x - 60, y: canvasCenter.y - 60 },
  };
  
  return defaultPositions[itemType] || { x: canvasCenter.x - 60, y: canvasCenter.y - 60 };
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
    position: getDefaultPosition(item.type),
    scale: 1,
    rotation: 0,
    equippedAt: Date.now(),
  };
  
  equippedItemsList.value.push(newEquippedItem);
  equippedItemStates.value.set(id, newEquippedItem);
  
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
  
  showToastMessage(`${removedItem.item.name}을(를) 해제했습니다!`);
  return true;
}

function isItemEquipped(item: Item): boolean {
  return equippedItemsList.value.some(equipped => equipped.item.id === item.id);
}

function getEquippedCount(item: Item): number {
  return equippedItemsList.value.filter(equipped => equipped.item.id === item.id).length;
}

// 드래그 관련 메소드들
function updateCanvasBounds() {
  if (mascotCanvas.value) {
    canvasBounds.value = mascotCanvas.value.getBoundingClientRect();
  }
}

function updateItemPosition(itemId: string, position: { x: number; y: number }) {
  const state = equippedItemStates.value.get(itemId);
  if (state) {
    state.position = position;
    equippedItemStates.value.set(itemId, state);
  }
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
}

function handleCanvasClick(e: Event) {
  // 캔버스 빈 공간 클릭 시 선택 해제
  if (e.target === mascotCanvas.value) {
    selectedItemId.value = null;
  }
}

// UI 개선 메소드들
function adjustItemScale(itemId: string, scaleChange: number) {
  const state = equippedItemStates.value.get(itemId);
  if (state) {
    const newScale = Math.max(0.3, Math.min(4, state.scale + scaleChange));
    updateItemScale(itemId, newScale);
    
    // 시각적 피드백
    showToastMessage(`크기: ${Math.round(newScale * 100)}%`);
  }
}

function resetItemPosition(itemId: string) {
  const state = equippedItemStates.value.get(itemId);
  if (state) {
    const defaultPos = getDefaultPosition(state.item.type);
    updateItemPosition(itemId, defaultPos);
    updateItemScale(itemId, 1);
    updateItemRotation(itemId, 0);
    
    showToastMessage(`${state.item.name} 위치가 초기화되었습니다`);
  }
}

function setItemQuickPosition(itemId: string, quickPosition: { name: string; icon: string; position: { x: number; y: number } }) {
  const state = equippedItemStates.value.get(itemId);
  if (state) {
    updateItemPosition(itemId, quickPosition.position);
    
    showToastMessage(`${state.item.name} → ${quickPosition.name}`);
  }
}

// 회전 조작 메소드들
function adjustItemRotation(itemId: string, rotationChange: number) {
  const state = equippedItemStates.value.get(itemId);
  if (state) {
    let newRotation = state.rotation + rotationChange;
    newRotation = ((newRotation % 360) + 360) % 360; // 0-360 범위로 정규화
    updateItemRotation(itemId, newRotation);
    
    // 시각적 피드백
    showToastMessage(`회전: ${Math.round(newRotation)}°`);
  }
}

function setItemQuickRotation(itemId: string, angle: number) {
  const state = equippedItemStates.value.get(itemId);
  if (state) {
    updateItemRotation(itemId, angle);
    
    showToastMessage(`${state.item.name} → ${angle}°`);
  }
}

// 선택된 아이템 제거
function removeSelectedItem() {
  if (selectedItemId.value) {
    removeEquippedItem(selectedItemId.value);
  }
}

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
    
    // 다음 프레임에서 다시 기본값으로 설정되도록 함
    setTimeout(() => {
      showToastMessage('모든 아이템이 초기화되었습니다');
    }, 100);
  }
}

function saveItemPositions() {
  // 실제 저장 로직은 백엔드 연동이 필요하지만, 
  // 현재는 localStorage에 저장하는 것으로 시뮬레이션
  try {
    const positionsData = {
      equippedItems: equippedItemsList.value,
      itemStates: {}
    };
    
    equippedItemStates.value.forEach((state, itemId) => {
      positionsData.itemStates[itemId] = {
        position: state.position,
        scale: state.scale,
        rotation: state.rotation,
      };
    });
    
    localStorage.setItem('mascot-multiple-items', JSON.stringify(positionsData));
    showToastMessage('아이템 위치가 저장되었습니다! 💾');
    
    console.log('저장된 다중 아이템 데이터:', positionsData);
  } catch (error) {
    console.error('위치 저장 실패:', error);
    showToastMessage('저장에 실패했습니다. 다시 시도해주세요.');
  }
}

// 저장된 위치 불러오기
function loadItemPositions() {
  try {
    const savedData = localStorage.getItem('mascot-multiple-items');
    if (savedData) {
      const positionsData = JSON.parse(savedData);
      
      if (positionsData.equippedItems) {
        // 새로운 다중 아이템 데이터 형식
        equippedItemsList.value = positionsData.equippedItems;
        
        // 상태 맵 재구성
        equippedItemStates.value.clear();
        equippedItemsList.value.forEach(item => {
          equippedItemStates.value.set(item.id, item);
        });
        
        // 저장된 상태 적용
        if (positionsData.itemStates) {
          Object.entries(positionsData.itemStates).forEach(([itemId, data]: [string, any]) => {
            const state = equippedItemStates.value.get(itemId);
            if (state && data.position && data.scale !== undefined) {
              state.position = data.position;
              state.scale = data.scale;
              state.rotation = data.rotation || 0;
              equippedItemStates.value.set(itemId, state);
            }
          });
        }
        
        console.log('저장된 다중 아이템 데이터 불러옴:', positionsData);
      }
    }
  } catch (error) {
    console.error('위치 불러오기 실패:', error);
  }
}

// 장착된 아이템의 이미지 URL 가져오기
function getEquippedItemImage(itemType: 'head' | 'clothing' | 'accessory' | 'background'): string | undefined {
  if (!currentMascot.value?.equippedItem) return undefined;
  
  // equippedItem 문자열에서 해당 타입의 아이템 찾기
  const equippedItem = items.value.find(item => 
    item.type === itemType && 
    currentMascot.value!.equippedItem!.includes(item.name)
  );
  
  return equippedItem?.imageUrl;
}

// 장착된 아이템의 이름 가져오기
function getEquippedItemName(itemType: 'head' | 'clothing' | 'accessory' | 'background'): string | undefined {
  if (!currentMascot.value?.equippedItem) return undefined;
  
  // equippedItem 문자열에서 해당 타입의 아이템 찾기
  const equippedItem = items.value.find(item => 
    item.type === itemType && 
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

// 컴포넌트 마운트
onMounted(async () => {
  console.log('마스코트 꾸미기 페이지 로드됨');
  await loadMascotData();
  
  // 캔버스 바운드 업데이트
  await nextTick();
  updateCanvasBounds();
  
  // 저장된 아이템 위치 불러오기
  await nextTick();
  loadItemPositions();
  
  // 윈도우 리사이즈 이벤트 리스너 추가
  window.addEventListener('resize', updateCanvasBounds);
  
  console.log('사용 가능한 아이템들:', items.value);
});

// 컴포넌트 언마운트 시 이벤트 리스너 제거
onUnmounted(() => {
  window.removeEventListener('resize', updateCanvasBounds);
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