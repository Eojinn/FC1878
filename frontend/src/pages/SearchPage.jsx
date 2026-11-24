import { Link, useSearchParams } from 'react-router-dom';
import { api } from '../api/api';
import Emblem from '../components/Emblem';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';

const ROLE_LABEL = { GK: '골키퍼', DF: '수비수', MF: '미드필더', FW: '공격수' };
const TYPE_LABEL = { OVERSEAS: '해외파', TRANSFER: '이적시장', SQUAD: '스쿼드' };

/** 팀·선수 검색 결과 (/search?q=) */
export default function SearchPage() {
  const [params] = useSearchParams();
  const q = (params.get('q') || '').trim();
  const { data, loading, error } = useAsync(() => (q ? api.search(q, 30) : Promise.resolve(null)), [q]);
  const teams = data?.teams ?? [];
  const players = data?.players ?? [];

  return (
    <div className="stack page-narrow">
      <div>
        <h1 className="h1">검색</h1>
        <p className="muted">{q ? <>‘{q}’ 팀 <strong className="num">{teams.length}</strong> · 선수 <strong className="num">{players.length}</strong></> : '위 검색창에 팀이나 선수 이름을 입력하세요.'}</p>
      </div>

      {q && (
        <StateBox loading={loading} error={error} empty={!teams.length && !players.length}
                  emptyText="맞는 팀·선수가 없어요. 영문(예: Son Heung-min, Saka)이나 한글(예: 손흥민, 사카)로 바꿔서 찾아보세요.">
          {teams.length > 0 && (
            <section className="card search-section" aria-label="팀">
              <h2 className="h3">팀</h2>
              <ul className="search-grid">
                {teams.map((t) => (
                  <li key={t.id}>
                    <Link to={t.link} className="search-team">
                      <Emblem src={t.logoUrl} name={t.name} size={36} />
                      <span className="search-team__text">
                        <strong>{t.name}</strong>
                        <span className="muted small">{t.subName ? `${t.subName} · ` : ''}경기일정 보기</span>
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>
            </section>
          )}

          {players.length > 0 && (
            <section className="card search-section" aria-label="선수">
              <h2 className="h3">선수</h2>
              <ul className="search-players">
                {players.map((p, i) => {
                  const body = (
                    <>
                      <span className={'pos-badge pos-badge--' + p.role} title={ROLE_LABEL[p.role]}>{p.role}</span>
                      <span className="search-player__text">
                        <strong>{p.name}{p.nameEn && <span className="hsearch__en">{p.nameEn}</span>}</strong>
                        <span className="muted small">{[p.nationality, p.detail].filter(Boolean).join(' · ')}</span>
                      </span>
                      <span className="search-player__team">
                        {p.teamName && <Emblem src={p.teamLogoUrl} name={p.teamName} size={22} />}
                        <span className="small">{p.teamName}</span>
                      </span>
                      <span className="search-player__type small">{TYPE_LABEL[p.type]}</span>
                    </>
                  );
                  return (
                    <li key={`${p.type}-${p.name}-${i}`}>
                      {p.link ? <Link to={p.link} className="search-player">{body}</Link> : <div className="search-player">{body}</div>}
                    </li>
                  );
                })}
              </ul>
            </section>
          )}
        </StateBox>
      )}
      <p className="muted small">게시글을 찾으려면 커뮤니티 페이지의 검색을 이용하세요.</p>
    </div>
  );
}
