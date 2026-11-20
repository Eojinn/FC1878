const DOW = ['일', '월', '화', '수', '목', '금', '토'];

/** 브라우저 시간대와 관계없이 한국 날짜(YYYY-MM-DD) */
export function kstToday(offsetDays = 0) {
  const now = new Date(Date.now() + 9 * 3600 * 1000 + offsetDays * 86400 * 1000);
  return now.toISOString().slice(0, 10);
}

export function dateTabs() {
  return [-1, 0, 1, 2, 3, 4, 5].map((o) => {
    const iso = kstToday(o);
    const [, m, d] = iso.split('-');
    const dow = DOW[new Date(iso + 'T00:00:00Z').getUTCDay()];
    const label = o === -1 ? '어제' : o === 0 ? '오늘' : o === 1 ? '내일' : dow;
    return { iso, label, short: `${m}.${d}` };
  });
}

/** "2026-11-22T01:30:00" → "01:30" (서버가 KST 로 내려줌) */
export const hhmm = (dt) => (dt ? dt.slice(11, 16) : '');

/** "2026-11-22T01:30:00" → "11.22 (일) 01:30" */
export function kickoffLabel(dt) {
  if (!dt) return '';
  const date = dt.slice(0, 10);
  const dow = DOW[new Date(date + 'T00:00:00Z').getUTCDay()];
  return `${date.slice(5, 7)}.${date.slice(8, 10)} (${dow}) ${hhmm(dt)}`;
}

/** "Regular Season - 13" → "13라운드" */
export function roundLabel(round) {
  if (!round) return '';
  const m = round.match(/(\d+)\s*$/);
  return m && round.startsWith('Regular Season') ? `${m[1]}라운드` : round;
}

export function relativeTime(dt) {
  if (!dt) return '';
  const diff = (Date.now() - new Date(dt + '+09:00').getTime()) / 1000;
  if (diff < 60) return '방금 전';
  if (diff < 3600) return `${Math.floor(diff / 60)}분 전`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`;
  return dt.slice(5, 10).replace('-', '.');
}

export const num = (n) => (n ?? 0).toLocaleString('ko-KR');

export const CATEGORY_LABEL = { FREE: '자유', MATCH: '경기', TRANSFER: '이적', OVERSEAS: '해외파', TACTICS: '전술' };

/** 인기 리그 표시 순서 (football-data.org 대회 ID: PL, PD, SA, BL1, FL1, CL) */
export const POPULAR_LEAGUE_ORDER = [2021, 2014, 2019, 2002, 2015, 2001];

/** 프리미어리그 ID (football-data.org) */
export const EPL_IDS = [2021];
