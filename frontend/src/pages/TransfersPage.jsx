import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api/api';
import Emblem from '../components/Emblem';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';
import { POSITION_LABEL } from '../utils/countries';
import { TRANSFER_TYPE, feeText, formatFee, shortDate, untilText } from '../utils/transfer';

/** 클럽 이름 + 엠블럼(이미지가 없어서 약자 원으로 표시) + 리그 */
function Club({ name, league, align = 'left' }) {
  return (
    <span className={'tr-club tr-club--' + align}>
      <Emblem name={name} size={30} />
      <span className="tr-club__text">
        <strong>{name}</strong>
        {league && <span className="muted small">{league}</span>}
      </span>
    </span>
  );
}

function TransferRow({ t }) {
  const type = TRANSFER_TYPE[t.type] ?? TRANSFER_TYPE.PERMANENT;
  return (
    <li className={'tr-row' + (t.korean ? ' is-korean' : '')}>
      <time className="tr-row__date num" dateTime={t.transferDate}>{shortDate(t.transferDate)}</time>
      <div className="tr-row__player">
        <span className={'pos-badge pos-badge--' + t.position} title={POSITION_LABEL[t.position]}>{t.position}</span>
        <span className="tr-row__name">
          <strong>{t.playerName}</strong>
          <span className="muted small">{[t.nationality, t.playerNameEn].filter(Boolean).join(' · ')}</span>
        </span>
      </div>
      <div className="tr-row__move">
        <Club name={t.fromClub} league={t.fromLeague} align="right" />
        <span className="tr-arrow" aria-label="에서">→</span>
        <Club name={t.toClub} league={t.toLeague} />
      </div>
      <div className="tr-row__deal">
        <span className={'tr-type tr-type--' + type.cls}>{type.label}</span>
        <strong className="tr-fee num">{feeText(t)}</strong>
        {(t.feeNote || t.contractUntil) && (
          <span className="muted small">{[t.feeNote, untilText(t.contractUntil)].filter(Boolean).join(' · ')}</span>
        )}
      </div>
    </li>
  );
}

export default function TransfersPage() {
  const [params, setParams] = useSearchParams();
  const windowKey = params.get('window') || '';
  const league = params.get('league') || '';
  const korean = params.get('korean') === 'true';
  const q = params.get('q') || '';
  const sort = params.get('sort') || 'date';
  const [keyword, setKeyword] = useState(q);
  useEffect(() => setKeyword(q), [q]);

  const { data, loading, error } = useAsync(
    () => api.transfers({ window: windowKey, league, korean: korean || undefined, q, sort }),
    [windowKey, league, korean, q, sort]);

  /** 바꾼 값만 주소에 반영 (빈 값은 주소에서 뺌) */
  const update = (next) => {
    const merged = { window: windowKey, league, korean: korean ? 'true' : '', q, sort, ...next };
    if (merged.sort === 'date') merged.sort = '';
    Object.keys(merged).forEach((k) => !merged[k] && delete merged[k]);
    setParams(merged);
  };

  const windows = data?.windows ?? [];
  const current = data?.window ?? windowKey;
  const windowLabel = current === 'all' ? '전체 기간' : windows.find((w) => w.key === current)?.label + ' 이적시장';
  const total = formatFee(data?.summary.totalFeeMillionEur);

  return (
    <div className="stack">
      <div className="section-head">
        <div>
          <h1 className="h1">이적시장</h1>
          {data && (
            <p className="muted">
              {windowLabel} · 확정 이적 <strong className="num">{data.summary.count}</strong>건
              {data.summary.koreanCount > 0 && <> · 한국 선수 <strong className="num">{data.summary.koreanCount}</strong>건</>}
              {total && Number(data.summary.totalFeeMillionEur) > 0 && <> · 이적료 합계 <strong className="num">{total}</strong></>}
            </p>
          )}
        </div>
        <form className="search tr-search" role="search" onSubmit={(e) => { e.preventDefault(); update({ q: keyword.trim() }); }}>
          <span aria-hidden="true">⌕</span>
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="선수·팀 검색"
                 aria-label="선수 또는 팀 이름으로 검색" />
          {q && <button type="button" className="link-btn" onClick={() => update({ q: '' })}>지우기</button>}
        </form>
      </div>

      <div className="chips" role="group" aria-label="이적시장 기간">
        {windows.map((w) => (
          <button key={w.key} type="button" className={'chip' + (current === w.key ? ' is-active' : '')}
                  aria-pressed={current === w.key} onClick={() => update({ window: w.key, league: '' })}>
            {w.label} <span className="chip__count">{w.count}</span>
          </button>
        ))}
        {windows.length > 1 && (
          <button type="button" className={'chip' + (current === 'all' ? ' is-active' : '')}
                  aria-pressed={current === 'all'} onClick={() => update({ window: 'all', league: '' })}>전체 기간</button>
        )}
      </div>

      <div className="tr-filters">
        <div className="chips" role="group" aria-label="리그">
          <button type="button" className={'chip' + (!league ? ' is-active' : '')} onClick={() => update({ league: '' })}>모든 리그</button>
          {(data?.leagues ?? []).slice(0, 8).map((l) => (
            <button key={l} type="button" className={'chip' + (league === l ? ' is-active' : '')}
                    aria-pressed={league === l} onClick={() => update({ league: l })}>{l}</button>
          ))}
        </div>
        <div className="chips tr-filters__right">
          <button type="button" className={'chip chip--korean' + (korean ? ' is-active' : '')} aria-pressed={korean}
                  onClick={() => update({ korean: korean ? '' : 'true' })}>한국 선수만</button>
          <button type="button" className={'chip' + (sort === 'date' ? ' is-active' : '')} onClick={() => update({ sort: 'date' })}>최신순</button>
          <button type="button" className={'chip' + (sort === 'fee' ? ' is-active' : '')} onClick={() => update({ sort: 'fee' })}>이적료순</button>
        </div>
      </div>

      {q && <p className="muted">‘{q}’ 검색 결과</p>}

      <section className="card tr-list-card" aria-label="이적 목록">
        <StateBox loading={loading} error={error} empty={!data?.items.length}
                  emptyText={q || league || korean ? '조건에 맞는 이적이 없어요.' : '등록된 이적이 없어요.'}>
          <div className="tr-head" aria-hidden="true">
            <span>날짜</span><span>선수</span><span className="center">이동</span><span className="right">조건</span>
          </div>
          <ul className="tr-list">
            {data?.items.map((t) => <TransferRow key={t.id} t={t} />)}
          </ul>
        </StateBox>
      </section>
      <p className="muted small">구단 공식 발표일 기준이며, 이적료는 공개·보도된 기본 금액이에요. 옵션은 조건 칸에 따로 적었어요.</p>
    </div>
  );
}
