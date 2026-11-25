import { Link, useSearchParams } from 'react-router-dom';
import { api } from '../api/api';
import CareerSection from '../components/CareerSection';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';

const dateText = (d) => (d ? `${d.slice(0, 4)}.${d.slice(5, 7)}.${d.slice(8, 10)}` : null);
const ageOf = (d) => {
  if (!d) return null;
  const b = new Date(d); const n = new Date();
  return n.getFullYear() - b.getFullYear() - (n < new Date(n.getFullYear(), b.getMonth(), b.getDate()) ? 1 : 0);
};

/** 해외파·이적시장 선수처럼 DB에 없는 선수: /career?name=손흥민&lang=ko&korea=true */
export default function CareerPage() {
  const [params] = useSearchParams();
  const name = params.get('name') || '';
  const lang = params.get('lang') || 'en';
  const korea = params.get('korea') === 'true';
  const { data, loading, error } = useAsync(
    () => (name ? api.career({ name, lang, korea: korea || undefined }) : Promise.resolve(null)), [name, lang, korea]);

  const current = data?.clubs?.filter((c) => c.current).at(-1);
  const displayName = data?.found ? (data.koreanName ?? data.name) : name;

  return (
    <div className="stack page-narrow">
      <StateBox loading={loading && !data} error={error}>
        <section className="card player-hero">
          <div className="player-hero__main">
            {data?.positions?.length > 0 && <span className="muted small">{data.positions.join(' · ')}</span>}
            <h1 className="h1">{displayName}</h1>
            {data?.found && data.koreanName && data.name && <p className="muted">{data.name}</p>}
            {data?.found && (
              <dl className="team-facts">
                {data.nationality && <div><dt>국적</dt><dd>{data.nationality}</dd></div>}
                {data.birthDate && <div><dt>생년월일</dt><dd>{dateText(data.birthDate)} ({ageOf(data.birthDate)}세)</dd></div>}
                {data.heightCm && <div><dt>키</dt><dd>{data.heightCm}cm</dd></div>}
                {current && <div><dt>소속팀</dt><dd>{current.team}</dd></div>}
              </dl>
            )}
          </div>
          <div className="team-hero__links">
            <Link to={`/transfers?window=all&q=${encodeURIComponent(name)}`} className="btn btn--ghost btn--sm">이적 기록</Link>
            {korea && <Link to="/overseas" className="btn btn--ghost btn--sm">해외파 지도</Link>}
            <Link to={`/news?q=${encodeURIComponent(displayName)}`} className="btn btn--ghost btn--sm">관련 뉴스</Link>
          </div>
        </section>
        <CareerSection data={data} loading={loading} error={error} />
      </StateBox>
    </div>
  );
}
