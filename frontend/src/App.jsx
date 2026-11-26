import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router-dom';
import ChatWidget from './components/ChatWidget';
import Footer from './components/Footer';
import Header from './components/Header';
import HomePage from './pages/HomePage';

// 페이지는 들어갈 때 불러옴 (첫 화면 로딩을 가볍게: 세계 지도·전술판 코드 등은 그 페이지에서만)
const LoginPage = lazy(() => import('./pages/AuthPages').then((m) => ({ default: m.LoginPage })));
const SignupPage = lazy(() => import('./pages/AuthPages').then((m) => ({ default: m.SignupPage })));
const CommunityPage = lazy(() => import('./pages/CommunityPage'));
const ComingSoonPage = lazy(() => import('./pages/ComingSoonPage'));
const FixtureDetailPage = lazy(() => import('./pages/FixtureDetailPage'));
const FixturesPage = lazy(() => import('./pages/FixturesPage'));
const MyPage = lazy(() => import('./pages/MyPage'));
const NewsPage = lazy(() => import('./pages/NewsPage'));
const SearchPage = lazy(() => import('./pages/SearchPage'));
const TeamPage = lazy(() => import('./pages/TeamPage'));
const PlayerPage = lazy(() => import('./pages/PlayerPage'));
const CareerPage = lazy(() => import('./pages/CareerPage'));
const OAuthCallbackPage = lazy(() => import('./pages/OAuthCallbackPage'));
const OverseasPage = lazy(() => import('./pages/OverseasPage'));
const PostDetailPage = lazy(() => import('./pages/PostDetailPage'));
const PostWritePage = lazy(() => import('./pages/PostWritePage'));
const RankingPage = lazy(() => import('./pages/RankingPage'));
const StandingsPage = lazy(() => import('./pages/StandingsPage'));
const TacticBoardPage = lazy(() => import('./pages/TacticBoardPage'));
const TransfersPage = lazy(() => import('./pages/TransfersPage'));

export default function App() {
  return (
    <div className="app">
      <Header />
      <main className="container main">
        <Suspense fallback={<div className="state">불러오는 중…</div>}>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/fixtures" element={<FixturesPage />} />
            <Route path="/fixtures/:id" element={<FixtureDetailPage />} />
            <Route path="/predict" element={<FixturesPage predictMode />} />
            <Route path="/standings" element={<StandingsPage />} />
            <Route path="/ranking" element={<RankingPage />} />
            <Route path="/community" element={<CommunityPage />} />
            <Route path="/community/write" element={<PostWritePage />} />
            <Route path="/community/:id" element={<PostDetailPage />} />
            <Route path="/community/:id/edit" element={<PostWritePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
            <Route path="/me" element={<MyPage />} />
            <Route path="/news" element={<NewsPage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/teams/:id" element={<TeamPage />} />
            <Route path="/players/:id" element={<PlayerPage />} />
            <Route path="/career" element={<CareerPage />} />
            <Route path="/transfers" element={<TransfersPage />} />
            <Route path="/overseas" element={<OverseasPage />} />
            <Route path="/tacticboard" element={<TacticBoardPage />} />
            <Route path="*" element={<ComingSoonPage title="페이지를 찾을 수 없어요" />} />
          </Routes>
        </Suspense>
      </main>
      <Footer />
      <ChatWidget />
    </div>
  );
}
