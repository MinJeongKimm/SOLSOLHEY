<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100">
    <!-- 마스코트 메인 영역 -->
    <div class="container mx-auto px-4 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        <!-- 왼쪽: 마스코트 정보 & 컨트롤 -->
        <div class="lg:col-span-1 space-y-4">
          <!-- 마스코트 정보 카드 -->
          <div class="bg-white rounded-2xl shadow-lg p-6">
            <h2 class="text-xl font-bold text-gray-800 mb-4">🐾 마스코트 정보</h2>
            
            <div v-if="currentMascot" class="space-y-4">
              <!-- 이름 -->
              <div class="text-center">
                <h3 class="text-2xl font-bold text-purple-600">{{ currentMascot.name }}</h3>
                <p class="text-gray-600">{{ getMascotTypeDisplay(currentMascot.type) }}</p>
              </div>
              
              <!-- 레벨 & 경험치 -->
              <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-4">
                <div class="flex justify-between items-center mb-2">
                  <span class="font-semibold text-gray-700">레벨 {{ currentMascot.level }}</span>
                  <span class="text-sm text-gray-500">{{ currentMascot.experiencePoint }} / {{ getNextLevelExp() }} XP</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-3">
                  <div 
                    class="bg-gradient-to-r from-blue-500 to-purple-500 h-3 rounded-full transition-all duration-500"
                    :style="{ width: getExpPercentage() + '%' }"
                  ></div>
                </div>
              </div>
              
              <!-- 진화 단계 -->
              <div class="text-center">
                <span class="inline-block bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-sm font-medium">
                  진화 단계 {{ currentMascot.evolutionStage }}
                </span>
              </div>
            </div>
            
            <div v-else class="text-center py-8">
              <div class="text-6xl mb-4">🥚</div>
              <p class="text-gray-600 mb-4">아직 마스코트가 없습니다</p>
              <button 
                @click="showCreateModal = true"
                class="bg-purple-500 hover:bg-purple-600 text-white px-6 py-2 rounded-lg font-medium transition-colors"
              >
                마스코트 생성하기
              </button>
            </div>
          </div>
          
          <!-- 액션 버튼들 -->
          <div v-if="currentMascot" class="bg-white rounded-2xl shadow-lg p-6">
            <h3 class="text-lg font-bold text-gray-800 mb-4">🎮 액션</h3>
            <div class="space-y-2">
              <button 
                @click="showNotReady('먹이주기')"
                class="w-full bg-green-500 hover:bg-green-600 text-white py-2 px-4 rounded-lg font-medium transition-colors"
              >
                🍎 먹이주기
              </button>
              <button 
                @click="showNotReady('놀아주기')"
                class="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded-lg font-medium transition-colors"
              >
                🎾 놀아주기
              </button>
              <button 
                @click="showNotReady('훈련하기')"
                class="w-full bg-orange-500 hover:bg-orange-600 text-white py-2 px-4 rounded-lg font-medium transition-colors"
              >
                💪 훈련하기
              </button>
              <button 
                @click="openEditModal"
                class="w-full bg-purple-500 hover:bg-purple-600 text-white py-2 px-4 rounded-lg font-medium transition-colors"
              >
                ✏️ 이름 변경
              </button>
            </div>
          </div>
        </div>
        
        <!-- 중앙: 마스코트 캐릭터 -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-2xl shadow-lg p-8 h-full">
            <div class="relative h-full min-h-[400px] flex items-center justify-center">
              <!-- 배경 -->
              <div 
                v-if="currentMascot?.equippedItems.background" 
                class="absolute inset-0 rounded-xl opacity-30"
                :style="{ backgroundImage: `url(${currentMascot.equippedItems.background.imageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }"
              ></div>
              
              <!-- 마스코트 캐릭터 -->
              <div v-if="currentMascot" class="relative z-10 text-center">
                <!-- 메인 캐릭터 (이모지로 대체) -->
                <div class="text-8xl mb-4 animate-bounce">
                  {{ getMascotEmoji(currentMascot.type) }}
                </div>
                
                <!-- 장착된 의상 표시 -->
                <div v-if="currentMascot.equippedItems.clothing" class="text-center">
                  <p class="text-sm text-gray-600">착용중: {{ currentMascot.equippedItems.clothing.name }}</p>
                </div>
                
                <!-- 장착된 액세서리 표시 -->
                <div v-if="currentMascot.equippedItems.accessory" class="text-center mt-2">
                  <p class="text-sm text-gray-600">액세서리: {{ currentMascot.equippedItems.accessory.name }}</p>
                </div>
              </div>
              
              <div v-else class="text-center">
                <div class="text-8xl mb-4 opacity-50">🥚</div>
                <p class="text-gray-500">마스코트를 생성해주세요!</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 오른쪽: 아이템 인벤토리 -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-2xl shadow-lg p-6 h-full">
            <div class="flex justify-between items-center mb-4">
              <h3 class="text-lg font-bold text-gray-800">🎒 아이템</h3>
              <div class="flex space-x-2">
                <button 
                  @click="activeTab = 'inventory'"
                  :class="[
                    'px-3 py-1 rounded-lg text-sm font-medium transition-colors',
                    activeTab === 'inventory' ? 'bg-purple-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  ]"
                >
                  보유
                </button>
                <button 
                  @click="activeTab = 'shop'"
                  :class="[
                    'px-3 py-1 rounded-lg text-sm font-medium transition-colors',
                    activeTab === 'shop' ? 'bg-purple-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  ]"
                >
                  상점
                </button>
              </div>
            </div>
            
            <!-- 아이템 타입 필터 -->
            <div class="flex space-x-1 mb-4">
              <button 
                v-for="type in itemTypes" 
                :key="type.id"
                @click="selectedItemType = type.id"
                :class="[
                  'px-2 py-1 rounded text-xs font-medium transition-colors',
                  selectedItemType === type.id ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                ]"
              >
                {{ type.name }}
              </button>
            </div>
            
            <!-- 아이템 목록 -->
            <div class="space-y-2 max-h-64 overflow-y-auto">
              <div 
                v-for="item in filteredItems" 
                :key="item.id"
                :class="[
                  'border-2 rounded-lg p-3 cursor-pointer transition-all hover:shadow-md',
                  rarityColors[item.rarity]?.bg,
                  rarityColors[item.rarity]?.border,
                  isEquipped(item) ? 'ring-2 ring-purple-500' : ''
                ]"
                @click="selectItem(item)"
              >
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <h4 :class="['font-medium text-sm', rarityColors[item.rarity]?.text]">
                      {{ item.name }}
                      <span v-if="isEquipped(item)" class="text-purple-600 text-xs">✓ 착용중</span>
                    </h4>
                    <p class="text-xs text-gray-600 mt-1">{{ item.description }}</p>
                    <div class="flex justify-between items-center mt-2">
                      <span :class="['text-xs font-medium px-2 py-1 rounded', rarityColors[item.rarity]?.bg]">
                        {{ getRarityDisplay(item.rarity) }}
                      </span>
                      <span class="text-xs font-bold text-yellow-600">{{ item.price }}P</span>
                    </div>
                  </div>
                </div>
                
                <!-- 액션 버튼 -->
                <div class="mt-2 flex space-x-2">
                  <button 
                    v-if="activeTab === 'inventory' && item.isOwned && currentMascot"
                    @click.stop="equipItem(item)"
                    :disabled="isEquipped(item)"
                    :class="[
                      'flex-1 py-1 px-2 rounded text-xs font-medium transition-colors',
                      isEquipped(item) 
                        ? 'bg-gray-300 text-gray-500 cursor-not-allowed' 
                        : 'bg-purple-500 hover:bg-purple-600 text-white'
                    ]"
                  >
                    {{ isEquipped(item) ? '착용중' : '착용' }}
                  </button>
                  <button 
                    v-if="activeTab === 'shop' && !item.isOwned"
                    @click.stop="showNotReady('아이템 구매')"
                    class="flex-1 bg-green-500 hover:bg-green-600 text-white py-1 px-2 rounded text-xs font-medium transition-colors"
                  >
                    구매
                  </button>
                  <button 
                    v-if="isEquipped(item)"
                    @click.stop="unequipItem(item)"
                    class="px-2 py-1 bg-red-500 hover:bg-red-600 text-white rounded text-xs font-medium transition-colors"
                  >
                    해제
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 마스코트 생성 모달 -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
        <h3 class="text-xl font-bold text-gray-800 mb-4">🐾 새 마스코트 생성</h3>
        
        <div class="space-y-4">
          <!-- 이름 입력 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">마스코트 이름</label>
            <input 
              v-model="newMascot.name"
              type="text" 
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:outline-none"
              placeholder="예: 쏠쏠이"
              maxlength="20"
            />
          </div>
          
          <!-- 종류 선택 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">마스코트 종류</label>
            <div class="grid grid-cols-2 gap-2">
              <button 
                v-for="type in mascotTypes" 
                :key="type.id"
                @click="newMascot.type = type.id"
                :class="[
                  'p-3 rounded-lg border-2 transition-all',
                  newMascot.type === type.id 
                    ? 'border-purple-500 bg-purple-50' 
                    : 'border-gray-200 hover:border-gray-300'
                ]"
              >
                <div class="text-2xl mb-1">{{ getMascotEmoji(type.id) }}</div>
                <div class="text-sm font-medium">{{ type.name }}</div>
              </button>
            </div>
          </div>
        </div>
        
        <div class="flex space-x-3 mt-6">
          <button 
            @click="showCreateModal = false"
            class="flex-1 bg-gray-500 hover:bg-gray-600 text-white py-2 px-4 rounded-lg font-medium transition-colors"
          >
            취소
          </button>
          <button 
            @click="createMascot"
            :disabled="!newMascot.name || !newMascot.type"
            class="flex-1 bg-purple-500 hover:bg-purple-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white py-2 px-4 rounded-lg font-medium transition-colors"
          >
            생성
          </button>
        </div>
      </div>
    </div>
    
    <!-- 이름 변경 모달 -->
    <div v-if="showEditModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
        <h3 class="text-xl font-bold text-gray-800 mb-4">✏️ 마스코트 이름 변경</h3>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">새로운 이름</label>
          <input 
            v-model="editName"
            type="text" 
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:outline-none"
            :placeholder="currentMascot?.name"
            maxlength="20"
          />
        </div>
        
        <div class="flex space-x-3 mt-6">
          <button 
            @click="closeEditModal"
            class="flex-1 bg-gray-500 hover:bg-gray-600 text-white py-2 px-4 rounded-lg font-medium transition-colors"
          >
            취소
          </button>
          <button 
            @click="updateMascotName"
            :disabled="!editName || editName === currentMascot?.name"
            class="flex-1 bg-purple-500 hover:bg-purple-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white py-2 px-4 rounded-lg font-medium transition-colors"
          >
            변경
          </button>
        </div>
      </div>
    </div>
    
    <!-- 알림 토스트 -->
    <div 
      v-if="showToast" 
      class="fixed bottom-4 right-4 bg-blue-500 text-white px-6 py-3 rounded-lg shadow-lg z-50 transition-all"
      :class="{ 'opacity-0': !showToast }"
    >
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { createMascot as createMascotApi, equipItems, updateMascot, handleApiError } from '../api/index';
import { mockMascot, mockItems, mascotTypes, levelExperience, rarityColors } from '../data/mockData';
import type { Mascot, Item, CreateMascotRequest, EquipItemsRequest } from '../types/api';

// 반응형 데이터
const currentMascot = ref<Mascot | null>(mockMascot);
const items = ref<Item[]>(mockItems);
const activeTab = ref<'inventory' | 'shop'>('inventory');
const selectedItemType = ref<'all' | 'clothing' | 'background' | 'accessory'>('all');

// 모달 상태
const showCreateModal = ref(false);
const showEditModal = ref(false);

// 폼 데이터
const newMascot = ref<CreateMascotRequest>({
  name: '',
  type: ''
});
const editName = ref('');

// 토스트 알림
const showToast = ref(false);
const toastMessage = ref('');

// 아이템 타입 필터
const itemTypes = [
  { id: 'all', name: '전체' },
  { id: 'clothing', name: '의상' },
  { id: 'background', name: '배경' },
  { id: 'accessory', name: '액세서리' }
];

// 필터링된 아이템 목록
const filteredItems = computed(() => {
  let filtered = items.value;
  
  // 탭에 따른 필터링
  if (activeTab.value === 'inventory') {
    filtered = filtered.filter(item => item.isOwned);
  } else {
    filtered = filtered.filter(item => !item.isOwned);
  }
  
  // 타입에 따른 필터링
  if (selectedItemType.value !== 'all') {
    filtered = filtered.filter(item => item.type === selectedItemType.value);
  }
  
  return filtered;
});

// 유틸리티 함수들
function getMascotEmoji(type: string): string {
  const emojiMap: Record<string, string> = {
    bear: '🐻',
    tiger: '🐯',
    eagle: '🦅',
    lion: '🦁',
    panda: '🐼'
  };
  return emojiMap[type] || '🐾';
}

function getMascotTypeDisplay(type: string): string {
  const typeObj = mascotTypes.find(t => t.id === type);
  return typeObj ? typeObj.name : type;
}

function getRarityDisplay(rarity: string): string {
  const rarityMap: Record<string, string> = {
    common: '일반',
    rare: '희귀',
    epic: '영웅',
    legendary: '전설'
  };
  return rarityMap[rarity] || rarity;
}

function getNextLevelExp(): number {
  if (!currentMascot.value) return 0;
  const nextLevel = currentMascot.value.level + 1;
  const levelData = levelExperience.find(l => l.level === nextLevel);
  return levelData ? levelData.requiredExp : 9999;
}

function getExpPercentage(): number {
  if (!currentMascot.value) return 0;
  const currentLevel = levelExperience.find(l => l.level === currentMascot.value!.level);
  const nextLevel = levelExperience.find(l => l.level === currentMascot.value!.level + 1);
  
  if (!currentLevel || !nextLevel) return 100;
  
  const currentExp = currentMascot.value.experiencePoint - currentLevel.requiredExp;
  const totalExp = nextLevel.requiredExp - currentLevel.requiredExp;
  
  return Math.min(100, (currentExp / totalExp) * 100);
}

function isEquipped(item: Item): boolean {
  if (!currentMascot.value) return false;
  const equipped = currentMascot.value.equippedItems;
  
  switch (item.type) {
    case 'clothing':
      return equipped.clothing?.id === item.id;
    case 'background':
      return equipped.background?.id === item.id;
    case 'accessory':
      return equipped.accessory?.id === item.id;
    default:
      return false;
  }
}

// 마스코트 생성
async function createMascot() {
  try {
    // Mock 데이터로 시뮬레이션
    const newMascotData: Mascot = {
      id: Date.now(),
      name: newMascot.value.name,
      type: newMascot.value.type,
      level: 1,
      experiencePoint: 0,
      evolutionStage: 0,
      equippedItems: {},
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    
    currentMascot.value = newMascotData;
    showCreateModal.value = false;
    showToast.value = true;
    toastMessage.value = `${newMascotData.name}이(가) 태어났습니다! 🎉`;
    
    setTimeout(() => {
      showToast.value = false;
    }, 3000);
    
    // 실제 API 호출 (주석 처리)
    // const response = await createMascotApi(newMascot.value);
    // if (response.success && response.mascot) {
    //   currentMascot.value = response.mascot;
    // }
  } catch (error) {
    console.error('마스코트 생성 실패:', error);
    showNotReady('마스코트 생성');
  }
}

// 아이템 장착
async function equipItem(item: Item) {
  if (!currentMascot.value) return;
  
  try {
    // Mock 데이터로 시뮬레이션
    const updatedMascot = { ...currentMascot.value };
    updatedMascot.equippedItems = { ...updatedMascot.equippedItems };
    
    switch (item.type) {
      case 'clothing':
        updatedMascot.equippedItems.clothing = item;
        break;
      case 'background':
        updatedMascot.equippedItems.background = item;
        break;
      case 'accessory':
        updatedMascot.equippedItems.accessory = item;
        break;
    }
    
    currentMascot.value = updatedMascot;
    showToast.value = true;
    toastMessage.value = `${item.name}을(를) 착용했습니다!`;
    
    setTimeout(() => {
      showToast.value = false;
    }, 2000);
    
    // 실제 API 호출 (주석 처리)
    // const equipData: EquipItemsRequest = {
    //   equipItems: {
    //     [`${item.type}Id`]: item.id
    //   }
    // };
    // const response = await equipItems(equipData);
  } catch (error) {
    console.error('아이템 착용 실패:', error);
    showNotReady('아이템 착용');
  }
}

// 아이템 해제
function unequipItem(item: Item) {
  if (!currentMascot.value) return;
  
  const updatedMascot = { ...currentMascot.value };
  updatedMascot.equippedItems = { ...updatedMascot.equippedItems };
  
  switch (item.type) {
    case 'clothing':
      updatedMascot.equippedItems.clothing = undefined;
      break;
    case 'background':
      updatedMascot.equippedItems.background = undefined;
      break;
    case 'accessory':
      updatedMascot.equippedItems.accessory = undefined;
      break;
  }
  
  currentMascot.value = updatedMascot;
  showToast.value = true;
  toastMessage.value = `${item.name}을(를) 해제했습니다.`;
  
  setTimeout(() => {
    showToast.value = false;
  }, 2000);
}

// 이름 변경 모달
function openEditModal() {
  if (currentMascot.value) {
    editName.value = currentMascot.value.name;
    showEditModal.value = true;
  }
}

function closeEditModal() {
  showEditModal.value = false;
  editName.value = '';
}

// 마스코트 이름 변경
async function updateMascotName() {
  if (!currentMascot.value || !editName.value) return;
  
  try {
    // Mock 데이터로 시뮬레이션
    const updatedMascot = { ...currentMascot.value };
    updatedMascot.name = editName.value;
    updatedMascot.updatedAt = new Date().toISOString();
    
    currentMascot.value = updatedMascot;
    showEditModal.value = false;
    showToast.value = true;
    toastMessage.value = `이름이 ${editName.value}(으)로 변경되었습니다!`;
    
    setTimeout(() => {
      showToast.value = false;
    }, 2000);
    
    // 실제 API 호출 (주석 처리)
    // const response = await updateMascot({ name: editName.value });
  } catch (error) {
    console.error('이름 변경 실패:', error);
    showNotReady('이름 변경');
  }
}

// 준비중 알림
function showNotReady(feature: string) {
  showToast.value = true;
  toastMessage.value = `${feature} 기능은 준비중입니다! 🚧`;
  
  setTimeout(() => {
    showToast.value = false;
  }, 2000);
}

function selectItem(item: Item) {
  // 아이템 상세 정보 표시 (준비중)
  showNotReady('아이템 상세 정보');
}

// 컴포넌트 마운트
onMounted(() => {
  // 마스코트 정보 로드 (현재는 mock 데이터 사용)
  console.log('마스코트 페이지 로드됨');
});
</script>

<style scoped>
/* 애니메이션 */
@keyframes bounce {
  0%, 20%, 53%, 80%, 100% {
    transform: translate3d(0,0,0);
  }
  40%, 43% {
    transform: translate3d(0,-30px,0);
  }
  70% {
    transform: translate3d(0,-15px,0);
  }
  90% {
    transform: translate3d(0,-4px,0);
  }
}

.animate-bounce {
  animation: bounce 2s infinite;
}

/* 스크롤바 스타일링 */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
