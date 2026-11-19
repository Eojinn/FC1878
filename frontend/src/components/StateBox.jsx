/** 로딩/에러/빈 상태 공통 표시 */
export default function StateBox({ loading, error, empty, emptyText = '데이터가 없어요.', children }) {
  if (loading) return <div className="state">불러오는 중…</div>;
  if (error) return <div className="state state--error">{error.message || '불러오지 못했어요.'}</div>;
  if (empty) return <div className="state">{emptyText}</div>;
  return children;
}
