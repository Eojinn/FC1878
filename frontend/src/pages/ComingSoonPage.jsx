import { Link } from 'react-router-dom';

export default function ComingSoonPage({ title }) {
  return (
    <section className="card coming-soon page-narrow">
      <h1 className="h1">{title}</h1>
      <p className="muted">준비 중인 메뉴예요. 곧 만나요!</p>
      <Link to="/" className="btn btn--primary">홈으로</Link>
    </section>
  );
}
