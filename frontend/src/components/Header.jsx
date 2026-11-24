import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import Emblem from './Emblem';
import HeaderSearch from './HeaderSearch';

const MENU = [
  { to: '/', label: '홈', end: true },
  { to: '/fixtures', label: '경기일정' },
  { to: '/standings', label: '순위' },
  { to: '/predict', label: '승부예측' },
  { to: '/ranking', label: '랭킹' },
  { to: '/news', label: '뉴스' },
  { to: '/community', label: '커뮤니티' },
  { to: '/transfers', label: '이적시장' },
  { to: '/overseas', label: '해외파' },
  { to: '/tacticboard', label: '전술판' },
];

export default function Header() {
  const { user, logout } = useAuth();

  return (
    <header className="header">
      <div className="container header__inner">
        <div className="header__top">
          <Link to="/" className="logo">
            <span className="logo__mark" aria-hidden="true">18</span>
            <span className="logo__text">FC1878</span>
          </Link>

          <div className="header__actions">
            <HeaderSearch />
            {user ? (
              <>
                <Link to="/me" className="user-chip">
                  {user.favoriteTeamLogo && <Emblem src={user.favoriteTeamLogo} name={user.favoriteTeamName} size={22} />}
                  <span>{user.nickname}</span>
                  <span className="user-chip__point">{user.point.toLocaleString()}P</span>
                </Link>
                <button type="button" className="btn btn--ghost" onClick={logout}>로그아웃</button>
              </>
            ) : (
              <>
                <Link to="/signup" className="btn btn--ghost">회원가입</Link>
                <Link to="/login" className="btn btn--primary">로그인</Link>
              </>
            )}
          </div>
        </div>

        <nav className="gnb" aria-label="주 메뉴">
          {MENU.map((m) => (
            <NavLink key={m.to} to={m.to} end={m.end}
                     className={({ isActive }) => 'gnb__link' + (isActive ? ' is-active' : '')}>
              {m.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </header>
  );
}
