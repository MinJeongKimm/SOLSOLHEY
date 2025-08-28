<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50">
    <div class="w-full max-w-2xl bg-white rounded-2xl shadow-2xl overflow-hidden">
      <!-- Header -->
      <div class="flex items-center justify-between px-6 py-4 border-b bg-gradient-to-r from-blue-50 to-indigo-50">
        <h3 class="text-lg font-bold text-gray-800 flex items-center gap-2">
          <span>챌린지상세</span>
          <span v-if="title" class="text-sm font-medium text-gray-500">/ {{ title }}</span>
        </h3>
        <button @click="requestClose" class="text-gray-400 hover:text-gray-600 text-2xl leading-none">×</button>
      </div>

      <!-- Tabs (hidden when onlyTab is set) -->
      <div v-if="showTabs" class="px-6 pt-4">
        <div class="flex flex-wrap gap-2">
          <button
            v-for="t in tabs"
            :key="t.key"
            @click="active = t.key"
            :class="[
              'px-3 py-2 rounded-full text-sm font-medium transition-colors',
              active === t.key ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            ]"
          >
            {{ t.label }}
          </button>
        </div>
      </div>

      <!-- Body -->
      <div class="px-6 py-4 space-y-4">
        <!-- Exchange Rates -->
        <div v-if="active === 'EXCHANGE_RATES'" class="space-y-3">
          <div class="text-center">
            <button @click="callExchangeRates" :disabled="loading"
              class="btn-primary">
              {{ loading ? '조회 중...' : '환율 전체 조회' }}
            </button>
          </div>

          <div v-if="exchangeList.length" class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div v-for="it in exchangeList" :key="`${it.currency}-${it.id}`" class="card">
              <div class="flex items-center justify-between">
                <div class="text-sm font-semibold text-gray-800">{{ it.currency }}</div>
              </div>
              <div class="mt-2 flex items-center justify-between">
                <div class="text-xs text-gray-500">환율</div>
                <div class="text-sm font-bold text-blue-700">{{ it.exchangeRate }}</div>
              </div>
              <div class="mt-1 flex items-center justify-between">
                <div class="text-xs text-gray-500">최소 환전</div>
                <div class="text-sm text-gray-800">{{ it.exchangeMin }}</div>
              </div>
              <div class="mt-1 text-right text-[11px] text-gray-400" v-if="it.created">{{ it.created }}</div>
            </div>
          </div>
          <ApiResult :error="error" :raw="raw" :items="[]" raw-label="원본 데이터 보기" />
        </div>

        <!-- Single Rate -->
        <div v-else-if="active === 'SINGLE_RATE'" class="space-y-3">
          <div class="grid grid-cols-3 gap-3">
            <div class="col-span-3 sm:col-span-1">
              <label class="label">통화</label>
              <input v-model="rateCurrency" class="input" placeholder="USD" />
            </div>
          </div>
          <div class="text-center">
            <button @click="callSingleRate" :disabled="loading" class="btn-primary">
              {{ loading ? '조회 중...' : '환율 조회' }}
            </button>
          </div>

          <div v-if="singleRateCard" class="card">
            <div class="card-row">
              <span class="card-key">통화</span>
              <span class="card-val">{{ singleRateCard.currency }}</span>
            </div>
            <div class="card-row">
              <span class="card-key">환율</span>
              <span class="card-val font-semibold text-blue-700">{{ singleRateCard.rate }}</span>
            </div>
            <div v-if="singleRateCard.min" class="card-row">
              <span class="card-key">최소 환전</span>
              <span class="card-val">{{ singleRateCard.min }}</span>
            </div>
            <div v-if="singleRateCard.baseDate" class="card-row">
              <span class="card-key">기준일</span>
              <span class="card-val">{{ singleRateCard.baseDate }}</span>
            </div>
          </div>
          <ApiResult :error="error" :raw="raw" />
        </div>

        <!-- Estimate -->
        <div v-else-if="active === 'ESTIMATE'" class="space-y-3">
          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="label">환전 전 통화</label>
              <input v-model="estimateSrcCurrency" class="input" placeholder="USD" />
            </div>
            <div>
              <label class="label">환전 후 통화</label>
              <input v-model="estimateDstCurrency" class="input" placeholder="KRW" />
            </div>
            <div>
              <label class="label">환전 전 금액</label>
              <input v-model.number="estimateAmount" type="number" min="1" class="input" />
            </div>
          </div>
          <div class="text-center">
            <button @click="callEstimate" :disabled="loading" class="btn-primary">
              {{ loading ? '계산 중...' : '환전 예상 조회' }}
            </button>
          </div>

          <div v-if="estimateCard && estimatedRate != null" class="card">
            <div class="card-row">
              <span class="card-key">적용 환율(예상)</span>
              <span class="card-val font-semibold text-indigo-700">{{ estimatedRate }}</span>
            </div>
          </div>
          <ApiResult :error="error" :raw="raw" />
        </div>

        <!-- Transaction History -->
        <div v-else-if="active === 'TX_HISTORY'" class="space-y-3">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="label">UserKey</label>
              <input v-model="txUserKey" class="input" />
            </div>
            <div>
              <label class="label">계좌번호</label>
              <input v-model="txAccountNo" class="input" />
            </div>
            <div>
              <label class="label">시작일(YYYYMMDD)</label>
              <input v-model="txStartDate" class="input" />
            </div>
            <div>
              <label class="label">종료일(YYYYMMDD)</label>
              <input v-model="txEndDate" class="input" />
            </div>
            <div>
              <label class="label">유형(M/D/A)</label>
              <input v-model="txType" class="input" />
            </div>
            <div>
              <label class="label">정렬(ASC/DESC)</label>
              <input v-model="txOrder" class="input" />
            </div>
          </div>
          <div class="text-center">
            <button @click="callTxHistory" :disabled="loading" class="btn-primary">
              {{ loading ? '조회 중...' : '거래내역 조회' }}
            </button>
          </div>

          <div v-if="txSummary" class="card space-y-2">
            <div class="card-row">
              <span class="card-key">총 건수</span>
              <span class="card-val">{{ txSummary.totalCount }}</span>
            </div>
            <div v-if="txSummary.first" class="pt-2 border-t">
              <div class="text-xs text-gray-500 mb-1">첫 거래 미리보기</div>
              <div class="grid grid-cols-2 gap-2 text-sm">
                <div v-for="(v, k) in txSummary.first" :key="String(k)" class="flex justify-between gap-3">
                  <span class="text-gray-500">{{ k }}</span>
                  <span class="text-gray-800 font-medium">{{ v }}</span>
                </div>
              </div>
            </div>
          </div>
          <ApiResult :error="error" :raw="raw" />
        </div>
      </div>

      <!-- Footer -->
      <div class="px-6 py-4 border-t bg-gray-50 flex items-center justify-end">
        <button @click="requestClose" class="px-4 py-2 rounded-lg bg-gray-200 text-gray-700 hover:bg-gray-300 font-medium">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, defineComponent } from 'vue';
import { getAllExchangeRates, getExchangeRate, estimateExchange, getTransactionHistory } from '../api/index';

interface Tab { key: 'EXCHANGE_RATES'|'SINGLE_RATE'|'ESTIMATE'|'TX_HISTORY'; label: string }

const props = defineProps<{ visible: boolean; title?: string; defaultTab?: Tab['key']; onlyTab?: Tab['key']; challengeId?: number; targetCount?: number }>();
const emit = defineEmits<{ (e: 'close'): void; (e: 'succeeded'): void }>();

const tabs: Tab[] = [
  { key: 'EXCHANGE_RATES', label: '환율 전체 조회' },
  { key: 'SINGLE_RATE', label: '환율 조회' },
  { key: 'ESTIMATE', label: '환전 예상' },
  { key: 'TX_HISTORY', label: '거래내역' },
];

const active = ref<Tab['key']>(props.onlyTab || props.defaultTab || 'EXCHANGE_RATES');
const showTabs = computed(() => !props.onlyTab);
watch(() => [props.onlyTab, props.defaultTab] as const, ([o, d]) => { active.value = o || d || 'EXCHANGE_RATES'; });

// Common state
const loading = ref(false);
const error = ref('');
const raw = ref<any>(null);
const actionInvoked = ref(false);
const actionSucceeded = ref(false);
const lastAction = ref<Tab['key'] | null>(null);

// Single rate
const rateCurrency = ref('USD');
const singleRateCard = ref<{ currency: string; rate: number|string; min?: string|number; baseDate?: string } | null>(null);

// Estimate
const estimateSrcCurrency = ref('USD');
const estimateDstCurrency = ref('KRW');
const estimateAmount = ref<number | null>(100);
const estimateCard = ref<{ 
  sourceCurrency: string; sourceAmount: string|number; sourceName?: string;
  targetCurrency: string; targetAmount: string|number; targetName?: string;
} | null>(null);

// Tx history
const txUserKey = ref('');
const txAccountNo = ref('');
const txStartDate = ref('20240101');
const txEndDate = ref('20241231');
const txType = ref<'M'|'D'|'A'>('A');
const txOrder = ref<'ASC'|'DESC'>('DESC');
const txSummary = ref<{ totalCount: number; first?: Record<string, any> } | null>(null);

function firstArrayIn(obj: any): any[] | null {
  if (!obj) return null;
  const candidates = [obj.payload, obj.data, obj.list, obj.items, obj.rates];
  for (const c of candidates) {
    if (Array.isArray(c)) return c;
    if (c && typeof c === 'object') {
      if (Array.isArray(c.payload)) return c.payload;
      if (Array.isArray(c.list)) return c.list;
      if (Array.isArray(c.items)) return c.items;
    }
  }
  return null;
}

const exchangeList = computed(() => {
  const arr = firstArrayIn(raw.value) || (Array.isArray(raw.value?.REC) ? raw.value.REC : []);
  return (arr as any[]).map((r: any) => ({
    id: r.id ?? r.seq ?? '',
    currency: r.currency || r.currencyCode || '-',
    exchangeRate: r.exchangeRate ?? r.rate ?? r.price ?? '-',
    exchangeMin: r.exchangeMin ?? r.min ?? '-',
    created: r.created ?? r.createdAt ?? r.baseDate ?? '',
  }));
});

function resetState() {
  error.value = '';
  raw.value = null;
  singleRateCard.value = null;
  estimateCard.value = null;
  txSummary.value = null;
}

async function callExchangeRates() {
  loading.value = true; resetState();
  try {
    actionInvoked.value = true;
    const res = await getAllExchangeRates();
    raw.value = res;
    // Accept both backend-wrapped and provider format
    const ok = (res && res.code === 'success') || (res?.Header?.responseCode === 'H0000');
    if (!ok) throw new Error(res?.Header?.responseMessage || res?.message || '환율 전체 조회 실패');
    actionSucceeded.value = true;
    lastAction.value = 'EXCHANGE_RATES';
  } catch (e: any) {
    error.value = e?.message || '호출 실패';
  } finally { loading.value = false; }
}

async function callSingleRate() {
  if (!rateCurrency.value) { error.value = '통화를 입력하세요.'; return; }
  loading.value = true; resetState();
  try {
    actionInvoked.value = true;
    const res = await getExchangeRate(rateCurrency.value);
    raw.value = res;
    // Backend format
    if (res && res.code === 'success' && (res.exchangeRate != null || res.data)) {
      const d = (res.data ?? res);
      singleRateCard.value = { currency: d.currencyCode || d.currency || rateCurrency.value, rate: d.exchangeRate ?? d.rate ?? d.price, min: d.exchangeMin ?? d.min, baseDate: d.baseDate ?? d.created ?? '' };
      actionSucceeded.value = true;
      lastAction.value = 'SINGLE_RATE';
    } else {
      // Provider format
      const ok = res?.Header?.responseCode === 'H0000';
      const rec = res?.REC;
      const item = Array.isArray(rec) ? rec[0] : rec;
      if (ok && item && (item.exchangeRate != null || item.rate != null)) {
        singleRateCard.value = { currency: item.currency || item.currencyCode || rateCurrency.value, rate: item.exchangeRate ?? item.rate ?? item.price, min: item.exchangeMin ?? item.min, baseDate: item.created ?? item.baseDate ?? '' };
        actionSucceeded.value = true;
        lastAction.value = 'SINGLE_RATE';
      } else {
        throw new Error(res?.Header?.responseMessage || res?.message || '환율 조회 실패');
      }
    }
  } catch (e: any) {
    error.value = e?.message || '호출 실패';
  } finally { loading.value = false; }
}

async function callEstimate() {
  if (!estimateSrcCurrency.value || !estimateDstCurrency.value || !estimateAmount.value) { error.value = '입력을 확인하세요.'; return; }
  loading.value = true; resetState();
  try {
    actionInvoked.value = true;
    const res = await estimateExchange({ currency: estimateSrcCurrency.value, exchangeCurrency: estimateDstCurrency.value, amount: estimateAmount.value });
    raw.value = res;
    // Backend format
    if (res && res.code === 'success' && (res.data || (res.estimatedAmount != null))) {
      const d = (res.data ?? res);
      estimateCard.value = {
        sourceCurrency: d.sourceCurrency || estimateSrcCurrency.value,
        sourceAmount: d.sourceAmount ?? d.amount ?? '',
        sourceName: d.sourceCurrencyName,
        targetCurrency: d.targetCurrency || estimateDstCurrency.value,
        targetAmount: d.estimatedAmount ?? d.targetAmount ?? '',
        targetName: d.targetCurrencyName,
      };
      actionSucceeded.value = true;
      lastAction.value = 'ESTIMATE';
    } else {
      // Provider format: REC.currency / REC.exchangeCurrency
      const ok = res?.Header?.responseCode === 'H0000';
      const rec = res?.REC;
      const src = rec?.currency;
      const dst = rec?.exchangeCurrency;
      if (ok && src && dst) {
        estimateCard.value = {
          sourceCurrency: src.currency || estimateSrcCurrency.value,
          sourceAmount: src.amount ?? '',
          sourceName: src.currencyName,
          targetCurrency: dst.currency || estimateDstCurrency.value,
          targetAmount: dst.amount ?? '',
          targetName: dst.currencyName,
        };
        actionSucceeded.value = true;
        lastAction.value = 'ESTIMATE';
      } else {
        throw new Error(res?.Header?.responseMessage || res?.message || '환전 예상 조회 실패');
      }
    }
  } catch (e: any) {
    error.value = e?.message || '호출 실패';
  } finally { loading.value = false; }
}

async function callTxHistory() {
  if (!txUserKey.value || !txAccountNo.value || !txStartDate.value || !txEndDate.value) { error.value = '필수 입력을 확인하세요.'; return; }
  loading.value = true; resetState();
  try {
    actionInvoked.value = true;
    const res = await getTransactionHistory({ userKey: txUserKey.value, accountNo: txAccountNo.value, startDate: txStartDate.value, endDate: txEndDate.value, transactionType: txType.value, orderByType: txOrder.value });
    raw.value = res;
    // Backend format
    if (res && res.code === 'success') {
      const list = (res.list && Array.isArray(res.list) ? res.list : []);
      const first = list[0] ? summarizeObj(list[0]) : undefined;
      txSummary.value = { totalCount: res.totalCount ?? list.length ?? 0, first };
      actionSucceeded.value = true;
      lastAction.value = 'TX_HISTORY';
    } else {
      // Provider format: REC.list
      const ok = res?.Header?.responseCode === 'H0000';
      const list = Array.isArray(res?.REC?.list) ? res.REC.list : [];
      if (ok) {
        const first = list[0] ? summarizeObj(list[0]) : undefined;
        txSummary.value = { totalCount: Number(res?.REC?.totalCount ?? list.length) || list.length, first };
        actionSucceeded.value = true;
        lastAction.value = 'TX_HISTORY';
      } else {
        throw new Error(res?.Header?.responseMessage || res?.message || '거래내역 조회 실패');
      }
    }
  } catch (e: any) {
    error.value = e?.message || '호출 실패';
  } finally { loading.value = false; }
}

function summarizeObj(o: any): Record<string, any> {
  const out: Record<string, any> = {};
  Object.keys(o || {}).slice(0, 6).forEach(k => out[k] = o[k]);
  return out;
}

function num(v: any): number | null {
  if (v == null) return null;
  const s = String(v).replace(/,/g, '').trim();
  const n = Number(s);
  return Number.isFinite(n) ? n : null;
}

const estimatedRate = computed(() => {
  if (!estimateCard.value) return null;
  const s = num(estimateCard.value.sourceAmount);
  const t = num(estimateCard.value.targetAmount);
  if (s && t) {
    const r = t / s;
    if (Number.isFinite(r)) return r.toLocaleString(undefined, { maximumFractionDigits: 6 });
  }
  return null;
});

const showEqualWarning = computed(() => {
  if (!estimateCard.value) return false;
  const s = num(estimateCard.value.sourceAmount);
  const t = num(estimateCard.value.targetAmount);
  if (s == null || t == null) return false;
  return s === t && estimateCard.value.sourceCurrency !== estimateCard.value.targetCurrency;
});

async function requestClose() {
  try {
    if (actionInvoked.value && actionSucceeded.value && lastAction.value) {
      emit('succeeded');
    }
  } catch (e) {
    console.error('닫기 중 완료 반영 실패:', e);
  } finally {
    emit('close');
  }
}

// Local sub-component for pretty results
const ApiResult = defineComponent({
  name: 'ApiResult',
  props: {
    error: { type: String, default: '' },
    raw: { type: Object as any, default: null },
    items: { type: Array as () => Array<{ currency: string; rate: any }>, default: () => [] },
    rawLabel: { type: String, default: '원본 보기' },
  },
  setup(props) {
    const showRaw = ref(false);
    return { showRaw, props } as any;
  },
  template: `
    <div>
      <p v-if="error" class="text-sm text-red-600 text-center">{{ error }}</p>
      <div v-else>
        <div v-if="items && items.length" class="grid grid-cols-2 sm:grid-cols-3 gap-2">
          <div v-for="it in items" :key="it.currency" class="bg-gray-50 rounded-lg p-3 border">
            <div class="text-xs text-gray-500">{{ it.currency }}</div>
            <div class="text-sm font-semibold text-gray-800">{{ it.rate }}</div>
          </div>
        </div>
        <p v-else-if="raw" class="text-center text-sm text-gray-500">데이터가 없습니다.</p>
        <div v-if="raw" class="mt-3">
          <button @click="showRaw = !showRaw" class="text-xs text-gray-500 underline">
            {{ showRaw ? '원본 숨기기' : rawLabel }}
          </button>
          <div v-if="showRaw" class="mt-2 text-xs bg-gray-50 border rounded p-2 max-h-56 overflow-auto">
            <pre class="whitespace-pre-wrap">{{ JSON.stringify(raw, null, 2) }}</pre>
          </div>
        </div>
      </div>
    </div>
  `,
});
</script>

<script lang="ts">
// Lightweight local component for pretty results
export default {};
</script>

<style scoped>
.btn-primary {
  @apply inline-flex items-center justify-center w-full max-w-xs mx-auto px-6 py-3 rounded-lg font-medium bg-blue-600 text-white hover:bg-blue-700 transition-colors shadow;
}
.input {
  @apply w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent;
}
.label {
  @apply block text-xs font-medium text-gray-600 mb-1;
}
.card {
  @apply bg-white border rounded-xl p-4 shadow-sm;
}
.card-row {
  @apply flex items-center justify-between py-1;
}
.card-key { @apply text-sm text-gray-500; }
.card-val { @apply text-sm text-gray-800; }
</style>

<!-- no extra scripts -->
