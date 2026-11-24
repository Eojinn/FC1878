import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/api';
import StateBox from '../components/StateBox';
import WorldMap from '../components/WorldMap';
import { useAsync } from '../hooks/useAsync';
import { MAP_VIEWS, POSITION_LABEL } from '../utils/countries';

const cityKey = (p) => `${p.countryCode}|${p.city}`;

/** 나라별로 묶고, 선수가 많은 나라부터 */
function groupByCountry(players) {
  const map = new Map();
  for (const p of players) {
    if (!map.has(p.countryCode)) map.set(p.countryCode, { code: p.countryCode, name: p.countryName, players: [] });
    map.get(p.countryCode).players.push(p);
  }
  return [...map.values()].sort((a, b) => b.players.length - a.players.length);
}

const formatAsOf = (d) => {
  if (!d) return '';
  const [y, m, day] = d.split('-');
  return `${y}.${m}.${day} 기준`;
};

export default function OverseasPage() {
  const { data, loading, error } = useAsync(() => api.overseas(), []);
  const [view, setView] = useState('world');
  const [selected, setSelected] = useState(null);

  const players = data?.players ?? [];
  const countries = useMemo(() => groupByCountry(players), [players]);
  const filtered = useMemo(() => {
    if (!selected) return players;
    if (selected.type === 'city') return players.filter((p) => selected.cityKeys.includes(cityKey(p)));
    return players.filter((p) => p.countryCode === selected.code);
  }, [players, selected]);
  const filteredCountries = useMemo(() => groupByCountry(filtered), [filtered]);

  const toggle = (next) =>
    setSelected((cur) => (cur && cur.type === next.type && (cur.key ?? cur.code) === (next.key ?? next.code) ? null : next));
  const selectedLabel = selected?.type === 'city'
    ? selected.label
    : countries.find((c) => c.code === selected?.code)?.name;

  return (
    <div className="stack">
      <div className="overseas-head">
        <div>
          <h1 className="h1">해외파</h1>
          {data && (
            <p className="muted">
              해외 리그에서 뛰는 한국 선수 <strong className="num">{players.length}</strong>명 ·{' '}
              <strong className="num">{countries.length}</strong>개 나라 <span className="small">({formatAsOf(data.asOf)})</span>
            </p>
          )}
        </div>
        <div className="chips" role="group" aria-label="지도 보기 범위">
          {MAP_VIEWS.map((v) => (
            <button key={v.key} type="button" className={'chip' + (v.key === view ? ' is-active' : '')}
                    aria-pressed={v.key === view} onClick={() => setView(v.key)}>{v.label}</button>
          ))}
        </div>
      </div>

      <StateBox loading={loading} error={error} empty={!players.length} emptyText="등록된 해외파 선수가 없어요.">
        <section className="card overseas-map">
          <WorldMap players={players} view={view} selected={selected} onSelect={toggle} />
          <p className="muted small overseas-map__hint">도시 마커나 색칠된 나라를 누르면 아래 목록이 그곳 선수만 보여요.</p>
        </section>

        <div className="chips" role="group" aria-label="나라별 보기">
          <button type="button" className={'chip' + (!selected ? ' is-active' : '')} onClick={() => setSelected(null)}>
            전체 <span className="chip__count">{players.length}</span>
          </button>
          {countries.map((c) => {
            const on = selected?.type === 'country' && selected.code === c.code;
            return (
              <button key={c.code} type="button" className={'chip' + (on ? ' is-active' : '')} aria-pressed={on}
                      onClick={() => toggle({ type: 'country', code: c.code })}>
                {c.name} <span className="chip__count">{c.players.length}</span>
              </button>
            );
          })}
        </div>

        {selected && (
          <p className="overseas-filter">
            <strong>{selectedLabel}</strong> 선수 {filtered.length}명
            <button type="button" className="btn btn--ghost btn--sm" onClick={() => setSelected(null)}>전체 보기</button>
          </p>
        )}

        <div className="overseas-groups">
          {filteredCountries.map((c) => (
            <section key={c.code} className="card overseas-group" aria-label={`${c.name} 해외파`}>
              <h2 className="h3">{c.name} <span className="muted num">{c.players.length}</span></h2>
              <ul className="overseas-list">
                {c.players.map((p) => (
                  <li key={p.id} className="overseas-player">
                    <span className={'pos-badge pos-badge--' + p.position} title={POSITION_LABEL[p.position]}>{p.position}</span>
                    <div className="overseas-player__body">
                      <Link to={p.nameEn ? `/career?name=${encodeURIComponent(p.nameEn)}&lang=en&korea=true` : `/career?name=${encodeURIComponent(p.name)}&lang=ko&korea=true`}
                            className="player-link"><strong>{p.name}</strong>{p.nameEn && <span className="hsearch__en">{p.nameEn}</span>}</Link>
                      <span className="muted small">
                        {p.club} · {p.league}
                        {p.onLoan && <> · <span className="loan-tag">{p.parentClub}에서 임대</span></>}
                      </span>
                    </div>
                  </li>
                ))}
              </ul>
            </section>
          ))}
        </div>
      </StateBox>
    </div>
  );
}
