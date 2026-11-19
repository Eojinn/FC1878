import { useParams } from 'react-router-dom';
import { api } from '../api/api';
import Emblem from '../components/Emblem';
import PredictionCard from '../components/PredictionCard';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';
import { useLiveScores } from '../hooks/useLiveScores';
import { kickoffLabel, roundLabel } from '../utils/format';

const STATUS = { SCHEDULED: '경기 전', LIVE: '진행 중', FINISHED: '경기 종료', POSTPONED: '연기', CANCELLED: '취소' };

export default function FixtureDetailPage() {
  const { id } = useParams();
  const { data: f, loading, error, setData } = useAsync(() => api.fixture(id), [id]);
  useLiveScores((u) => { if (String(u.id) === id) setData(u); });

  return (
    <div className="stack-lg page-narrow">
      <StateBox loading={loading} error={error}>
        {f && (
          <>
            <section className="card scoreboard">
              <p className="muted center">
                <Emblem src={f.league.logoUrl} name={f.league.name} size={20} /> {f.league.name} · {roundLabel(f.round)} · {kickoffLabel(f.kickoffAt)}
              </p>
              <div className="scoreboard__main">
                <div className="pick-card__team"><Emblem src={f.home.logoUrl} name={f.home.name} size={88} /><strong>{f.home.name}</strong></div>
                <div className="scoreboard__score">
                  <span>{f.homeScore ?? '-'} : {f.awayScore ?? '-'}</span>
                  <small className={f.status === 'LIVE' ? 'is-live' : 'muted'}>
                    {f.status === 'LIVE' ? `${f.elapsed ?? ''}' 진행 중` : STATUS[f.status]}
                  </small>
                  {f.homePenalty != null && <small className="muted">승부차기 {f.homePenalty} - {f.awayPenalty}</small>}
                </div>
                <div className="pick-card__team"><Emblem src={f.away.logoUrl} name={f.away.name} size={88} /><strong>{f.away.name}</strong></div>
              </div>
            </section>
            <PredictionCard fixture={f} title="승부예측" large={false} />
          </>
        )}
      </StateBox>
    </div>
  );
}
