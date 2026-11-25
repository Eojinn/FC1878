import { useState } from 'react';
import { api } from '../api/api';
import { useAsync } from '../hooks/useAsync';
import { useLiveScores } from '../hooks/useLiveScores';
import { dateTabs, kstToday } from '../utils/format';
import FixtureGroups from './FixtureGroups';
import StateBox from './StateBox';

/** 날짜 탭 + 리그 필터 + 리그별 경기 목록 */
export default function FixtureBoard({ leagues = [], onlyScheduled = false, emptyText, initialDate }) {
  const tabs = dateTabs();
  const [date, setDate] = useState(/^\d{4}-\d{2}-\d{2}$/.test(initialDate || '') ? initialDate : kstToday());
  const [leagueId, setLeagueId] = useState(null);
  const { data, error, loading, setData } = useAsync(() => api.fixtures({ date, leagueId }), [date, leagueId]);

  // 실시간 스코어 반영
  useLiveScores((updated) => {
    setData((groups) => groups?.map((g) => ({
      ...g, fixtures: g.fixtures.map((f) => (f.id === updated.id ? updated : f)),
    })));
  });

  const groups = (data || [])
    .map((g) => ({ ...g, fixtures: onlyScheduled ? g.fixtures.filter((f) => f.status === 'SCHEDULED') : g.fixtures }))
    .filter((g) => g.fixtures.length > 0);

  return (
    <div className="stack">
      <nav className="date-tabs" aria-label="날짜 선택">
        {tabs.map((t) => (
          <button key={t.iso} type="button" className={'date-tab' + (t.iso === date ? ' is-active' : '')}
                  aria-pressed={t.iso === date} onClick={() => setDate(t.iso)}>
            <span className="date-tab__label">{t.label}</span>
            <span className="date-tab__date">{t.short}</span>
          </button>
        ))}
      </nav>

      <div className="chips">
        <button type="button" className={'chip' + (leagueId == null ? ' is-active' : '')} onClick={() => setLeagueId(null)}>전체</button>
        {leagues.map((l) => (
          <button key={l.id} type="button" className={'chip' + (leagueId === l.id ? ' is-active' : '')}
                  onClick={() => setLeagueId(l.id)}>{l.name}</button>
        ))}
      </div>

      <StateBox loading={loading} error={error} empty={groups.length === 0}
                emptyText={emptyText || '이 날짜에는 경기가 없어요.'}>
        <FixtureGroups groups={groups} />
      </StateBox>
    </div>
  );
}
