import { Link } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../auth/AuthContext';
import Emblem from '../components/Emblem';
import FixtureBoard from '../components/FixtureBoard';
import PredictionCard from '../components/PredictionCard';
import { PopularPosts, RankingMini, StandingsMini } from '../components/Sidebar';
import { useAsync } from '../hooks/useAsync';
import { useLeagues } from '../hooks/useLeagues';
import { EPL_IDS } from '../utils/format';

function PopularLeagues({ leagues }) {
  return (
    <section className="card side-card" aria-labelledby="lg-title">
      <h2 id="lg-title" className="h3">인기 리그</h2>
      <div className="league-tiles">
        {leagues.slice(0, 6).map((l) => (
          <Link key={l.id} to={`/standings?league=${l.id}`} className="league-tile">
            <Emblem src={l.logoUrl} name={l.name} size={44} light />
            <span>{l.name}</span>
          </Link>
        ))}
        {leagues.length === 0 && <p className="muted">리그 데이터가 아직 없어요.</p>}
      </div>
    </section>
  );
}

function FollowBanner() {
  const { user } = useAuth();
  if (user?.favoriteTeamId) {
    return (
      <section className="follow-banner">
        <div className="follow-banner__team">
          <Emblem src={user.favoriteTeamLogo} name={user.favoriteTeamName} size={48} />
          <div>
            <p className="follow-banner__sub">내 팀</p>
            <h2 className="h3">{user.favoriteTeamName}</h2>
          </div>
        </div>
        <Link to={`/fixtures?team=${user.favoriteTeamId}`} className="btn btn--light">내 팀 경기 보기</Link>
      </section>
    );
  }
  return (
    <section className="follow-banner">
      <h2 className="h3">좋아하는 팀을 팔로우하세요</h2>
      <p>로그인하면 내 팀 경기 일정과 승부예측 결과를 홈에서 바로 볼 수 있어요.</p>
      <Link to={user ? '/me' : '/login'} className="btn btn--light">{user ? '팀 선택하기' : '로그인하고 팀 선택'}</Link>
    </section>
  );
}

export default function HomePage() {
  const { leagues } = useLeagues();
  const { data: featured, loading } = useAsync(() => api.featured(), []);
  const epl = leagues.find((l) => EPL_IDS.includes(l.externalId)) || leagues[0];

  return (
    <div className="stack-lg">
      <div className="row">
        <div className="col-main">
          {featured ? <PredictionCard fixture={featured} /> : (
            <section className="card pick-card pick-card--large">
              <h2 className="h2">오늘의 승부예측</h2>
              <p className="muted">{loading ? '불러오는 중…' : '예정된 경기가 없어요.'}</p>
            </section>
          )}
        </div>
        <div className="col-side stack-lg">
          <PopularLeagues leagues={leagues} />
          <FollowBanner />
        </div>
      </div>

      <div className="row row--top">
        <section className="col-main stack" aria-labelledby="fx-title">
          <div className="section-head">
            <h2 id="fx-title" className="h2">경기 일정</h2>
            <Link to="/fixtures" className="more">전체 일정 보기</Link>
          </div>
          <FixtureBoard leagues={leagues} />
        </section>
        <aside className="col-side stack-lg">
          <StandingsMini league={epl} />
          <RankingMini />
          <PopularPosts />
        </aside>
      </div>
    </div>
  );
}
