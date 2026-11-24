import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api } from '../api/api';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';
import { CATEGORY_LABEL, num, relativeTime } from '../utils/format';

export default function CommunityPage() {
  const [params, setParams] = useSearchParams();
  const category = params.get('category') || '';
  const keyword = params.get('keyword') || '';
  const sort = params.get('sort') || 'latest';
  const page = Number(params.get('page') || 0);

  const [input, setInput] = useState(keyword);
  useEffect(() => setInput(keyword), [keyword]);

  const { data, loading, error } = useAsync(
    () => api.posts({ category, keyword, sort, page, size: 20 }), [category, keyword, sort, page]);

  const update = (next) => {
    const merged = { category, keyword, sort, page: 0, ...next };
    Object.keys(merged).forEach((k) => (merged[k] === '' || merged[k] === 0) && k !== 'page' && delete merged[k]);
    if (!merged.page) delete merged.page;
    setParams(merged);
  };

  return (
    <div className="stack page-narrow">
      <div className="section-head">
        <h1 className="h1">커뮤니티</h1>
        <div className="community-actions">
          <form className="search" role="search" onSubmit={(e) => { e.preventDefault(); update({ keyword: input.trim() }); }}>
            <span aria-hidden="true">⌕</span>
            <input value={input} onChange={(e) => setInput(e.target.value)} placeholder="게시글 검색" aria-label="게시글 검색" />
          </form>
          <Link to="/community/write" className="btn btn--primary">글쓰기</Link>
        </div>
      </div>
      {keyword && (
        <p className="muted">‘{keyword}’ 검색 결과 · <button type="button" className="link-btn" onClick={() => update({ keyword: '' })}>검색 지우기</button></p>
      )}
      <div className="chips">
        <button type="button" className={'chip' + (!category ? ' is-active' : '')} onClick={() => update({ category: '' })}>전체</button>
        {Object.entries(CATEGORY_LABEL).map(([k, v]) => (
          <button key={k} type="button" className={'chip' + (category === k ? ' is-active' : '')} onClick={() => update({ category: k })}>{v}</button>
        ))}
        <span className="grow" />
        <button type="button" className={'chip' + (sort === 'latest' ? ' is-active' : '')} onClick={() => update({ sort: 'latest' })}>최신순</button>
        <button type="button" className={'chip' + (sort === 'popular' ? ' is-active' : '')} onClick={() => update({ sort: 'popular' })}>인기순</button>
      </div>

      <section className="card side-card--flush">
        <StateBox loading={loading} error={error} empty={!data?.content?.length} emptyText="게시글이 없어요. 첫 글을 남겨 보세요!">
          {data?.content.map((p) => (
            <Link to={`/community/${p.id}`} className="post-row" key={p.id}>
              <span className="post-row__title">
                <span className="cat">{CATEGORY_LABEL[p.category] || p.category}</span>
                <span className="ellipsis">{p.title}</span>
              </span>
              <span className="muted small">{p.authorNickname} · 조회 {num(p.viewCount)} · {relativeTime(p.createdAt)}</span>
            </Link>
          ))}
        </StateBox>
      </section>

      {data && data.totalPages > 1 && (
        <nav className="pager" aria-label="페이지">
          <button type="button" className="btn btn--ghost" disabled={page === 0} onClick={() => setParams({ ...Object.fromEntries(params), page: page - 1 })}>이전</button>
          <span className="muted">{page + 1} / {data.totalPages}</span>
          <button type="button" className="btn btn--ghost" disabled={data.last} onClick={() => setParams({ ...Object.fromEntries(params), page: page + 1 })}>다음</button>
        </nav>
      )}
    </div>
  );
}
