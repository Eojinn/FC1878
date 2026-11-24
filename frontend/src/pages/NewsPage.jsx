import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api/api';
import StateBox from '../components/StateBox';
import { relativeTime } from '../utils/format';

/** 기사 하나: 제목·요약·언론사·시간. 누르면 원문(새 탭) */
function NewsCard({ item }) {
  return (
    <li className="news-item">
      <a href={item.link} target="_blank" rel="noopener noreferrer" className="news-item__link">
        <strong className="news-item__title">{item.title}</strong>
        {item.summary && <p className="news-item__summary">{item.summary}</p>}
        <span className="news-item__meta muted small">
          {item.source && <span className="news-item__source">{item.source}</span>}
          {item.publishedAt && <time dateTime={item.publishedAt}>{relativeTime(item.publishedAt)}</time>}
          <span aria-hidden="true">↗</span>
        </span>
      </a>
    </li>
  );
}

export default function NewsPage() {
  const [params, setParams] = useSearchParams();
  const topic = params.get('topic') || 'ALL';
  const q = params.get('q') || '';
  const [keyword, setKeyword] = useState(q);
  const [state, setState] = useState({ items: [], topics: [], start: 1, hasMore: false, sample: false });
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => setKeyword(q), [q]);

  // 주제·검색어가 바뀌면 처음부터 다시
  useEffect(() => {
    let alive = true;
    setLoading(true);
    setError(null);
    api.news({ topic, q, start: 1 })
      .then((res) => alive && setState(res))
      .catch((e) => alive && setError(e))
      .finally(() => alive && setLoading(false));
    return () => { alive = false; };
  }, [topic, q]);

  const loadMore = async () => {
    setLoadingMore(true);
    try {
      const res = await api.news({ topic, q, start: state.start + 20 });
      setState((s) => {
        const seen = new Set(s.items.map((i) => i.link));
        return { ...res, items: [...s.items, ...res.items.filter((i) => !seen.has(i.link))] };
      });
    } catch (e) {
      setError(e);
    } finally {
      setLoadingMore(false);
    }
  };

  const update = (next) => {
    const merged = { topic, q, ...next };
    if (merged.topic === 'ALL') delete merged.topic;
    if (!merged.q) delete merged.q;
    setParams(merged);
  };

  return (
    <div className="stack page-narrow">
      <div className="section-head">
        <div>
          <h1 className="h1">해외축구 뉴스</h1>
          <p className="muted">유럽 리그·챔피언스리그·해외파·이적 소식을 최신순으로 모았어요.</p>
        </div>
        <form className="search news-search" role="search" onSubmit={(e) => { e.preventDefault(); update({ q: keyword.trim() }); }}>
          <span aria-hidden="true">⌕</span>
          <input value={keyword} maxLength={40} onChange={(e) => setKeyword(e.target.value)}
                 placeholder="선수·팀 뉴스 검색" aria-label="뉴스 검색" />
          {q && <button type="button" className="link-btn" onClick={() => update({ q: '' })}>지우기</button>}
        </form>
      </div>

      <div className="chips" role="group" aria-label="뉴스 주제">
        {(state.topics.length ? state.topics : [{ key: 'ALL', label: '해외축구' }]).map((t) => {
          const on = !q && topic === t.key;
          return (
            <button key={t.key} type="button" className={'chip' + (on ? ' is-active' : '')} aria-pressed={on}
                    onClick={() => update({ topic: t.key, q: '' })}>{t.key === 'ALL' ? '전체' : t.label}</button>
          );
        })}
      </div>

      {q && <p className="muted">‘{q}’ 검색 결과</p>}
      {state.sample && !loading && (
        <p className="notice-box">
          지금은 <strong>샘플 기사</strong>가 보이고 있어요. 네이버 API 허브 키(<code>NAVER_CLIENT_ID</code>, <code>NAVER_CLIENT_SECRET</code>)를
          넣고 서버를 다시 시작하면 실제 기사가 나와요.
        </p>
      )}

      <section className="card news-card" aria-label="기사 목록">
        <StateBox loading={loading} error={error} empty={!state.items.length}
                  emptyText={q ? '검색 결과가 없어요. 다른 이름으로 찾아보세요.' : '기사가 없어요.'}>
          <ul className="news-list">
            {state.items.map((item) => <NewsCard key={item.link} item={item} />)}
          </ul>
          {state.hasMore && (
            <div className="news-more">
              <button type="button" className="btn btn--ghost" onClick={loadMore} disabled={loadingMore}>
                {loadingMore ? '불러오는 중…' : '기사 더 보기'}
              </button>
            </div>
          )}
        </StateBox>
      </section>
      <p className="muted small">
        기사 검색 결과는 네이버 뉴스 검색에서 제공돼요. 제목을 누르면 해당 언론사 기사(원문)로 이동하며, 기사의 저작권은 각 언론사에 있어요.
      </p>
    </div>
  );
}
