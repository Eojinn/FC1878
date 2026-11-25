import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../auth/AuthContext';
import { useAsync } from '../hooks/useAsync';
import { kickoffLabel, num, roundLabel } from '../utils/format';
import Emblem from './Emblem';

/** 홈/원정 승·무 비율 + 예측 버튼 카드 */
export default function PredictionCard({ fixture, title = '오늘의 승부예측', large = true }) {
  const { user, refresh } = useAuth();
  const navigate = useNavigate();
  const { data: stats, setData: setStats } = useAsync(() => api.predictionStats(fixture.id), [fixture.id, user?.id]);
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);

  const open = fixture.status === 'SCHEDULED' && new Date(fixture.kickoffAt + '+09:00') > new Date();
  const s = stats || { total: 0, homePercent: 0, drawPercent: 0, awayPercent: 0, myPick: null };

  const pick = async (p) => {
    if (!user) { navigate('/login', { state: { from: window.location.pathname } }); return; }
    setError(''); setSending(true);
    try { setStats(await api.predict(fixture.id, p)); refresh(); }
    catch (e) { setError(e.message); }
    finally { setSending(false); }
  };

  const options = [
    ['HOME', `${fixture.home.name} 승`],
    ['DRAW', '무승부'],
    ['AWAY', `${fixture.away.name} 승`],
  ];

  return (
    <section className={'card pick-card' + (large ? ' pick-card--large' : '')} aria-label={title}>
      <div className="pick-card__head">
        <div>
          <h2 className="h2">{title}</h2>
          <p className="muted">
            {fixture.league.name} · {roundLabel(fixture.round)} · {kickoffLabel(fixture.kickoffAt)} 킥오프
          </p>
        </div>
        <span className="badge-soft">적중 시 +10P</span>
      </div>

      <div className="pick-card__teams">
        <div className="pick-card__team">
          <Emblem src={fixture.home.logoUrl} name={fixture.home.name} size={large ? 80 : 56} />
          <strong>{fixture.home.name}</strong>
        </div>
        <div className="pick-card__vs">
          <span>VS</span>
          <small className="muted">참여 {num(s.total)}명</small>
        </div>
        <div className="pick-card__team">
          <Emblem src={fixture.away.logoUrl} name={fixture.away.name} size={large ? 80 : 56} />
          <strong>{fixture.away.name}</strong>
        </div>
      </div>

      <div>
        <div className="ratio" role="img"
             aria-label={`홈 승 ${s.homePercent}%, 무승부 ${s.drawPercent}%, 원정 승 ${s.awayPercent}%`}>
          {s.total === 0 ? <span className="ratio__empty" /> : (
            <>
              <span className="ratio__home" style={{ width: `${s.homePercent}%` }} />
              <span className="ratio__draw" style={{ width: `${s.drawPercent}%` }} />
              <span className="ratio__away" style={{ width: `${s.awayPercent}%` }} />
            </>
          )}
        </div>
        <div className="ratio__labels">
          <span>홈 {s.homePercent}%</span><span className="muted">무 {s.drawPercent}%</span><span>원정 {s.awayPercent}%</span>
        </div>
      </div>

      <div className="pick-card__buttons">
        {options.map(([key, label]) => (
          <button key={key} type="button" disabled={!open || sending} aria-pressed={s.myPick === key}
                  className={'pick-btn' + (s.myPick === key ? ' is-picked' : '')} onClick={() => pick(key)}>
            {label}
          </button>
        ))}
      </div>
      {!open && <p className="muted center">예측이 마감된 경기예요.</p>}
      {!user && open && <p className="muted center">로그인하면 예측에 참여할 수 있어요.</p>}
      {error && <p className="error-text">{error}</p>}
    </section>
  );
}
