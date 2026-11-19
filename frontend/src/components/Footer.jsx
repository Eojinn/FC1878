import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer__inner">
        <div className="footer__brand">
          <strong className="logo__text">FC1878</strong>
          <span>축구 팬을 위한 올인원 커뮤니티 · Since 1878</span>
        </div>
        <nav className="footer__links" aria-label="서비스 정보">
          <Link to="/about">서비스 소개</Link>
          <Link to="/terms">이용약관</Link>
          <Link to="/privacy" className="is-strong">개인정보처리방침</Link>
          <Link to="/bug-report">버그 제보</Link>
        </nav>
        <small className="footer__copy">© 2026 FC1878. 경기 데이터 제공: API-Football</small>
      </div>
    </footer>
  );
}
