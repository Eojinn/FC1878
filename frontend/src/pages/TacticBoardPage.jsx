import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { api } from '../api/api';
import Emblem from '../components/Emblem';
import TacticPitch from '../components/TacticPitch';
import { useAsync } from '../hooks/useAsync';
import { useHistory } from '../hooks/useHistory';
import { POSITION_LABEL } from '../utils/countries';
import { dateTabs, hhmm } from '../utils/format';
import { FORMATIONS, PITCH_H, PITCH_W, arrange, formationSlots, placeholderPlayers } from '../utils/formations';

const STORAGE_KEY = 'fc1878.tacticboard.v1';
const DRAW_COLORS = ['#FFFFFF', '#FFE45C', '#FF4D63', '#5CC8FF'];
const TEAM_COLORS = ['#D7263D', '#2F6FEB', '#F5F5F5', '#111111', '#7A1F3D', '#6CABDD', '#FDB913', '#1D8F5A'];
const TOOLS = [
  { key: 'move', label: '이동', icon: '✥', hint: '선수·공을 끌어서 옮겨요' },
  { key: 'arrow', label: '패스', icon: '➝', hint: '화살표 (패스·이동)' },
  { key: 'run', label: '침투', icon: '⇢', hint: '점선 화살표 (침투·오프더볼)' },
  { key: 'line', label: '선', icon: '／', hint: '직선 (라인·간격)' },
  { key: 'pen', label: '펜', icon: '✎', hint: '자유롭게 그리기' },
  { key: 'erase', label: '지우개', icon: '⌫', hint: '그린 선을 눌러서 지워요' },
];
const ROLES = ['GK', 'DF', 'MF', 'FW'];

function newTeam(side, formation, color, gkColor) {
  return {
    teamId: null, name: side === 'home' ? '홈' : '원정', logoUrl: null, color, gkColor, formation,
    players: arrange(side, formation, placeholderPlayers(side, formation)),
  };
}

function initialBoard() {
  return {
    show: 'both',
    teams: { home: newTeam('home', '4-3-3', '#D7263D', '#F2C94C'), away: newTeam('away', '4-4-2', '#2F6FEB', '#3DDC97') },
    ball: { x: PITCH_W / 2, y: PITCH_H / 2 },
    drawings: [],
  };
}

function loadBoard() {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY));
    if (saved?.teams?.home?.players && saved?.teams?.away?.players && Array.isArray(saved.drawings)) return saved;
  } catch { /* 저장된 게 없거나 깨졌으면 새 판 */ }
  return initialBoard();
}

const fromSquad = (p) => ({ key: `s${p.id}`, name: p.name, number: p.number, role: p.role });

/** 스쿼드에서 포메이션에 맞게 11명 고르기 (포지션별 등번호 순). 실제 선발과는 다를 수 있음 */
function pickLineup(squad, formation) {
  const need = { GK: 0, DF: 0, MF: 0, FW: 0 };
  formationSlots(formation).forEach((s) => { need[s.role] += 1; });
  const chosen = [];
  ROLES.forEach((role) => chosen.push(...squad.filter((p) => p.role === role).slice(0, need[role])));
  const rest = squad.filter((p) => !chosen.includes(p) && p.role !== 'GK');
  while (chosen.length < 11 && rest.length) chosen.push(rest.shift());
  return chosen.map(fromSquad);
}

/** 피치 SVG → PNG 파일 저장 (위에 팀 이름·포메이션을 적어서) */
async function savePng(svg, board) {
  const W = 2100;
  const H = Math.round((W * (PITCH_H + 8)) / (PITCH_W + 8));
  const head = 120;
  const clone = svg.cloneNode(true);
  clone.setAttribute('width', W);
  clone.setAttribute('height', H);
  clone.querySelectorAll('[data-export-hide]').forEach((el) => el.remove()); // 선택 표시는 그림에서 뺌
  const url = URL.createObjectURL(new Blob([new XMLSerializer().serializeToString(clone)], { type: 'image/svg+xml' }));
  try {
    const img = await new Promise((resolve, reject) => {
      const i = new Image();
      i.onload = () => resolve(i);
      i.onerror = reject;
      i.src = url;
    });
    const canvas = document.createElement('canvas');
    canvas.width = W;
    canvas.height = H + head;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#140B0D';
    ctx.fillRect(0, 0, W, H + head);
    const { home, away } = board.teams;
    const title = board.show === 'both'
      ? `${home.name} ${home.formation}  vs  ${away.name} ${away.formation}`
      : `${board.teams[board.show].name} ${board.teams[board.show].formation}`;
    ctx.fillStyle = '#F3E9EB';
    ctx.font = '700 52px "Noto Sans KR", sans-serif';
    ctx.textBaseline = 'middle';
    ctx.fillText(title, 48, head / 2);
    ctx.fillStyle = '#FF4D63';
    ctx.font = '700 36px "Space Grotesk", sans-serif';
    ctx.textAlign = 'right';
    ctx.fillText('FC1878', W - 48, head / 2);
    ctx.drawImage(img, 0, head, W, H);
    const a = document.createElement('a');
    a.href = canvas.toDataURL('image/png');
    a.download = `fc1878-tactics-${new Date().toISOString().slice(0, 10)}.png`;
    a.click();
  } finally {
    URL.revokeObjectURL(url);
  }
}

/** 경기를 골라서 두 팀으로 시작 */
function MatchPicker({ onPick }) {
  const tabs = useMemo(() => dateTabs(), []);
  const [date, setDate] = useState(tabs[1].iso);
  const { data, loading } = useAsync(() => api.fixtures({ date }), [date]);
  const fixtures = (data ?? []).flatMap((g) => g.fixtures.map((f) => ({ ...f, leagueName: g.league.name })));
  return (
    <section className="card tb-match" aria-label="경기로 시작">
      <div className="tb-match__head">
        <strong>경기로 시작</strong>
        <div className="chips">
          {tabs.map((t) => (
            <button key={t.iso} type="button" className={'chip chip--sm' + (date === t.iso ? ' is-active' : '')}
                    onClick={() => setDate(t.iso)}>{t.label} <span className="muted">{t.short}</span></button>
          ))}
        </div>
      </div>
      <div className="tb-match__list">
        {loading && <span className="muted small">불러오는 중…</span>}
        {!loading && !fixtures.length && <span className="muted small">이 날은 경기가 없어요. 아래에서 팀을 직접 골라도 돼요.</span>}
        {fixtures.map((f) => (
          <button key={f.id} type="button" className="tb-fixture" onClick={() => onPick(f)}>
            <span className="muted small">{f.leagueName} · {hhmm(f.kickoffAt)}</span>
            <span className="tb-fixture__teams">
              <Emblem src={f.home.logoUrl} name={f.home.name} size={20} /> {f.home.name}
              <span className="muted">vs</span>
              <Emblem src={f.away.logoUrl} name={f.away.name} size={20} /> {f.away.name}
            </span>
          </button>
        ))}
      </div>
    </section>
  );
}

export default function TacticBoardPage() {
  const history = useHistory(loadBoard());
  const { state: board, set, replace, checkpoint, dropCheckpointIfUnchanged, undo, redo, canUndo, canRedo } = history;
  const [tool, setTool] = useState('move');
  const [color, setColor] = useState(DRAW_COLORS[0]);
  const [showNames, setShowNames] = useState(true);
  const [selected, setSelected] = useState(null);
  const [panelSide, setPanelSide] = useState('home');
  const [squads, setSquads] = useState({ home: null, away: null });
  const [squadLoading, setSquadLoading] = useState({ home: false, away: false });
  const [benchQuery, setBenchQuery] = useState('');
  const [notice, setNotice] = useState('');
  const svgRef = useRef(null);
  const wrapRef = useRef(null);
  const { data: teams } = useAsync(() => api.teams(), []);

  // 판은 이 브라우저에 자동 저장 (새로고침해도 유지)
  useEffect(() => {
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(board)); } catch { /* 저장 공간이 막혀 있으면 무시 */ }
  }, [board]);

  const updateTeam = useCallback((side, fn) => set((b) => ({ ...b, teams: { ...b.teams, [side]: fn(b.teams[side]) } })), [set]);

  /** 팀 스쿼드를 불러와서 (autoFill 이면) 포메이션대로 11명 배치 */
  const loadSquad = useCallback(async (side, teamId, autoFill) => {
    if (!teamId) { setSquads((s) => ({ ...s, [side]: null })); return; }
    setSquadLoading((s) => ({ ...s, [side]: true }));
    try {
      const list = await api.squad(teamId);
      setSquads((s) => ({ ...s, [side]: list ?? [] }));
      if (autoFill && list?.length) {
        replace((b) => {
          const team = b.teams[side];
          if (team.teamId !== teamId) return b; // 그 사이에 다른 팀으로 바꿨으면 무시
          return { ...b, teams: { ...b.teams, [side]: { ...team, players: arrange(side, team.formation, pickLineup(list, team.formation)) } } };
        });
        setNotice('스쿼드에서 포지션별로 11명을 자동 배치했어요. 실제 선발과 다를 수 있으니 벤치에서 바꿔 주세요.');
      }
    } catch {
      setSquads((s) => ({ ...s, [side]: [] }));
    } finally {
      setSquadLoading((s) => ({ ...s, [side]: false }));
    }
  }, [replace]);

  // 처음 열었을 때 저장된 팀이 있으면 스쿼드만 다시 불러옴
  useEffect(() => {
    ['home', 'away'].forEach((side) => board.teams[side].teamId && loadSquad(side, board.teams[side].teamId, false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const chooseTeam = (side, teamId) => {
    const t = teams?.find((x) => String(x.id) === String(teamId));
    setSelected(null);
    updateTeam(side, (team) => ({
      ...team, teamId: t?.id ?? null, name: t?.name ?? (side === 'home' ? '홈' : '원정'), logoUrl: t?.logoUrl ?? null,
      players: arrange(side, team.formation, placeholderPlayers(side, team.formation)),
    }));
    loadSquad(side, t?.id ?? null, true);
  };

  const pickMatch = (f) => {
    setSelected(null);
    set((b) => {
      const make = (side, t) => ({
        ...b.teams[side], teamId: t.id, name: t.name, logoUrl: t.logoUrl,
        players: arrange(side, b.teams[side].formation, placeholderPlayers(side, b.teams[side].formation)),
      });
      return { ...b, show: 'both', drawings: [], ball: { x: PITCH_W / 2, y: PITCH_H / 2 },
        teams: { home: make('home', f.home), away: make('away', f.away) } };
    });
    loadSquad('home', f.home.id, true);
    loadSquad('away', f.away.id, true);
  };

  const changeFormation = (side, formation) => updateTeam(side, (team) => ({
    ...team, formation, players: arrange(side, formation, team.players),
  }));

  const resetPositions = (side) => updateTeam(side, (team) => ({
    ...team, players: arrange(side, team.formation, team.players),
  }));

  // 선택한 선수 / 벤치
  const selectedTeam = selected ? board.teams[selected.side] : null;
  const selectedPlayer = selectedTeam?.players.find((p) => p.key === selected.key) ?? null;
  const benchSide = selected?.side ?? panelSide;
  const onPitchKeys = new Set(board.teams[benchSide].players.map((p) => p.key));
  const bench = (squads[benchSide] ?? [])
    .filter((p) => !onPitchKeys.has(`s${p.id}`))
    .filter((p) => !benchQuery || p.name.toLowerCase().includes(benchQuery.trim().toLowerCase()));

  const substitute = (squadPlayer) => {
    if (!selected || selected.side !== benchSide) {
      setNotice('피치에서 바꿀 선수를 먼저 눌러 주세요.');
      return;
    }
    const incoming = fromSquad(squadPlayer);
    updateTeam(selected.side, (team) => ({
      ...team, players: team.players.map((p) => (p.key === selected.key ? { ...p, ...incoming } : p)),
    }));
    setSelected({ side: selected.side, key: incoming.key });
  };

  const editSelected = (field, value) => replace((b) => ({
    ...b,
    teams: {
      ...b.teams,
      [selected.side]: {
        ...b.teams[selected.side],
        players: b.teams[selected.side].players.map((p) => (p.key === selected.key ? { ...p, [field]: value } : p)),
      },
    },
  }));

  // 단축키: Ctrl+Z 실행 취소, Ctrl+Shift+Z / Ctrl+Y 다시 실행, Esc 선택 해제
  useEffect(() => {
    const onKey = (e) => {
      if (e.target.closest('input, textarea, select')) return;
      const mod = e.ctrlKey || e.metaKey;
      if (mod && e.key.toLowerCase() === 'z') { e.preventDefault(); if (e.shiftKey) redo(); else undo(); }
      else if (mod && e.key.toLowerCase() === 'y') { e.preventDefault(); redo(); }
      else if (e.key === 'Escape') setSelected(null);
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [undo, redo]);

  useEffect(() => {
    if (!notice) return undefined;
    const t = setTimeout(() => setNotice(''), 4000);
    return () => clearTimeout(t);
  }, [notice]);

  const toggleFullscreen = () => {
    if (document.fullscreenElement) document.exitFullscreen();
    else wrapRef.current?.requestFullscreen?.();
  };

  const resetBoard = () => {
    setSelected(null);
    setSquads({ home: null, away: null });
    set(initialBoard());
  };

  const team = board.teams[panelSide];

  return (
    <div className="stack">
      <div className="section-head">
        <div>
          <h1 className="h1">전술판</h1>
          <p className="muted">두 팀의 포메이션을 한 피치에 올리고, 선수를 옮기고, 움직임을 그려 보세요.</p>
        </div>
      </div>

      <MatchPicker onPick={pickMatch} />

      <div className="tb-layout">
        {/* 왼쪽: 팀 설정 */}
        <aside className="card tb-panel" aria-label="팀 설정">
          <div className="tb-tabs" role="tablist">
            {['home', 'away'].map((side) => (
              <button key={side} type="button" role="tab" aria-selected={panelSide === side}
                      className={'tb-tab' + (panelSide === side ? ' is-active' : '')} onClick={() => setPanelSide(side)}>
                <span className="tb-dot" style={{ background: board.teams[side].color }} />
                {side === 'home' ? '홈' : '원정'}
              </button>
            ))}
          </div>

          <label className="field"><span>팀</span>
            <select value={team.teamId ?? ''} onChange={(e) => chooseTeam(panelSide, e.target.value)}>
              <option value="">팀 선택 안 함 ({panelSide === 'home' ? '홈' : '원정'})</option>
              {(teams ?? []).map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>
          </label>

          <label className="field"><span>포메이션</span>
            <select value={team.formation} onChange={(e) => changeFormation(panelSide, e.target.value)}>
              {FORMATIONS.map((f) => <option key={f} value={f}>{f}</option>)}
            </select>
          </label>

          <div className="field"><span>유니폼 색</span>
            <div className="tb-swatches" role="radiogroup" aria-label="유니폼 색">
              {TEAM_COLORS.map((c) => (
                <button key={c} type="button" role="radio" aria-checked={team.color === c} aria-label={c}
                        className={'tb-swatch' + (team.color === c ? ' is-active' : '')} style={{ background: c }}
                        onClick={() => updateTeam(panelSide, (t) => ({ ...t, color: c }))} />
              ))}
            </div>
          </div>

          <button type="button" className="btn btn--ghost btn--sm" onClick={() => resetPositions(panelSide)}>
            포메이션 위치로 되돌리기
          </button>

          <hr className="tb-sep" />

          <div className="field"><span>피치에 보이기</span>
            <div className="chips">
              {[['both', '두 팀'], ['home', '홈만'], ['away', '원정만']].map(([k, l]) => (
                <button key={k} type="button" className={'chip chip--sm' + (board.show === k ? ' is-active' : '')}
                        onClick={() => set((b) => ({ ...b, show: k }))}>{l}</button>
              ))}
            </div>
          </div>
          <label className="tb-check">
            <input type="checkbox" checked={showNames} onChange={(e) => setShowNames(e.target.checked)} /> 선수 이름 표시
          </label>
        </aside>

        {/* 가운데: 도구 + 피치 */}
        <div className="tb-center" ref={wrapRef}>
          <div className="card tb-toolbar" role="toolbar" aria-label="전술판 도구">
            <div className="tb-group">
              {TOOLS.map((t) => (
                <button key={t.key} type="button" title={t.hint} aria-pressed={tool === t.key}
                        className={'tb-tool' + (tool === t.key ? ' is-active' : '')} onClick={() => setTool(t.key)}>
                  <span aria-hidden="true">{t.icon}</span>{t.label}
                </button>
              ))}
            </div>
            <div className="tb-group" role="radiogroup" aria-label="선 색">
              {DRAW_COLORS.map((c) => (
                <button key={c} type="button" role="radio" aria-checked={color === c} aria-label={`선 색 ${c}`}
                        className={'tb-swatch tb-swatch--sm' + (color === c ? ' is-active' : '')} style={{ background: c }}
                        onClick={() => { setColor(c); if (tool === 'move' || tool === 'erase') setTool('arrow'); }} />
              ))}
            </div>
            <div className="tb-group">
              <button type="button" className="tb-tool" onClick={() => set((b) => ({ ...b, ball: b.ball ? null : { x: PITCH_W / 2, y: PITCH_H / 2 } }))}>
                <span aria-hidden="true">⚽</span>{board.ball ? '공 빼기' : '공 추가'}
              </button>
              <button type="button" className="tb-tool" onClick={undo} disabled={!canUndo} title="실행 취소 (Ctrl+Z)">
                <span aria-hidden="true">↶</span>실행 취소
              </button>
              <button type="button" className="tb-tool" onClick={redo} disabled={!canRedo} title="다시 실행 (Ctrl+Shift+Z)">
                <span aria-hidden="true">↷</span>다시 실행
              </button>
              <button type="button" className="tb-tool" disabled={!board.drawings.length}
                      onClick={() => set((b) => ({ ...b, drawings: [] }))}>그림 지우기</button>
            </div>
            <span className="grow" />
            <div className="tb-group">
              <button type="button" className="tb-tool" onClick={toggleFullscreen}>⛶ 전체 화면</button>
              <button type="button" className="tb-tool" onClick={resetBoard}>초기화</button>
              <button type="button" className="btn btn--primary btn--sm" onClick={() => savePng(svgRef.current, board)}>
                이미지로 저장
              </button>
            </div>
          </div>

          <div className="tb-pitch-wrap">
            <div className="tb-teambar">
              {['home', 'away'].filter((s) => board.show === 'both' || board.show === s).map((side) => (
                <span key={side} className={'tb-teambar__team tb-teambar__team--' + side}>
                  <Emblem src={board.teams[side].logoUrl} name={board.teams[side].name} size={22} />
                  <strong>{board.teams[side].name}</strong>
                  <span className="num muted">{board.teams[side].formation}</span>
                </span>
              ))}
            </div>
            <TacticPitch ref={svgRef} board={board} tool={tool} color={color} colors={DRAW_COLORS}
                         showNames={showNames} selected={selected} onSelect={setSelected} history={history} />
            {notice && <p className="tb-notice" role="status">{notice}</p>}
          </div>
          <p className="muted small">
            이동 도구로 선수·공을 끌어 옮기고, 선수를 누른 다음 오른쪽 벤치에서 교체할 수 있어요.
            판은 이 브라우저에 자동으로 저장돼요.
          </p>
        </div>

        {/* 오른쪽: 선택한 선수 + 벤치 */}
        <aside className="card tb-panel" aria-label="선수 교체">
          {selectedPlayer ? (
            <div className="tb-selected">
              <strong className="small muted">선택한 선수 · {selected.side === 'home' ? '홈' : '원정'}</strong>
              <div className="tb-selected__row">
                <label className="field tb-num"><span>번호</span>
                  <input type="number" min={1} max={99} value={selectedPlayer.number ?? ''}
                         onFocus={checkpoint} onBlur={dropCheckpointIfUnchanged}
                         onChange={(e) => editSelected('number', e.target.value === '' ? null : Number(e.target.value))} />
                </label>
                <label className="field grow"><span>이름</span>
                  <input maxLength={30} value={selectedPlayer.name} placeholder="직접 입력"
                         onFocus={checkpoint} onBlur={dropCheckpointIfUnchanged}
                         onChange={(e) => editSelected('name', e.target.value)} />
                </label>
              </div>
            </div>
          ) : (
            <p className="muted small">피치에서 선수를 누르면 번호·이름을 바꾸거나 벤치 선수와 교체할 수 있어요.</p>
          )}

          <hr className="tb-sep" />

          <div className="tb-bench-head">
            <strong>{board.teams[benchSide].name} 벤치 · 스쿼드</strong>
            {squads[benchSide] && <span className="muted small num">{bench.length}명</span>}
          </div>
          <div className="search tb-bench-search">
            <span aria-hidden="true">⌕</span>
            <input value={benchQuery} onChange={(e) => setBenchQuery(e.target.value)} placeholder="선수 검색" aria-label="벤치 선수 검색" />
          </div>
          <div className="tb-bench">
            {squadLoading[benchSide] && <p className="muted small">스쿼드 불러오는 중…</p>}
            {!squadLoading[benchSide] && !board.teams[benchSide].teamId && (
              <p className="muted small">왼쪽에서 팀을 고르거나 위에서 경기를 고르면 스쿼드가 나와요.</p>
            )}
            {!squadLoading[benchSide] && board.teams[benchSide].teamId && squads[benchSide]?.length === 0 && (
              <p className="muted small">이 팀의 스쿼드 정보를 불러오지 못했어요. 선수를 눌러 이름을 직접 입력해 주세요.</p>
            )}
            {ROLES.map((role) => {
              const list = bench.filter((p) => p.role === role);
              if (!list.length) return null;
              return (
                <div key={role} className="tb-bench__group">
                  <span className="tb-bench__role">{POSITION_LABEL[role]}</span>
                  {list.map((p) => (
                    <button key={p.id} type="button" className="tb-bench__player" onClick={() => substitute(p)}
                            title={selectedPlayer ? `${selectedPlayer.name || selectedPlayer.number} 대신 투입` : '피치에서 선수를 먼저 누르세요'}>
                      <span className="num tb-bench__num">{p.number ?? '-'}</span>
                      <span className="tb-bench__name">{p.name}</span>
                      <span className="muted small">{p.position}</span>
                    </button>
                  ))}
                </div>
              );
            })}
          </div>
        </aside>
      </div>
    </div>
  );
}
