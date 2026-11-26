import { Link } from 'react-router-dom';
import { hhmm, roundLabel } from '../utils/format';
import Emblem from './Emblem';

function statusOf(f) {
  if (f.status === 'LIVE') return { time: `${f.elapsed ?? ''}'`, tag: 'LIVE', cls: 'tag--live', live: true };
  if (f.status === 'FINISHED') return { time: hhmm(f.kickoffAt), tag: '종료', cls: 'tag--done' };
  if (f.status === 'POSTPONED') return { time: hhmm(f.kickoffAt), tag: '연기', cls: 'tag--done' };
  if (f.status === 'CANCELLED') return { time: hhmm(f.kickoffAt), tag: '취소', cls: 'tag--done' };
  return { time: hhmm(f.kickoffAt), tag: '예측하기', cls: 'tag--pick' };
}

function score(f) {
  if (f.homeScore == null) return 'vs';
  const pen = f.homePenalty != null ? ` (${f.homePenalty}-${f.awayPenalty})` : '';
  return `${f.homeScore} : ${f.awayScore}${pen}`;
}

/** 리그별로 묶인 경기 목록 (GET /api/fixtures 응답 그대로) */
export default function FixtureGroups({ groups }) {
  return groups.map((g) => (
    <div className="card fixture-group" key={g.league.id}>
      <div className="fixture-group__head">
        <Emblem src={g.league.logoUrl} name={g.league.name} size={22} light />
        <strong>{g.league.name}</strong>
        <span className="muted">{roundLabel(g.fixtures[0]?.round)}</span>
      </div>
      {g.fixtures.map((f) => {
        const st = statusOf(f);
        return (
          <Link to={`/fixtures/${f.id}`} className="fixture-row" key={f.id}>
            <span className={'fixture-row__time' + (st.live ? ' is-live' : '')}>{st.time}</span>
            <span className="fixture-row__team is-home">
              <span className="ellipsis">{f.home.name}</span>
              <Emblem src={f.home.logoUrl} name={f.home.name} size={26} />
            </span>
            <span className="fixture-row__score">{score(f)}</span>
            <span className="fixture-row__team">
              <Emblem src={f.away.logoUrl} name={f.away.name} size={26} />
              <span className="ellipsis">{f.away.name}</span>
            </span>
            <span className={'tag ' + st.cls}>{st.tag}</span>
          </Link>
        );
      })}
    </div>
  ));
}
