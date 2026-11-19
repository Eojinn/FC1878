import { api } from '../api/api';
import { useAuth } from '../auth/AuthContext';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';
import { num } from '../utils/format';

export default function RankingPage() {
  const { user } = useAuth();
  const { data, loading, error } = useAsync(() => api.rankings(100), []);
  const { data: mine } = useAsync(() => (user ? api.myRank() : Promise.resolve(null)), [user?.id]);

  return (
    <div className="stack page-narrow">
      <h1 className="h1">포인트 랭킹</h1>
      {user && (
        <div className="card my-rank">
          <span>내 순위</span><strong className="num">{mine?.rank ?? '-'}위</strong>
          <span className="muted">{num(user.point)}P</span>
        </div>
      )}
      <section className="card side-card">
        <StateBox loading={loading} error={error} empty={!data?.length} emptyText="아직 랭킹이 없어요.">
          <ol className="rank-list rank-list--full">
            {data?.map((u) => (
              <li key={u.memberId} className={user?.id === u.memberId ? 'is-me' : ''}>
                <span className={'rank-badge' + (u.rank <= 3 ? ' is-top' : '')}>{u.rank}</span>
                <span className="grow">{u.nickname}</span>
                <strong className="num">{num(u.point)}P</strong>
              </li>
            ))}
          </ol>
        </StateBox>
      </section>
    </div>
  );
}
