import { Link, useParams } from 'react-router-dom';
import { api } from '../api/api';
import CareerSection from '../components/CareerSection';
import Emblem from '../components/Emblem';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';
import { POSITION_LABEL } from '../utils/countries';
import { TRANSFER_TYPE } from '../utils/transfer';

const dateText = (d) => (d ? `${d.slice(0, 4)}.${d.slice(5, 7)}.${d.slice(8, 10)}` : '-');

export default function PlayerPage() {
  const { id } = useParams();
  const { data: p, loading, error } = useAsync(() => api.player(id), [id]);
  // 커리어는 외부(Wikidata)에서 가져와서 느릴 수 있으니 따로 불러옴
  const career = useAsync(() => api.playerCareer(id), [id]);
  const total = (p?.stats ?? []).reduce((acc, s) => ({
    played: acc.played + (s.played ?? 0), goals: acc.goals + s.goals, assists: acc.assists + (s.assists ?? 0),
  }), { played: 0, goals: 0, assists: 0 });
  const teamName = p?.team?.name ?? p?.teamName;
  const teamLogo = p?.team?.logoUrl ?? p?.teamLogoUrl;

  return (
    <div className="stack page-narrow">
      <StateBox loading={loading} error={error}>
        {p && (
          <>
            <section className="card player-hero">
              <span className={'player-hero__num num'} aria-label="등번호">{p.number ?? '-'}</span>
              <div className="player-hero__main">
                <span className={'pos-badge pos-badge--' + p.role}>{p.role}</span>
                <h1 className="h1">{p.koreanName ?? career.data?.koreanName ?? p.name}</h1>
                {(p.koreanName || career.data?.koreanName) && <p className="muted">{p.name}</p>}
                <dl className="team-facts">
                  <div><dt>포지션</dt><dd>{POSITION_LABEL[p.role]}{p.position && p.position !== POSITION_LABEL[p.role] ? ` · ${p.position}` : ''}</dd></div>
                  <div><dt>국적</dt><dd>{p.nationality ?? '-'}</dd></div>
                  <div><dt>생년월일</dt><dd>{dateText(p.dateOfBirth)}{p.age != null ? ` (${p.age}세)` : ''}</dd></div>
                  {career.data?.heightCm && <div><dt>키</dt><dd>{career.data.heightCm}cm</dd></div>}
                </dl>
              </div>
              {teamName && (
                p.team ? (
                  <Link to={`/teams/${p.team.id}`} className="player-hero__team">
                    <Emblem src={teamLogo} name={teamName} size={44} />
                    <span><span className="muted small">소속팀</span><strong>{teamName}</strong></span>
                  </Link>
                ) : (
                  <span className="player-hero__team">
                    <Emblem src={teamLogo} name={teamName} size={44} />
                    <span><span className="muted small">소속팀</span><strong>{teamName}</strong></span>
                  </span>
                )
              )}
            </section>

            <section className="card player-stats" aria-label="대회별 기록">
              <h2 className="h3">대회별 기록</h2>
              {p.stats.length ? (
                <>
                  <div className="player-totals">
                    <div><span className="muted small">출전</span><strong className="num big">{total.played}</strong></div>
                    <div><span className="muted small">골</span><strong className="num big">{total.goals}</strong></div>
                    <div><span className="muted small">도움</span><strong className="num big">{total.assists}</strong></div>
                  </div>
                  <table className="squad-table">
                    <thead><tr><th className="left">대회</th><th>시즌</th><th>경기</th><th>골</th><th>도움</th><th>페널티 골</th></tr></thead>
                    <tbody>
                      {p.stats.map((s) => (
                        <tr key={`${s.league.id}-${s.season}`}>
                          <td className="left"><span className="team-cell"><Emblem src={s.league.logoUrl} name={s.league.name} size={20} light />{s.league.name}</span></td>
                          <td className="num muted">{s.season}-{String(s.season + 1).slice(2)}</td>
                          <td className="num">{s.played ?? '-'}</td>
                          <td className="num"><strong>{s.goals}</strong></td>
                          <td className="num">{s.assists ?? '-'}</td>
                          <td className="num">{s.penalties ?? '-'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </>
              ) : (
                <p className="muted small">아직 기록이 없어요. 대회별 득점 순위(상위 100명)에 들면 골·도움·출전 기록이 표시돼요.</p>
              )}
            </section>

            <CareerSection data={career.data} loading={career.loading} error={career.error} />

            {p.transfers.length > 0 && (
              <section className="card player-stats" aria-label="최근 이적">
                <h2 className="h3">최근 이적</h2>
                <ul className="player-transfers">
                  {p.transfers.map((t) => (
                    <li key={`${t.date}-${t.toClub}`}>
                      <span className="muted small num">{dateText(t.date)}</span>
                      <span>{t.fromClub} <span className="tr-arrow">→</span> <strong>{t.toClub}</strong></span>
                      <span className={'tr-type tr-type--' + (TRANSFER_TYPE[t.type]?.cls ?? 'permanent')}>{TRANSFER_TYPE[t.type]?.label}</span>
                      {t.feeText && <strong className="num">{t.feeText}</strong>}
                    </li>
                  ))}
                </ul>
              </section>
            )}
          </>
        )}
      </StateBox>
    </div>
  );
}
