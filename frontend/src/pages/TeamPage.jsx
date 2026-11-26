import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/api';
import Emblem from '../components/Emblem';
import FormDots from '../components/FormDots';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';
import { POSITION_LABEL } from '../utils/countries';
import { kickoffLabel } from '../utils/format';

const ROLES = ['GK', 'DF', 'MF', 'FW'];
const SORTS = [
  { key: 'position', label: '포지션순' },
  { key: 'goals', label: '골' },
  { key: 'assists', label: '도움' },
];

/** 이 팀 기준 결과: 승·무·패 */
function resultOf(f, teamId) {
  if (f.homeScore == null) return null;
  const mine = f.home.id === teamId ? f.homeScore : f.awayScore;
  const theirs = f.home.id === teamId ? f.awayScore : f.homeScore;
  return mine > theirs ? 'W' : mine < theirs ? 'L' : 'D';
}

function MatchList({ title, fixtures, teamId, empty }) {
  return (
    <section className="card team-matches" aria-label={title}>
      <h2 className="h3">{title}</h2>
      {!fixtures.length && <p className="muted small">{empty}</p>}
      <ul>
        {fixtures.map((f) => {
          const home = f.home.id === teamId;
          const opp = home ? f.away : f.home;
          const r = resultOf(f, teamId);
          return (
            <li key={f.id}>
              <Link to={`/fixtures/${f.id}`} className="team-match">
                <span className="muted small team-match__date">{kickoffLabel(f.kickoffAt)}</span>
                <span className="team-match__league muted small">{f.league.name}</span>
                <span className="team-match__opp">
                  <span className="muted small">{home ? '홈' : '원정'}</span>
                  <Emblem src={opp.logoUrl} name={opp.name} size={22} />
                  <strong className="ellipsis">{opp.name}</strong>
                </span>
                <span className="team-match__score num">
                  {f.homeScore == null ? (f.status === 'LIVE' ? 'LIVE' : 'vs') : `${home ? f.homeScore : f.awayScore} : ${home ? f.awayScore : f.homeScore}`}
                </span>
                {r && <span className={'form__dot form__dot--' + r}>{{ W: '승', D: '무', L: '패' }[r]}</span>}
              </Link>
            </li>
          );
        })}
      </ul>
    </section>
  );
}

export default function TeamPage() {
  const { id } = useParams();
  const teamId = Number(id);
  const { data, loading, error } = useAsync(() => api.teamProfile(id), [id]);
  const [sort, setSort] = useState('position');

  const squad = useMemo(() => data?.squad ?? [], [data]);
  const hasStats = squad.some((p) => p.goals != null || p.played != null);
  const leaders = useMemo(() => {
    const pick = (key) => [...squad].filter((p) => (p[key] ?? 0) > 0).sort((a, b) => b[key] - a[key]).slice(0, 3);
    return { goals: pick('goals'), assists: pick('assists') };
  }, [squad]);
  const sorted = sort === 'position' ? squad
    : [...squad].sort((a, b) => (b[sort] ?? -1) - (a[sort] ?? -1));

  const t = data?.team;
  const info = data?.info;

  return (
    <div className="stack">
      <StateBox loading={loading} error={error}>
        {t && (
          <>
            <section className="card team-hero">
              <Emblem src={t.logoUrl} name={t.name} size={84} />
              <div className="team-hero__main">
                <h1 className="h1">{t.name}</h1>
                {data.englishName && <p className="muted">{data.englishName}</p>}
                <dl className="team-facts">
                  {info?.coach && <div><dt>감독</dt><dd>{info.coach}{info.coachNationality ? ` (${info.coachNationality})` : ''}</dd></div>}
                  {info?.venue && <div><dt>홈구장</dt><dd>{info.venue}</dd></div>}
                  {info?.founded && <div><dt>창단</dt><dd>{info.founded}년</dd></div>}
                  {info?.clubColors && <div><dt>클럽 컬러</dt><dd>{info.clubColors}</dd></div>}
                  {info?.website && <div><dt>홈페이지</dt><dd><a href={info.website} target="_blank" rel="noopener noreferrer">{info.website.replace(/^https?:\/\//, '')}</a></dd></div>}
                </dl>
                {!info && <p className="muted small">감독·경기장 같은 구단 정보는 football-data.org 토큰이 있을 때 보여요.</p>}
              </div>
              <div className="team-hero__links">
                <Link to={`/fixtures?team=${t.id}`} className="btn btn--ghost btn--sm">전체 경기일정</Link>
                <Link to="/tacticboard" className="btn btn--ghost btn--sm">전술판에서 보기</Link>
              </div>
            </section>

            <div className="team-grid">
              <div className="stack">
                {data.standings.map((s) => (
                  <section key={s.league.id} className="card team-standing" aria-label={`${s.league.name} 순위`}>
                    <div className="team-standing__head">
                      <Emblem src={s.league.logoUrl} name={s.league.name} size={22} light />
                      <strong>{s.league.name}</strong>
                      <span className="muted small">{s.season}-{String(s.season + 1).slice(2)} 시즌</span>
                      <Link to="/standings" className="link-btn">순위표</Link>
                    </div>
                    <div className="team-standing__stats">
                      <div><span className="muted small">순위</span><strong className="num big">{s.rank}위</strong></div>
                      <div><span className="muted small">승점</span><strong className="num big">{s.points}</strong></div>
                      <div><span className="muted small">경기</span><strong className="num">{s.played}</strong></div>
                      <div><span className="muted small">승 · 무 · 패</span><strong className="num">{s.win} · {s.draw} · {s.lose}</strong></div>
                      <div><span className="muted small">득점 · 실점</span><strong className="num">{s.goalsFor} · {s.goalsAgainst}</strong></div>
                      <div><span className="muted small">득실</span><strong className="num">{s.goalDiff > 0 ? `+${s.goalDiff}` : s.goalDiff}</strong></div>
                    </div>
                    <div className="team-standing__form"><span className="muted small">최근 5경기</span><FormDots form={s.form} /></div>
                  </section>
                ))}
                {!data.standings.length && <section className="card team-standing"><p className="muted small">리그 순위 기록이 없어요.</p></section>}

                <MatchList title="최근 경기" fixtures={data.recent} teamId={teamId} empty="최근 경기 기록이 없어요." />
                <MatchList title="다음 경기" fixtures={data.upcoming} teamId={teamId} empty="예정된 경기가 없어요." />
              </div>

              <div className="stack">
                {(leaders.goals.length > 0 || leaders.assists.length > 0) && (
                  <section className="card team-leaders" aria-label="팀 내 기록 순위">
                    <h2 className="h3">팀 내 기록 <span className="muted small">{data.statSeason}-{String(data.statSeason + 1).slice(2)} 시즌 · 리그+챔스</span></h2>
                    <div className="team-leaders__cols">
                      {[['goals', '골'], ['assists', '도움']].map(([k, label]) => (
                        <div key={k}>
                          <span className="muted small">{label}</span>
                          <ol>
                            {leaders[k].map((p) => (
                              <li key={p.name}>
                                {p.playerId ? <Link to={`/players/${p.playerId}`}>{p.name}</Link> : p.name}
                                <strong className="num">{p[k]}</strong>
                              </li>
                            ))}
                            {!leaders[k].length && <li className="muted small">기록 없음</li>}
                          </ol>
                        </div>
                      ))}
                    </div>
                  </section>
                )}

                <section className="card team-squad" aria-label="선수단">
                  <div className="team-squad__head">
                    <h2 className="h3">선수단 <span className="muted small num">{squad.length}명</span></h2>
                    {hasStats && (
                      <div className="chips">
                        {SORTS.map((s) => (
                          <button key={s.key} type="button" className={'chip chip--sm' + (sort === s.key ? ' is-active' : '')}
                                  onClick={() => setSort(s.key)}>{s.label}</button>
                        ))}
                      </div>
                    )}
                  </div>
                  {!squad.length && <p className="muted small">선수단 정보가 아직 없어요. football-data.org 토큰이 있으면 하루에 한 번 자동으로 채워져요.</p>}
                  {squad.length > 0 && (
                    <table className="squad-table">
                      <thead>
                        <tr><th>번호</th><th className="left">선수</th><th className="left">국적</th><th>나이</th>
                          <th title="출전 경기">경기</th><th>골</th><th>도움</th></tr>
                      </thead>
                      {(sort === 'position' ? ROLES : ['ALL']).map((role) => {
                        const rows = role === 'ALL' ? sorted : sorted.filter((p) => p.role === role);
                        if (!rows.length) return null;
                        return (
                          <tbody key={role}>
                            {role !== 'ALL' && <tr className="squad-table__group"><th colSpan={7} className="left">{POSITION_LABEL[role]}</th></tr>}
                            {rows.map((p) => (
                              <tr key={`${p.name}-${p.number}`}>
                                <td className="num muted">{p.number ?? '-'}</td>
                                <td className="left">
                                  <span className="squad-player">
                                    <span className={'pos-badge pos-badge--' + p.role}>{p.role}</span>
                                    {p.playerId ? <Link to={`/players/${p.playerId}`}>{p.name}</Link> : <span>{p.name}</span>}
                                    {p.koreanName && <span className="muted small">{p.koreanName}</span>}
                                  </span>
                                </td>
                                <td className="left muted small">{p.nationality ?? '-'}</td>
                                <td className="num">{p.age ?? '-'}</td>
                                <td className="num">{p.played ?? '-'}</td>
                                <td className="num"><strong>{p.goals ?? '-'}</strong></td>
                                <td className="num">{p.assists ?? '-'}</td>
                              </tr>
                            ))}
                          </tbody>
                        );
                      })}
                    </table>
                  )}
                  <p className="muted small">경기·골·도움은 football-data.org 득점 순위(대회별 상위 100명)에 든 선수만 나와요. ‘-’는 기록이 없다는 뜻이에요.</p>
                </section>
              </div>
            </div>
          </>
        )}
      </StateBox>
    </div>
  );
}
