/* 이적시장 화면용 표시 함수 */

export const TRANSFER_TYPE = {
  PERMANENT: { label: '완전 이적', cls: 'permanent' },
  LOAN: { label: '임대', cls: 'loan' },
  FREE: { label: '자유 이적', cls: 'free' },
  LOAN_RETURN: { label: '임대 복귀', cls: 'return' },
};

/** 145 → "€145M", 4.5 → "€4.5M" */
export function formatFee(million) {
  if (million == null) return null;
  const n = Number(million);
  return `€${Number.isInteger(n) ? n : n.toFixed(1).replace(/\.0$/, '')}M`;
}

/** 이적료 칸에 보여줄 글자 */
export function feeText(t) {
  const fee = formatFee(t.feeMillionEur);
  if (fee) return fee;
  if (t.type === 'FREE') return '이적료 없음';
  if (t.type === 'LOAN') return '임대료 비공개';
  if (t.type === 'LOAN_RETURN') return '-';
  return '비공개';
}

/** "2026-07-25" → "7.25" */
export const shortDate = (d) => (d ? `${Number(d.slice(5, 7))}.${Number(d.slice(8, 10))}` : '');

/** "2031-06-30" → "2031년 6월까지" */
export const untilText = (d) => (d ? `${d.slice(0, 4)}년 ${Number(d.slice(5, 7))}월까지` : '');
