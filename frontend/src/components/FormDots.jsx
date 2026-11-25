/** 최근 경기 결과 "WWDLW" → 승·무·패 칸 */
export default function FormDots({ form }) {
  if (!form) return <span className="muted">-</span>;
  return (
    <span className="form-dots" aria-label={`최근 경기 ${form}`}>
      {form.split('').map((c, i) => (
        <span key={i} className={'form__dot form__dot--' + c}>{{ W: '승', D: '무', L: '패' }[c] || c}</span>
      ))}
    </span>
  );
}
