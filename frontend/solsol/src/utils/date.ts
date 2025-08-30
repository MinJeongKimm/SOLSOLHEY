// 간단한 날짜 포맷 유틸
// 입력은 ISO 문자열(yyyy-MM-ddTHH:mm:ss) 또는 [yyyy, MM, dd, HH, mm, ss] 배열을 허용합니다.

export function formatYMD(input?: string | number[] | null): string {
  if (!input) return '';
  // 배열 형태: [yyyy, M, d, ...]
  if (Array.isArray(input)) {
    const [y, m, d] = input as number[];
    if (!y || !m || !d) return '';
    return `${pad(y, 4)}.${pad(m, 2)}.${pad(d, 2)}`;
  }
  // 문자열 처리
  const s = String(input);
  // 2025-08-27T12:34:56 -> 2025.08.27
  if (s.length >= 10) {
    const y = s.slice(0, 4);
    const m = s.slice(5, 7);
    const d = s.slice(8, 10);
    if (y && m && d) return `${y}.${m}.${d}`;
  }
  const d2 = new Date(s);
  if (isNaN(d2.getTime())) return '';
  return `${d2.getFullYear()}.${pad(d2.getMonth() + 1, 2)}.${pad(d2.getDate(), 2)}`;
}

export function formatYMDHMS(input?: string | number[] | null): string {
  if (!input) return '';
  // 배열 형태: [yyyy, M, d, H, m, s]
  if (Array.isArray(input)) {
    const [y, m, d, hh = 0, mm = 0, ss = 0] = input as number[];
    if (!y || !m || !d) return '';
    return `${pad(y, 4)}.${pad(m, 2)}.${pad(d, 2)} ${pad(hh, 2)}:${pad(mm, 2)}:${pad(ss, 2)}`;
  }
  const s = String(input);
  if (s.length >= 19) {
    const ymd = `${s.slice(0, 4)}.${s.slice(5, 7)}.${s.slice(8, 10)}`;
    const hms = `${s.slice(11, 13)}:${s.slice(14, 16)}:${s.slice(17, 19)}`;
    return `${ymd} ${hms}`;
  }
  const d2 = new Date(s);
  if (isNaN(d2.getTime())) return '';
  return `${d2.getFullYear()}.${pad(d2.getMonth() + 1, 2)}.${pad(d2.getDate(), 2)} ${pad(d2.getHours(), 2)}:${pad(d2.getMinutes(), 2)}:${pad(d2.getSeconds(), 2)}`;
}

function pad(n: number, width: number): string {
  const s = String(n);
  return s.length >= width ? s : '0'.repeat(width - s.length) + s;
}

