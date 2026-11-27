import { useEffect, useId, useMemo, useRef, useState } from 'react';
import { api } from '../api/api';
import Emblem from './Emblem';

/**
 * 검색되는 팀 고르기 (전술판용).
 * - 입력하면 영문 이름으로 바로 거르고, 한글·별명(맨유, 토트넘 …)은 서버 검색으로 함께 찾음
 * - ↑↓ 로 고르고 Enter, Esc 로 닫기
 *
 * @param teams    전체 팀 목록 [{ id, name, logoUrl }]
 * @param value    선택된 팀 id (없으면 null)
 * @param onChange (teamId | null) => void
 */
export default function TeamPicker({ teams = [], value, onChange, emptyLabel = '팀 선택 안 함' }) {
  const [open, setOpen] = useState(false);
  const [q, setQ] = useState('');
  const [remote, setRemote] = useState([]);   // 서버 검색 결과 (한글 이름 포함)
  const [active, setActive] = useState(0);
  const boxRef = useRef(null);
  const inputRef = useRef(null);
  const listRef = useRef(null);
  const listId = useId();

  const current = teams.find((t) => String(t.id) === String(value)) ?? null;
  const keyword = q.trim();

  // 한글·별명 검색: 입력이 멈추고 0.2초 뒤 서버에 물어봄
  useEffect(() => {
    if (!keyword) { setRemote([]); return undefined; }
    let alive = true;
    const t = setTimeout(() => {
      api.search(keyword, 15).then((r) => alive && setRemote(r.teams ?? [])).catch(() => alive && setRemote([]));
    }, 200);
    return () => { alive = false; clearTimeout(t); };
  }, [keyword]);

  // 영문 이름으로 바로 거른 것 + 서버가 찾은 것 (겹치면 한 번만)
  const options = useMemo(() => {
    if (!keyword) return teams.map((t) => ({ ...t, sub: null }));
    const compact = keyword.toLowerCase().replace(/\s/g, '');
    const byId = new Map();
    remote.forEach((r) => {
      const t = teams.find((x) => x.id === r.id);
      if (t) byId.set(t.id, { ...t, sub: r.subName });
    });
    teams.forEach((t) => {
      if (!byId.has(t.id) && t.name.toLowerCase().replace(/\s/g, '').includes(compact)) byId.set(t.id, { ...t, sub: null });
    });
    return [...byId.values()];
  }, [keyword, remote, teams]);

  // 열릴 때 입력칸에 커서, 닫힐 때 검색어 지우기
  useEffect(() => {
    if (open) { inputRef.current?.focus(); setActive(0); } else setQ('');
  }, [open]);

  // 검색어를 치면 첫 번째 결과를 미리 골라 둠 (Enter 로 바로 선택)
  useEffect(() => { setActive(keyword ? 1 : 0); }, [keyword]);

  // 바깥을 누르면 닫기
  useEffect(() => {
    if (!open) return undefined;
    const onDown = (e) => { if (!boxRef.current?.contains(e.target)) setOpen(false); };
    document.addEventListener('mousedown', onDown);
    return () => document.removeEventListener('mousedown', onDown);
  }, [open]);

  // 고른 줄이 보이게 스크롤
  useEffect(() => {
    listRef.current?.querySelector('.is-active')?.scrollIntoView({ block: 'nearest' });
  }, [active, open]);

  const pick = (id) => { onChange(id); setOpen(false); };

  // 맨 위 "선택 안 함"을 0번으로, 팀은 1번부터
  const onKeyDown = (e) => {
    if (e.key === 'ArrowDown') { e.preventDefault(); setActive((a) => Math.min(a + 1, options.length)); }
    else if (e.key === 'ArrowUp') { e.preventDefault(); setActive((a) => Math.max(a - 1, 0)); }
    else if (e.key === 'Enter') {
      e.preventDefault();
      if (active === 0) pick(null);
      else if (options[active - 1]) pick(options[active - 1].id);
    } else if (e.key === 'Escape') { e.preventDefault(); setOpen(false); }
  };

  return (
    <div className="team-picker" ref={boxRef}>
      <button type="button" className="team-picker__current" aria-haspopup="listbox" aria-expanded={open}
              onClick={() => setOpen((o) => !o)}>
        {current
          ? <><Emblem src={current.logoUrl} name={current.name} size={20} /><span>{current.name}</span></>
          : <span className="muted">{emptyLabel}</span>}
        <span className="team-picker__caret" aria-hidden="true">▾</span>
      </button>

      {open && (
        <div className="team-picker__pop">
          <input ref={inputRef} className="team-picker__input" value={q} onChange={(e) => setQ(e.target.value)}
                 onKeyDown={onKeyDown} placeholder="팀 이름 검색 (예: 토트넘, 맨유, Arsenal)"
                 role="combobox" aria-expanded="true" aria-controls={listId} aria-autocomplete="list" />
          <ul className="team-picker__list" id={listId} role="listbox" ref={listRef}>
            <li role="option" aria-selected={value == null}
                className={'team-picker__item team-picker__none' + (active === 0 ? ' is-active' : '')}
                onMouseEnter={() => setActive(0)} onMouseDown={(e) => { e.preventDefault(); pick(null); }}>
              {emptyLabel}
            </li>
            {options.map((t, i) => (
              <li key={t.id} role="option" aria-selected={String(t.id) === String(value)}
                  className={'team-picker__item' + (active === i + 1 ? ' is-active' : '') + (String(t.id) === String(value) ? ' is-selected' : '')}
                  onMouseEnter={() => setActive(i + 1)} onMouseDown={(e) => { e.preventDefault(); pick(t.id); }}>
                <Emblem src={t.logoUrl} name={t.name} size={22} />
                <span className="team-picker__name">
                  {t.name}
                  {t.sub && <span className="muted small"> {t.sub}</span>}
                </span>
              </li>
            ))}
            {keyword && options.length === 0 && <li className="team-picker__empty muted">"{keyword}" 팀을 찾지 못했어요</li>}
          </ul>
        </div>
      )}
    </div>
  );
}
