import { Link } from 'react-router-dom';
import { api } from '../api/api';
import { useAsync } from '../hooks/useAsync';
import { CATEGORY_LABEL, num, relativeTime } from '../utils/format';
import Emblem from './Emblem';
import StateBox from './StateBox';

export function StandingsMini({ league }) {
  const { data, error, loading } = useAsync(
    () => (league ? api.standings(league.id) : Promise.resolve([])), [league?.id]);
  return (
    <section className="card side-card" aria-label="순위">
      <div className="side-card__head">
        <h2 className="h3">{league ? `${league.name} 순위` : '리그 순위'}</h2>
        <Link to="/standings" className="more">전체 순위</Link>
      </div>
      <StateBox loading={loading} error={error} empty={!data?.length} emptyText="순위 정보가 아직 없어요.">
        <div className="mini-table">
          <div className="mini-table__row mini-table__row--head"><span>#</span><span>팀</span><span>경기</span><span>승점</span></div>
          {data?.slice(0, 6).map((s) => (
            <div className="mini-table__row" key={s.team.id}>
              <span className="rank-num">{s.rank}</span>
              <Link to={`/teams/${s.team.id}`} className="team-cell team-cell--link"><Emblem src={s.team.logoUrl} name={s.team.name} size={22} />{s.team.name}</Link>
              <span className="muted">{s.played}</span>
              <strong className="num">{s.points}</strong>
            </div>
          ))}
        </div>
      </StateBox>
    </section>
  );
}

export function RankingMini() {
  const { data, error, loading } = useAsync(() => api.rankings(5), []);
  return (
    <section className="card side-card" aria-label="포인트 랭킹">
      <div className="side-card__head">
        <h2 className="h3">포인트 랭킹</h2>
        <Link to="/ranking" className="more">전체 랭킹</Link>
      </div>
      <StateBox loading={loading} error={error} empty={!data?.length} emptyText="아직 랭킹이 없어요. 첫 예측에 참여해 보세요!">
        <ol className="rank-list">
          {data?.map((u) => (
            <li key={u.memberId}>
              <span className={'rank-badge' + (u.rank <= 3 ? ' is-top' : '')}>{u.rank}</span>
              <span className="grow">{u.nickname}</span>
              <strong className="num">{num(u.point)}P</strong>
            </li>
          ))}
        </ol>
      </StateBox>
    </section>
  );
}

export function PopularPosts() {
  const { data, error, loading } = useAsync(() => api.posts({ sort: 'popular', size: 4 }), []);
  return (
    <section className="card side-card side-card--flush" aria-label="커뮤니티 인기글">
      <div className="side-card__head">
        <h2 className="h3">커뮤니티 인기글</h2>
        <Link to="/community/write" className="more">글쓰기</Link>
      </div>
      <StateBox loading={loading} error={error} empty={!data?.content?.length} emptyText="아직 게시글이 없어요.">
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
  );
}
