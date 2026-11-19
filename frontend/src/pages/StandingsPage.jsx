import { useSearchParams } from 'react-router-dom';
import { api } from '../api/api';
import Emblem from '../components/Emblem';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';
import { useLeagues } from '../hooks/useLeagues';

export default function StandingsPage() {
  const { leagues } = useLeagues();
  const [params, setParams] = useSearchParams();
  const leagueId = Number(params.get('league')) || leagues[0]?.id;
  const league = leagues.find((l) => l.id === leagueId);
  const { data, loading, error } = useAsync(
    () => (leagueId ? api.standings(leagueId) : Promise.resolve([])), [leagueId]);

  return (
    <div className="stack page-narrow">
      <h1 className="h1">순위</h1>
      <div className="chips">
        {leagues.map((l) => (
          <button key={l.id} type="button" className={'chip chip--emblem' + (l.id === leagueId ? ' is-active' : '')}
                  onClick={() => setParams({ league: l.id })}>
            <Emblem src={l.logoUrl} name={l.name} size={20} />{l.name}
          </button>
        ))}
      </div>
      <section className="card table-wrap" aria-label={`${league?.name ?? ''} 순위표`}>
        <StateBox loading={loading} error={error} empty={!data?.length} emptyText="순위 정보가 아직 없어요.">
          <table className="table">
            <thead>
              <tr><th>#</th><th className="left">팀</th><th>경기</th><th>승</th><th>무</th><th>패</th><th>득실</th><th>승점</th><th>최근 5경기</th></tr>
            </thead>
            <tbody>
              {data?.map((s) => (
                <tr key={s.team.id}>
                  <td className="rank-num">{s.rank}</td>
                  <td className="left"><span className="team-cell"><Emblem src={s.team.logoUrl} name={s.team.name} size={24} />{s.team.name}</span></td>
                  <td>{s.played}</td><td>{s.win}</td><td>{s.draw}</td><td>{s.lose}</td>
                  <td>{s.goalDiff > 0 ? `+${s.goalDiff}` : s.goalDiff}</td>
                  <td><strong className="num">{s.points}</strong></td>
                  <td>
                    <span className="form-dots">
                      {(s.form || '').split('').map((c, i) => (
                        <span key={i} className={'form__dot form__dot--' + c}>{{ W: '승', D: '무', L: '패' }[c] || c}</span>
                      ))}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </StateBox>
      </section>
    </div>
  );
}
