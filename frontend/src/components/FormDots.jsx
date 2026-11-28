/**
 * 최근 경기 결과 "WWDLW" → 승·무·패 칸 (왼쪽이 오래된 경기, 오른쪽이 가장 최근 경기)
 * size="sm" 이면 경기일정 목록용 작은 점
 */
export default function FormDots({ form, size }) {
  if (!form) return size === 'sm' ? null : <span className="muted">-</span>;
  const label = { W: '승', D: '무', L: '패' };
  return (
    <span className={'form-dots' + (size === 'sm' ? ' form-dots--sm' : '')}
          title={`최근 ${form.length}경기: ${form.split('').map((c) => label[c] || c).join(' ')} (오른쪽이 가장 최근)`}
          aria-label={`최근 ${form.length}경기 ${form.split('').map((c) => label[c] || c).join(' ')}`}>
      {form.split('').map((c, i) => (
        <span key={i} className={'form__dot form__dot--' + c}>{size === 'sm' ? '' : label[c] || c}</span>
      ))}
    </span>
  );
}
