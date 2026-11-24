import { request } from './client';

export const api = {
  // 인증 / 회원
  signup: (body) => request('/auth/signup', { method: 'POST', body }),
  login: (body) => request('/auth/login', { method: 'POST', body }),
  logout: () => request('/auth/logout', { method: 'POST' }),
  providers: () => request('/auth/providers'),
  me: () => request('/members/me'),
  myRank: () => request('/members/me/rank'),
  setFavoriteTeam: (teamId) => request('/members/me/favorite-team', { method: 'PUT', body: { teamId } }),
  changeNickname: (nickname) => request('/members/me/nickname', { method: 'PATCH', body: { nickname } }),
  changePassword: (currentPassword, newPassword) =>
    request('/members/me/password', { method: 'PATCH', body: { currentPassword, newPassword } }),
  withdraw: (password) => request('/members/me/withdraw', { method: 'POST', body: { password } }),

  // 축구 데이터
  leagues: () => request('/leagues'),
  teams: () => request('/teams'),
  squad: (teamId) => request(`/teams/${teamId}/squad`),
  search: (q, limit = 20) => request('/search', { params: { q, limit } }),
  teamProfile: (id) => request(`/teams/${id}/profile`),
  player: (id) => request(`/players/${id}`),
  fixtures: (params) => request('/fixtures', { params }),
  fixture: (id) => request(`/fixtures/${id}`),
  featured: () => request('/fixtures/featured'),
  standings: (leagueId, season) => request(`/leagues/${leagueId}/standings`, { params: { season } }),

  // 승부예측 / 랭킹
  predictionStats: (fixtureId) => request(`/fixtures/${fixtureId}/predictions`),
  predict: (fixtureId, pick) => request(`/fixtures/${fixtureId}/predictions`, { method: 'POST', body: { pick } }),
  rankings: (size = 50) => request('/rankings', { params: { size } }),

  // 해외파
  overseas: () => request('/overseas'),

  // 해외축구 뉴스
  news: (params) => request('/news', { params }),

  // 이적시장
  transfers: (params) => request('/transfers', { params }),

  // 커뮤니티
  posts: (params) => request('/posts', { params }),
  post: (id) => request(`/posts/${id}`),
  createPost: (body) => request('/posts', { method: 'POST', body }),
  updatePost: (id, body) => request(`/posts/${id}`, { method: 'PUT', body }),
  deletePost: (id) => request(`/posts/${id}`, { method: 'DELETE' }),
  comments: (postId) => request(`/posts/${postId}/comments`),
  createComment: (postId, content) => request(`/posts/${postId}/comments`, { method: 'POST', body: { content } }),
  deleteComment: (id) => request(`/comments/${id}`, { method: 'DELETE' }),
};
