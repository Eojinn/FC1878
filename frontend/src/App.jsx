import { Route, Routes } from 'react-router-dom';
import Footer from './components/Footer';
import Header from './components/Header';
import { LoginPage, SignupPage } from './pages/AuthPages';
import CommunityPage from './pages/CommunityPage';
import ComingSoonPage from './pages/ComingSoonPage';
import FixtureDetailPage from './pages/FixtureDetailPage';
import FixturesPage from './pages/FixturesPage';
import HomePage from './pages/HomePage';
import MyPage from './pages/MyPage';
import NewsPage from './pages/NewsPage';
import SearchPage from './pages/SearchPage';
import TeamPage from './pages/TeamPage';
import PlayerPage from './pages/PlayerPage';
import OAuthCallbackPage from './pages/OAuthCallbackPage';
import OverseasPage from './pages/OverseasPage';
import PostDetailPage from './pages/PostDetailPage';
import PostWritePage from './pages/PostWritePage';
import RankingPage from './pages/RankingPage';
import StandingsPage from './pages/StandingsPage';
import TacticBoardPage from './pages/TacticBoardPage';
import TransfersPage from './pages/TransfersPage';

export default function App() {
  return (
    <div className="app">
      <Header />
      <main className="container main">
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
          <Route path="/transfers" element={<TransfersPage />} />
          <Route path="/overseas" element={<OverseasPage />} />
          <Route path="/tacticboard" element={<TacticBoardPage />} />
          <Route path="*" element={<ComingSoonPage title="페이지를 찾을 수 없어요" />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}
