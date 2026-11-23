import { geoNaturalEarth1, geoPath } from 'd3-geo';
import { useMemo, useState } from 'react';
import { feature } from 'topojson-client';
import world from 'world-atlas/countries-110m.json';
import { COUNTRY_MAP_ID, KOREA_MAP_ID, MAP_VIEWS, SEOUL } from '../utils/countries';

const WIDTH = 960;
const HEIGHT = 500;

// 남극은 빼고 그림
const COUNTRIES = feature(world, world.objects.countries).features.filter((f) => f.id !== '010');
const WORLD_SHAPE = { type: 'FeatureCollection', features: COUNTRIES };

/** 같은 도시에 있는 선수들을 마커 하나로 묶음 */
function groupByCity(players) {
  const map = new Map();
  for (const p of players) {
    const key = `${p.countryCode}|${p.city}`;
    if (!map.has(key)) map.set(key, { key, city: p.city, countryName: p.countryName, countryCode: p.countryCode,
      lng: p.longitude, lat: p.latitude, players: [] });
    map.get(key).players.push(p);
  }
  return [...map.values()];
}

/**
 * 화면에서 너무 가까운 도시(뮌헨·아우크스부르크, 글래스고 두 팀 등)는 마커 하나로 합침.
 * 확대 정도에 따라 겹치는 정도가 달라서, 좌표를 화면 위치로 바꾼 뒤에 묶는다.
 */
function clusterCities(cities, project, minGap) {
  const clusters = [];
  const sorted = [...cities].sort((a, b) => b.players.length - a.players.length);
  for (const c of sorted) {
    const pt = project(c.lng, c.lat);
    if (!pt) continue;
    const near = clusters.find((k) => Math.hypot(k.x - pt[0], k.y - pt[1]) < minGap);
    if (near) {
      const n = near.cities.length;
      near.x = (near.x * n + pt[0]) / (n + 1);
      near.y = (near.y * n + pt[1]) / (n + 1);
      near.cities.push(c);
      near.players.push(...c.players);
    } else {
      clusters.push({ x: pt[0], y: pt[1], cities: [c], players: [...c.players] });
    }
  }
  return clusters.map((k) => {
    const countryNames = [...new Set(k.cities.map((c) => c.countryName))];
    return {
      ...k,
      key: k.cities.map((c) => c.key).sort().join(','),
      cityKeys: k.cities.map((c) => c.key),
      label: `${countryNames.join('·')} · ${k.cities.map((c) => c.city).join('·')}`,
    };
  });
}

function makeProjection(viewKey) {
  const view = MAP_VIEWS.find((v) => v.key === viewKey) ?? MAP_VIEWS[0];
  const projection = geoNaturalEarth1();
  if (!view.bounds) return projection.fitExtent([[8, 8], [WIDTH - 8, HEIGHT - 8]], WORLD_SHAPE);
  const [[w, s], [e, n]] = view.bounds;
  return projection.fitExtent([[24, 24], [WIDTH - 24, HEIGHT - 24]],
    { type: 'MultiPoint', coordinates: [[w, s], [e, s], [e, n], [w, n], [(w + e) / 2, n], [(w + e) / 2, s]] });
}

/**
 * 해외파 세계 지도.
 * - 선수가 있는 나라는 빨간 톤으로 칠하고, 연고 도시마다 마커(숫자 = 선수 수)
 * - 마커·나라를 누르면 onSelect({ type: 'city' | 'country', ... })
 */
export default function WorldMap({ players, view, selected, onSelect }) {
  const [hover, setHover] = useState(null);

  const { path, project } = useMemo(() => {
    const projection = makeProjection(view);
    return { path: geoPath(projection), project: (lng, lat) => projection([lng, lat]) };
  }, [view]);

  const zoomed = view !== 'world';
  const cities = useMemo(() => groupByCity(players), [players]);
  const clusters = useMemo(() => clusterCities(cities, project, zoomed ? 26 : 14), [cities, project, zoomed]);
  const activeMapIds = useMemo(() => new Set(players.map((p) => COUNTRY_MAP_ID[p.countryCode])), [players]);
  const countryCodesByMapId = useMemo(() => {
    const m = new Map();
    for (const p of players) {
      const id = COUNTRY_MAP_ID[p.countryCode];
      if (!m.has(id)) m.set(id, new Set());
      m.get(id).add(p.countryCode);
    }
    return m;
  }, [players]);

  const seoul = project(...SEOUL);
  const isCitySelected = (c) => selected?.type === 'city' && selected.cityKeys.includes(c.key);
  const isClusterSelected = (k) => selected?.type === 'city' && selected.key === k.key;
  const isCountrySelected = (mapId) =>
    selected?.type === 'country' && COUNTRY_MAP_ID[selected.code] === mapId;

  const keyActivate = (fn) => (e) => {
    if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); fn(); }
  };

  return (
    <div className="world-map">
      <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} role="img" aria-label="해외파 선수 소속 도시 세계 지도">
        <rect width={WIDTH} height={HEIGHT} className="world-map__sea" />
        <g>
          {COUNTRIES.map((f) => {
            const mapId = Number(f.id);
            const active = activeMapIds.has(mapId);
            const codes = countryCodesByMapId.get(mapId);
            const cls = 'world-map__country'
              + (mapId === KOREA_MAP_ID ? ' is-home' : '')
              + (active ? ' is-active' : '')
              + (isCountrySelected(mapId) ? ' is-selected' : '');
            return (
              <path key={f.id} d={path(f)} className={cls}
                    onClick={active ? () => onSelect({ type: 'country', code: [...codes][0] }) : undefined}>
                <title>{f.properties.name}</title>
              </path>
            );
          })}
        </g>

        {/* 전체 보기에서는 서울에서 각 도시로 이어지는 선 */}
        {!zoomed && (
          <g className="world-map__routes" aria-hidden="true">
            {cities.map((c) => (
              <path key={c.key} d={path({ type: 'LineString', coordinates: [SEOUL, [c.lng, c.lat]] })}
                    className={isCitySelected(c) ? 'is-selected' : ''} />
            ))}
          </g>
        )}

        {seoul && (
          <g className="world-map__home" transform={`translate(${seoul[0]},${seoul[1]})`} aria-hidden="true">
            <circle r={zoomed ? 6 : 4.5} />
            {!zoomed && <text y={-9}>서울</text>}
          </g>
        )}

        <g>
          {clusters.map((k) => {
            const n = k.players.length;
            const r = (zoomed ? 9 : 5.5) + Math.sqrt(n) * (zoomed ? 2.5 : 1.5);
            const label = `${k.label}: ${k.players.map((p) => `${p.name}(${p.club})`).join(', ')}`;
            const select = () => onSelect({ type: 'city', key: k.key, cityKeys: k.cityKeys, label: k.label });
            return (
              <g key={k.key} transform={`translate(${k.x},${k.y})`}
                 className={'world-map__marker' + (isClusterSelected(k) ? ' is-selected' : '')}
                 role="button" tabIndex={0} aria-label={label} aria-pressed={isClusterSelected(k)}
                 onClick={select} onKeyDown={keyActivate(select)}
                 onMouseEnter={() => setHover(k)} onMouseLeave={() => setHover(null)}
                 onFocus={() => setHover(k)} onBlur={() => setHover(null)}>
                <circle className="world-map__pulse" r={r + 4} />
                <circle r={r} />
                {(zoomed || n > 1) && <text dy="0.35em">{n}</text>}
              </g>
            );
          })}
        </g>
      </svg>

      {hover && (
        <div className="world-map__tip" role="tooltip"
             style={{ left: `${(hover.x / WIDTH) * 100}%`, top: `${(hover.y / HEIGHT) * 100}%` }}>
          <strong>{hover.cities.map((c) => c.city).join(' · ')}</strong>{' '}
          <span className="muted">{[...new Set(hover.cities.map((c) => c.countryName))].join(' · ')}</span>
          <ul>
            {hover.players.map((p) => (
              <li key={p.id}><b>{p.name}</b> · {p.club}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
