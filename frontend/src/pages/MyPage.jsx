import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../auth/AuthContext';
import AccountSettings from '../components/AccountSettings';
import Emblem from '../components/Emblem';
import StateBox from '../components/StateBox';
import { useAsync } from '../hooks/useAsync';
import { num } from '../utils/format';

export default function MyPage() {
  const { user, loading, setUser } = useAuth();
  const { data: teams, loading: tLoading, error } = useAsync(() => api.teams(), []);
  const [q, setQ] = useState('');

  if (!loading && !user) return <Navigate to="/login" state={{ from: '/me' }} replace />;
  if (!user) return null;

  const choose = async (teamId) => setUser(await api.setFavoriteTeam(teamId));
  const list = (teams || []).filter((t) => t.name.toLowerCase().includes(q.toLowerCase()));

  return (
    <div className="stack page-narrow">
      <h1 className="h1">내 정보</h1>
      <section className="card my-info">
        <div><span className="muted small">닉네임</span><strong>{user.nickname}</strong></div>
        <div><span className="muted small">이메일</span><span>{user.email}</span></div>
        <div><span className="muted small">포인트</span><strong className="num">{num(user.point)}P</strong></div>
        <div><span className="muted small">내 팀</span>
          <span className="team-cell">{user.favoriteTeamId
            ? <><Emblem src={user.favoriteTeamLogo} name={user.favoriteTeamName} size={24} />{user.favoriteTeamName}</>
            : '아직 선택하지 않았어요'}</span>
        </div>
      </section>

      <AccountSettings />

      <section className="card side-card">
        <div className="side-card__head">
          <h2 className="h3">좋아하는 팀 선택</h2>
          <input className="input-sm" type="search" placeholder="팀 이름 검색" aria-label="팀 이름 검색" value={q} onChange={(e) => setQ(e.target.value)} />
        </div>
        <StateBox loading={tLoading} error={error} empty={!list.length} emptyText="팀이 없어요.">
          <div className="team-grid">
            {list.map((t) => (
              <button key={t.id} type="button" aria-pressed={user.favoriteTeamId === t.id}
                      className={'team-pick' + (user.favoriteTeamId === t.id ? ' is-active' : '')} onClick={() => choose(t.id)}>
                <Emblem src={t.logoUrl} name={t.name} size={40} />
                <span>{t.name}</span>
              </button>
            ))}
          </div>
        </StateBox>
      </section>
    </div>
  );
}
