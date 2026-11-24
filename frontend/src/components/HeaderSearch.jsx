import { useEffect, useId, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api } from '../api/api';
import Emblem from './Emblem';
import { SearchIcon } from './Icons';

const ROLE_LABEL = { GK: '골키퍼', DF: '수비수', MF: '미드필더', FW: '공격수' };
const TYPE_LABEL = { OVERSEAS: '해외파', TRANSFER: '이적', SQUAD: null };

/**
 * 헤더 검색창: 팀·선수.
 * 두 글자 이상 입력하면 아래에 바로 결과가 나오고(↑↓로 고르고 Enter), 그냥 Enter 를 누르면 검색 결과 페이지로.
 */
export default function HeaderSearch() {
  const [q, setQ] = useState('');
  const [result, setResult] = useState(null);
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(-1);
  const navigate = useNavigate();
  const location = useLocation();
  const boxRef = useRef(null);
  const listId = useId();

  // 페이지를 옮기면 닫기
  useEffect(() => { setOpen(false); setActive(-1); }, [location.pathname, location.search]);

  // 입력이 멈추고 0.25초 뒤에 검색
  useEffect(() => {
    const keyword = q.trim();
    if (keyword.length < 2) { setResult(null); return undefined; }
    let alive = true;
    const t = setTimeout(() => {
      api.search(keyword, 5).then((r) => alive && setResult(r)).catch(() => alive && setResult(null));
    }, 250);
    return () => { alive = false; clearTimeout(t); };
  }, [q]);

  // 바깥을 누르면 닫기
  useEffect(() => {
    const onDown = (e) => { if (!boxRef.current?.contains(e.target)) setOpen(false); };
    document.addEventListener('mousedown', onDown);
    return () => document.removeEventListener('mousedown', onDown);
  }, []);

  const items = result ? [
    ...result.teams.map((t) => ({ kind: 'team', key: `t${t.id}`, link: t.link, data: t })),
    ...result.players.map((p, i) => ({ kind: 'player', key: `p${i}${p.name}`, link: p.link, data: p })),
  ] : [];

  const go = (link) => { if (link) { navigate(link); setQ(''); setOpen(false); } };
  const goResults = () => {
    const keyword = q.trim();
    if (keyword) { navigate(`/search?q=${encodeURIComponent(keyword)}`); setOpen(false); }
  };

  const onKeyDown = (e) => {
    if (e.key === 'ArrowDown') { e.preventDefault(); setOpen(true); setActive((a) => Math.min(a + 1, items.length - 1)); }
    else if (e.key === 'ArrowUp') { e.preventDefault(); setActive((a) => Math.max(a - 1, -1)); }
    else if (e.key === 'Escape') { setOpen(false); setActive(-1); }
  };

  const onSubmit = (e) => {
    e.preventDefault();
    if (open && active >= 0 && items[active]?.link) go(items[active].link);
    else goResults();
  };

  const showBox = open && q.trim().length >= 2 && result;

  return (
    <div className="hsearch" ref={boxRef}>
      <form className="search" role="search" onSubmit={onSubmit}>
        <SearchIcon />
        <label className="sr-only" htmlFor="site-search">팀·선수 검색</label>
        <input id="site-search" type="search" placeholder="팀·선수 검색" value={q} autoComplete="off"
               role="combobox" aria-expanded={!!showBox} aria-controls={listId} aria-autocomplete="list"
               aria-activedescendant={active >= 0 ? `${listId}-${active}` : undefined}
               onChange={(e) => { setQ(e.target.value); setOpen(true); setActive(-1); }}
               onFocus={() => setOpen(true)} onKeyDown={onKeyDown} />
      </form>

      {showBox && (
        <div className="hsearch__box" id={listId} role="listbox" aria-label="검색 미리보기">
          {!items.length && <p className="hsearch__empty muted small">‘{q.trim()}’에 맞는 팀·선수가 없어요.</p>}
          {result.teams.length > 0 && <p className="hsearch__group">팀</p>}
          {items.map((it, i) => (
            <div key={it.key}>
              {it.kind === 'player' && (i === 0 || items[i - 1].kind === 'team') && <p className="hsearch__group">선수</p>}
              <button type="button" id={`${listId}-${i}`} role="option" aria-selected={active === i}
                      className={'hsearch__item' + (active === i ? ' is-active' : '')}
                      onMouseEnter={() => setActive(i)} onClick={() => go(it.link)} disabled={!it.link}>
                {it.kind === 'team' ? (
                  <>
                    <Emblem src={it.data.logoUrl} name={it.data.name} size={26} />
                    <span className="hsearch__text">
                      <strong>{it.data.name}</strong>
                      {it.data.subName && <span className="muted small">{it.data.subName}</span>}
                    </span>
                  </>
                ) : (
                  <>
                    <span className={'pos-badge pos-badge--' + it.data.role}>{it.data.role}</span>
                    <span className="hsearch__text">
                      <strong>{it.data.name}
                        {it.data.nameEn && <span className="hsearch__en">{it.data.nameEn}</span>}
                        {TYPE_LABEL[it.data.type] && <span className="hsearch__tag">{TYPE_LABEL[it.data.type]}</span>}
                      </strong>
                      <span className="muted small">
                        {[it.data.teamName, ROLE_LABEL[it.data.role], it.data.nationality].filter(Boolean).join(' · ')}
                      </span>
                    </span>
                  </>
                )}
              </button>
            </div>
          ))}
          <button type="button" className="hsearch__all" onClick={goResults}>‘{q.trim()}’ 검색 결과 모두 보기 →</button>
        </div>
      )}
    </div>
  );
}
