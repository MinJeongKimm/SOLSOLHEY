<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 상단 바 -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="flex items-center justify-between px-4 py-3">
        <!-- 뒤로가기 버튼 -->
        <button 
          @click="goBack"
          class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
        >
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        <!-- 제목 -->
        <div class="text-center">
          <h1 class="text-xl font-bold text-gray-800">{{ challengeTitle || '금융 액션' }}</h1>
        </div>

        <div class="w-8"></div>
      </div>
    </div>

    <div class="p-4 max-w-xl mx-auto">

      <!-- 액션별 폼/컨트롤 -->
      <div class="space-y-4 bg-white border rounded-lg p-4">
        <template v-if="actionType === 'EXCHANGE_RATES'">
          <button @click="callExchangeRates" :disabled="loading" class="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700">
            {{ loading ? '조회 중...' : '환율 전체 조회' }}
          </button>
        </template>

        <template v-else-if="actionType === 'SINGLE_RATE'">
          <div class="flex items-center gap-2">
            <label class="text-sm text-gray-700">통화</label>
            <input v-model="rateCurrency" class="border rounded px-2 py-1 text-sm w-24" placeholder="USD" />
            <button @click="callSingleRate" :disabled="loading" class="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700">
              {{ loading ? '조회 중...' : '환율 조회' }}
            </button>
          </div>
        </template>

        <template v-else-if="actionType === 'ESTIMATE'">
          <div class="grid grid-cols-3 gap-2">
            <div>
              <label class="block text-xs text-gray-700 mb-1">소스</label>
              <input v-model="estimateSrcCurrency" class="border rounded px-2 py-1 text-sm w-full" placeholder="USD" />
            </div>
            <div>
              <label class="block text-xs text-gray-700 mb-1">대상</label>
              <input v-model="estimateDstCurrency" class="border rounded px-2 py-1 text-sm w-full" placeholder="KRW" />
            </div>
            <div>
              <label class="block text-xs text-gray-700 mb-1">금액</label>
              <input v-model.number="estimateAmount" type="number" min="1" class="border rounded px-2 py-1 text-sm w-full" />
            </div>
          </div>
          <button @click="callEstimate" :disabled="loading" class="mt-2 px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700">
            {{ loading ? '계산 중...' : '환전 예상 조회' }}
          </button>
        </template>

        <template v-else-if="actionType === 'TX_HISTORY'">
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs text-gray-700 mb-1">UserKey</label>
              <input v-model="txUserKey" class="border rounded px-2 py-1 text-sm w-full" />
            </div>
            <div>
              <label class="block text-xs text-gray-700 mb-1">계좌번호</label>
              <input v-model="txAccountNo" class="border rounded px-2 py-1 text-sm w-full" />
            </div>
            <div>
              <label class="block text-xs text-gray-700 mb-1">시작일(YYYYMMDD)</label>
              <input v-model="txStartDate" class="border rounded px-2 py-1 text-sm w-full" />
            </div>
            <div>
              <label class="block text-xs text-gray-700 mb-1">종료일(YYYYMMDD)</label>
              <input v-model="txEndDate" class="border rounded px-2 py-1 text-sm w-full" />
            </div>
            <div>
              <label class="block text-xs text-gray-700 mb-1">유형(M/D/A)</label>
              <input v-model="txType" class="border rounded px-2 py-1 text-sm w-full" />
            </div>
            <div>
              <label class="block text-xs text-gray-700 mb-1">정렬(ASC/DESC)</label>
              <input v-model="txOrder" class="border rounded px-2 py-1 text-sm w-full" />
            </div>
          </div>
          <button @click="callTxHistory" :disabled="loading" class="mt-2 px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700">
            {{ loading ? '조회 중...' : '거래내역 조회' }}
          </button>
        </template>

        <template v-else>
          <p class="text-sm text-gray-600">알 수 없는 액션입니다. 뒤로 가기 후 다시 시도하세요.</p>
        </template>
      </div>

      <!-- 결과/에러 출력 -->
      <div class="mt-4">
        <p v-if="error" class="text-sm text-red-600 mb-2">{{ error }}</p>
        <div v-if="result" class="text-xs text-gray-700 bg-gray-50 border rounded p-2">
          <pre class="whitespace-pre-wrap">{{ result }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getChallenges, getAllExchangeRates, getExchangeRate, estimateExchange, getTransactionHistory } from '../api/index';
import type { Challenge } from '../types/api';

const route = useRoute();
const router = useRouter();

const challengeId = Number(route.params.challengeId);
const challenge = ref<Challenge | null>(null);
const challengeTitle = computed(() => challenge.value?.challengeName || '');

// 액션 타입 추론
type ActionType = 'EXCHANGE_RATES' | 'SINGLE_RATE' | 'ESTIMATE' | 'TX_HISTORY' | 'UNKNOWN';
const actionType = ref<ActionType>('UNKNOWN');

// 공통 상태
const loading = ref(false);
const error = ref('');
const result = ref<any>(null);

// 폼 상태
const rateCurrency = ref('USD');
const estimateSrcCurrency = ref('USD');
const estimateDstCurrency = ref('KRW');
const estimateAmount = ref<number | null>(100);
const txUserKey = ref('');
const txAccountNo = ref('');
const txStartDate = ref('20240101');
const txEndDate = ref('20241231');
const txType = ref<'M' | 'D' | 'A'>('A');
const txOrder = ref<'ASC' | 'DESC'>('DESC');

function goBack() {
  router.push('/challenge');
}

function inferActionFromName(name: string): ActionType {
  const n = name.toLowerCase();
  if (n.includes('환율 전체') || n.includes('전체 환율')) return 'EXCHANGE_RATES';
  if (n.includes('환율 확인') || n.includes('단건')) return 'SINGLE_RATE';
  if (n.includes('환전') || n.includes('예상')) return 'ESTIMATE';
  if (n.includes('거래내역')) return 'TX_HISTORY';
  return 'UNKNOWN';
}

async function bootstrap() {
  try {
    const list = await getChallenges();
    const found = list.find(c => c.challengeId === challengeId) || null;
    challenge.value = found;
    if (found) actionType.value = inferActionFromName(found.challengeName);
  } catch (e: any) {
    error.value = '챌린지 정보를 불러오지 못했습니다.';
  }
}

async function callExchangeRates() {
  loading.value = true; error.value = ''; result.value = null;
  try {
    const res = await getAllExchangeRates();
    if (res && res.code === 'success') {
      result.value = { count: Array.isArray(res.payload) ? res.payload.length : 0 };
    } else {
      throw new Error(res?.message || '환율 전체 조회 실패');
    }
  } catch (e: any) {
    error.value = e?.message || '호출 실패';
  } finally { loading.value = false; }
}

async function callSingleRate() {
  if (!rateCurrency.value) { error.value = '통화를 입력하세요.'; return; }
  loading.value = true; error.value = ''; result.value = null;
  try {
    const res = await getExchangeRate(rateCurrency.value);
    if (res && res.code === 'success' && res.exchangeRate != null) {
      result.value = { currency: res.currencyCode, exchangeRate: res.exchangeRate };
    } else {
      throw new Error(res?.message || '환율 조회 실패');
    }
  } catch (e: any) {
    error.value = e?.message || '호출 실패';
  } finally { loading.value = false; }
}

async function callEstimate() {
  if (!estimateSrcCurrency.value || !estimateDstCurrency.value || !estimateAmount.value) { error.value = '입력을 확인하세요.'; return; }
  loading.value = true; error.value = ''; result.value = null;
  try {
    const res = await estimateExchange({ currency: estimateSrcCurrency.value, exchangeCurrency: estimateDstCurrency.value, amount: estimateAmount.value });
    if (res && res.code === 'success' && res.estimatedAmount != null) {
      result.value = { sourceCurrency: res.sourceCurrency, targetCurrency: res.targetCurrency, estimatedAmount: res.estimatedAmount };
    } else {
      throw new Error(res?.message || '환전 예상 조회 실패');
    }
  } catch (e: any) {
    error.value = e?.message || '호출 실패';
  } finally { loading.value = false; }
}

async function callTxHistory() {
  if (!txUserKey.value || !txAccountNo.value || !txStartDate.value || !txEndDate.value) { error.value = '필수 입력을 확인하세요.'; return; }
  loading.value = true; error.value = ''; result.value = null;
  try {
    const res = await getTransactionHistory({ userKey: txUserKey.value, accountNo: txAccountNo.value, startDate: txStartDate.value, endDate: txEndDate.value, transactionType: txType.value, orderByType: txOrder.value });
    if (res && res.code === 'success') {
      result.value = { totalCount: res.totalCount ?? 0 };
    } else {
      throw new Error(res?.message || '거래내역 조회 실패');
    }
  } catch (e: any) {
    error.value = e?.message || '호출 실패';
  } finally { loading.value = false; }
}

onMounted(bootstrap);
</script>

<style scoped>
</style>
