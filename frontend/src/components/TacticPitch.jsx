import { forwardRef, useRef } from 'react';
import { PITCH_H, PITCH_W, shortName } from '../utils/formations';

/*
 * 전술판 피치 (SVG, 단위 m).
 * 이미지 저장 때 CSS 가 적용되지 않으므로 색·굵기는 모두 SVG 속성으로 직접 지정.
 */

const LINE = '#FFFFFF';
const LINE_W = 0.22;
const MIN_DRAW = 0.8; // 이보다 짧게 그은 선은 실수로 보고 버림

const clamp = (v, min, max) => Math.max(min, Math.min(max, v));

/** 배경색에 잘 보이는 글자색 */
function textOn(hex) {
  const n = parseInt(hex.slice(1), 16);
  const [r, g, b] = [(n >> 16) & 255, (n >> 8) & 255, n & 255];
  return 0.299 * r + 0.587 * g + 0.114 * b > 160 ? '#111111' : '#FFFFFF';
}

const pathOf = (points) => points.map(([x, y], i) => `${i ? 'L' : 'M'}${x.toFixed(2)} ${y.toFixed(2)}`).join(' ');

function PitchMarkings() {
  const stripes = Array.from({ length: 10 }, (_, i) => i);
  const common = { fill: 'none', stroke: LINE, strokeWidth: LINE_W };
  const box = (x, w, h) => <rect x={x} y={(PITCH_H - h) / 2} width={w} height={h} {...common} />;
  return (
    <g>
      <rect x={-4} y={-4} width={PITCH_W + 8} height={PITCH_H + 8} fill="#1E6B3A" />
      {stripes.map((i) => (
        <rect key={i} x={(PITCH_W / 10) * i} y={0} width={PITCH_W / 10} height={PITCH_H}
              fill={i % 2 ? '#237A43' : '#1F7140'} />
      ))}
      <rect x={0} y={0} width={PITCH_W} height={PITCH_H} {...common} />
      <line x1={PITCH_W / 2} y1={0} x2={PITCH_W / 2} y2={PITCH_H} {...common} />
      <circle cx={PITCH_W / 2} cy={PITCH_H / 2} r={9.15} {...common} />
      <circle cx={PITCH_W / 2} cy={PITCH_H / 2} r={0.4} fill={LINE} />
      {box(0, 16.5, 40.32)}{box(PITCH_W - 16.5, 16.5, 40.32)}
      {box(0, 5.5, 18.32)}{box(PITCH_W - 5.5, 5.5, 18.32)}
      <rect x={-2} y={(PITCH_H - 7.32) / 2} width={2} height={7.32} {...common} />
      <rect x={PITCH_W} y={(PITCH_H - 7.32) / 2} width={2} height={7.32} {...common} />
      <circle cx={11} cy={PITCH_H / 2} r={0.35} fill={LINE} />
      <circle cx={PITCH_W - 11} cy={PITCH_H / 2} r={0.35} fill={LINE} />
      {/* 페널티 아크 */}
      <path d={`M16.5 ${PITCH_H / 2 - 7.31} A9.15 9.15 0 0 1 16.5 ${PITCH_H / 2 + 7.31}`} {...common} />
      <path d={`M${PITCH_W - 16.5} ${PITCH_H / 2 - 7.31} A9.15 9.15 0 0 0 ${PITCH_W - 16.5} ${PITCH_H / 2 + 7.31}`} {...common} />
      {/* 코너 */}
      {[[0, 0, 0, 1], [PITCH_W, 0, 1, 1], [0, PITCH_H, 0, 0], [PITCH_W, PITCH_H, 1, 0]].map(([x, y, sx, sy]) => (
        <path key={`${x}-${y}`} d={`M${x + (sx ? -1 : 1)} ${y} A1 1 0 0 ${sx === sy ? 1 : 0} ${x} ${y + (sy ? 1 : -1)}`} {...common} />
      ))}
    </g>
  );
}

function Drawing({ d, erasing, onErase }) {
  const dashed = d.kind === 'run';
  const arrow = d.kind === 'arrow' || d.kind === 'run';
  const points = d.kind === 'pen' ? d.points : [d.points[0], d.points[d.points.length - 1]];
  const path = pathOf(points);
  return (
    <g>
      <path d={path} fill="none" stroke={d.color} strokeWidth={0.45} strokeLinecap="round" strokeLinejoin="round"
            strokeDasharray={dashed ? '1.4 1' : undefined}
            markerEnd={arrow ? `url(#arrow-${d.color.slice(1)})` : undefined} />
      {/* 지우개로 누르기 쉽게 넓은 투명 영역 */}
      <path d={path} fill="none" stroke="transparent" strokeWidth={2.4}
            style={{ cursor: erasing ? 'pointer' : undefined, pointerEvents: erasing ? 'stroke' : 'none' }}
            onPointerDown={erasing ? (e) => { e.stopPropagation(); onErase(d.id); } : undefined} />
    </g>
  );
}

const TacticPitch = forwardRef(function TacticPitch(
  { board, tool, color, colors, showNames, selected, onSelect, history }, svgRef) {
  const drag = useRef(null);
  const { checkpoint, replace, set, dropCheckpointIfUnchanged, presentRef } = history;

  const toPitch = (e) => {
    const svg = svgRef.current;
    const pt = svg.createSVGPoint();
    pt.x = e.clientX; pt.y = e.clientY;
    const p = pt.matrixTransform(svg.getScreenCTM().inverse());
    return [clamp(p.x, -3, PITCH_W + 3), clamp(p.y, -3, PITCH_H + 3)];
  };

  const startDrag = (e, info) => {
    if (tool !== 'move') return;
    e.stopPropagation();
    e.currentTarget.setPointerCapture?.(e.pointerId);
    const [x, y] = toPitch(e);
    drag.current = { ...info, dx: info.x - x, dy: info.y - y };
    checkpoint();
  };

  const onBackgroundDown = (e) => {
    if (e.button !== 0) return;
    if (tool === 'move') { onSelect(null); return; }
    if (tool === 'erase') return;
    e.currentTarget.setPointerCapture?.(e.pointerId);
    const p = toPitch(e);
    const id = `d${Date.now()}${Math.random().toString(36).slice(2, 6)}`;
    drag.current = { type: 'draw', id, start: presentRef.current };
    checkpoint();
    replace((b) => ({ ...b, drawings: [...b.drawings, { id, kind: tool, color, points: [p, p] }] }));
  };

  const onMove = (e) => {
    const d = drag.current;
    if (!d) return;
    const [x, y] = toPitch(e);
    if (d.type === 'player') {
      replace((b) => ({
        ...b,
        teams: {
          ...b.teams,
          [d.side]: {
            ...b.teams[d.side],
            players: b.teams[d.side].players.map((p) => (p.key === d.key ? { ...p, x: x + d.dx, y: y + d.dy } : p)),
          },
        },
      }));
    } else if (d.type === 'ball') {
      replace((b) => ({ ...b, ball: { x: x + d.dx, y: y + d.dy } }));
    } else if (d.type === 'draw') {
      replace((b) => ({
        ...b,
        drawings: b.drawings.map((dr) => {
          if (dr.id !== d.id) return dr;
          if (dr.kind !== 'pen') return { ...dr, points: [dr.points[0], [x, y]] };
          const [lx, ly] = dr.points[dr.points.length - 1];
          return Math.hypot(lx - x, ly - y) < 0.35 ? dr : { ...dr, points: [...dr.points, [x, y]] };
        }),
      }));
    }
  };

  const onUp = () => {
    const d = drag.current;
    drag.current = null;
    if (!d) return;
    if (d.type === 'draw') {
      const dr = presentRef.current.drawings.find((x) => x.id === d.id);
      const pts = dr?.points ?? [];
      const len = pts.length < 2 ? 0 : Math.hypot(pts[pts.length - 1][0] - pts[0][0], pts[pts.length - 1][1] - pts[0][1]);
      if (!dr || (len < MIN_DRAW && pts.length < 4)) replace(d.start); // 너무 짧으면 없던 일로
    }
    dropCheckpointIfUnchanged();
  };

  const erase = (id) => set((b) => ({ ...b, drawings: b.drawings.filter((d) => d.id !== id) }));

  const sides = board.show === 'both' ? ['home', 'away'] : [board.show];
  const cursor = tool === 'move' ? 'default' : tool === 'erase' ? 'not-allowed' : 'crosshair';

  return (
    <svg ref={svgRef} className="tactic-pitch" viewBox={`-4 -4 ${PITCH_W + 8} ${PITCH_H + 8}`}
         xmlns="http://www.w3.org/2000/svg" fontFamily="'Noto Sans KR', 'Apple SD Gothic Neo', sans-serif"
         role="img" aria-label="전술판 피치" style={{ cursor, touchAction: 'none' }}
         onPointerMove={onMove} onPointerUp={onUp} onPointerCancel={onUp}>
      <defs>
        {colors.map((c) => (
          <marker key={c} id={`arrow-${c.slice(1)}`} viewBox="0 0 10 10" refX="7" refY="5"
                  markerWidth="5" markerHeight="5" orient="auto-start-reverse">
            <path d="M0 0 L10 5 L0 10 z" fill={c} />
          </marker>
        ))}
      </defs>

      <g onPointerDown={onBackgroundDown}>
        <PitchMarkings />
      </g>

      <g>
        {board.drawings.map((d) => <Drawing key={d.id} d={d} erasing={tool === 'erase'} onErase={erase} />)}
      </g>

      {sides.map((side) => {
        const team = board.teams[side];
        return (
          <g key={side}>
            {team.players.map((p) => {
              const isGk = p.slotRole === 'GK';
              const fill = isGk ? team.gkColor : team.color;
              const isSelected = selected?.side === side && selected.key === p.key;
              const label = showNames ? shortName(p.name) : '';
              return (
                <g key={p.key} transform={`translate(${p.x.toFixed(2)} ${p.y.toFixed(2)})`}
                   style={{ cursor: tool === 'move' ? 'grab' : undefined, pointerEvents: tool === 'move' ? 'auto' : 'none' }}
                   onPointerDown={(e) => {
                     onSelect({ side, key: p.key });
                     startDrag(e, { type: 'player', side, key: p.key, x: p.x, y: p.y });
                   }}>
                  <title>{[p.number, p.name || `${side === 'home' ? '홈' : '원정'} 선수`].filter(Boolean).join(' ')}</title>
                  {isSelected && <circle data-export-hide="true" r={2.75} fill="none" stroke="#FFE45C" strokeWidth={0.4} />}
                  <circle r={1.95} fill={fill} stroke="#FFFFFF" strokeWidth={0.28} />
                  <text textAnchor="middle" dy="0.62" fontSize="1.75" fontWeight="700" fill={textOn(fill)}>
                    {p.number ?? ''}
                  </text>
                  {label && (
                    <text textAnchor="middle" y="3.9" fontSize="1.45" fontWeight="700" fill="#FFFFFF"
                          stroke="#0B0B0B" strokeWidth="0.38" paintOrder="stroke">{label}</text>
                  )}
                </g>
              );
            })}
          </g>
        );
      })}

      {board.ball && (
        <g transform={`translate(${board.ball.x.toFixed(2)} ${board.ball.y.toFixed(2)})`}
           style={{ cursor: tool === 'move' ? 'grab' : undefined, pointerEvents: tool === 'move' ? 'auto' : 'none' }}
           onPointerDown={(e) => startDrag(e, { type: 'ball', x: board.ball.x, y: board.ball.y })}>
          <title>공</title>
          <circle r={1.05} fill="#FFFFFF" stroke="#111111" strokeWidth={0.18} />
          <path d="M0 -0.45 L0.43 -0.14 L0.27 0.37 L-0.27 0.37 L-0.43 -0.14 Z" fill="#111111" />
        </g>
      )}
    </svg>
  );
});

export default TacticPitch;
