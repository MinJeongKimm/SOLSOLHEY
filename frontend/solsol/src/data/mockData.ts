import type { Mascot, Item } from '../types/api';

// Mock 마스코트 데이터
export const mockMascot: Mascot = {
  id: 1,
  name: '쏠쏠이',
  type: '곰',
  level: 5,
  experiencePoint: 1250,
  evolutionStage: 1,
  equippedItems: {
    clothing: {
      id: 101,
      name: '파란 셔츠',
      type: 'clothing',
      description: '시원한 파란색 셔츠입니다.',
      price: 100,
      imageUrl: '/images/items/blue-shirt.png',
      rarity: 'common',
      isOwned: true
    },
    background: {
      id: 201,
      name: '캠퍼스 배경',
      type: 'background',
      description: '아름다운 대학 캠퍼스 배경입니다.',
      price: 200,
      imageUrl: '/images/items/campus-bg.png',
      rarity: 'rare',
      isOwned: true
    }
  },
  createdAt: '2024-08-18T09:00:00Z',
  updatedAt: '2024-08-18T15:30:00Z'
};

// Mock 아이템 데이터
export const mockItems: Item[] = [
  // 의상 아이템
  {
    id: 101,
    name: '파란 셔츠',
    type: 'clothing',
    description: '시원한 파란색 셔츠입니다.',
    price: 100,
    imageUrl: '/images/items/blue-shirt.png',
    rarity: 'common',
    isOwned: true
  },
  {
    id: 102,
    name: '빨간 후드티',
    type: 'clothing',
    description: '따뜻한 빨간색 후드티입니다.',
    price: 150,
    imageUrl: '/images/items/red-hoodie.png',
    rarity: 'common',
    isOwned: false
  },
  {
    id: 103,
    name: '정장 셔츠',
    type: 'clothing',
    description: '격식 있는 정장 셔츠입니다.',
    price: 300,
    imageUrl: '/images/items/formal-shirt.png',
    rarity: 'rare',
    isOwned: false
  },
  {
    id: 104,
    name: '마법사 로브',
    type: 'clothing',
    description: '신비로운 마법사의 로브입니다.',
    price: 500,
    imageUrl: '/images/items/wizard-robe.png',
    rarity: 'epic',
    isOwned: false
  },

  // 배경 아이템
  {
    id: 201,
    name: '캠퍼스 배경',
    type: 'background',
    description: '아름다운 대학 캠퍼스 배경입니다.',
    price: 200,
    imageUrl: '/images/items/campus-bg.png',
    rarity: 'rare',
    isOwned: true
  },
  {
    id: 202,
    name: '도서관 배경',
    type: 'background',
    description: '조용한 도서관 배경입니다.',
    price: 250,
    imageUrl: '/images/items/library-bg.png',
    rarity: 'rare',
    isOwned: false
  },
  {
    id: 203,
    name: '은행 배경',
    type: 'background',
    description: '신한은행 지점 배경입니다.',
    price: 400,
    imageUrl: '/images/items/bank-bg.png',
    rarity: 'epic',
    isOwned: false
  },
  {
    id: 204,
    name: '우주 배경',
    type: 'background',
    description: '신비로운 우주 공간 배경입니다.',
    price: 800,
    imageUrl: '/images/items/space-bg.png',
    rarity: 'legendary',
    isOwned: false
  },

  // 액세서리 아이템
  {
    id: 301,
    name: '안경',
    type: 'accessory',
    description: '멋진 검은색 안경입니다.',
    price: 80,
    imageUrl: '/images/items/glasses.png',
    rarity: 'common',
    isOwned: true
  },
  {
    id: 302,
    name: '모자',
    type: 'accessory',
    description: '캐주얼한 야구 모자입니다.',
    price: 120,
    imageUrl: '/images/items/cap.png',
    rarity: 'common',
    isOwned: false
  },
  {
    id: 303,
    name: '목걸이',
    type: 'accessory',
    description: '반짝이는 황금 목걸이입니다.',
    price: 350,
    imageUrl: '/images/items/necklace.png',
    rarity: 'rare',
    isOwned: false
  },
  {
    id: 304,
    name: '왕관',
    type: 'accessory',
    description: '화려한 다이아몬드 왕관입니다.',
    price: 1000,
    imageUrl: '/images/items/crown.png',
    rarity: 'legendary',
    isOwned: false
  }
];

// 마스코트 종류 목록
export const mascotTypes = [
  { id: 'bear', name: '곰', description: '힘이 세고 온순한 곰 마스코트' },
  { id: 'tiger', name: '호랑이', description: '용감하고 당당한 호랑이 마스코트' },
  { id: 'eagle', name: '독수리', description: '자유롭고 날렵한 독수리 마스코트' },
  { id: 'lion', name: '사자', description: '위엄있고 카리스마 넘치는 사자 마스코트' },
  { id: 'panda', name: '판다', description: '귀엽고 사랑스러운 판다 마스코트' }
];

// 레벨별 필요 경험치
export const levelExperience = [
  { level: 1, requiredExp: 0 },
  { level: 2, requiredExp: 100 },
  { level: 3, requiredExp: 300 },
  { level: 4, requiredExp: 600 },
  { level: 5, requiredExp: 1000 },
  { level: 6, requiredExp: 1500 },
  { level: 7, requiredExp: 2100 },
  { level: 8, requiredExp: 2800 },
  { level: 9, requiredExp: 3600 },
  { level: 10, requiredExp: 4500 }
];

// 희귀도별 색상 테마
export const rarityColors = {
  common: {
    bg: 'bg-gray-100',
    border: 'border-gray-300',
    text: 'text-gray-700',
    accent: 'text-gray-500'
  },
  rare: {
    bg: 'bg-blue-100',
    border: 'border-blue-300',
    text: 'text-blue-700',
    accent: 'text-blue-500'
  },
  epic: {
    bg: 'bg-purple-100',
    border: 'border-purple-300',
    text: 'text-purple-700',
    accent: 'text-purple-500'
  },
  legendary: {
    bg: 'bg-yellow-100',
    border: 'border-yellow-300',
    text: 'text-yellow-700',
    accent: 'text-yellow-500'
  }
};
