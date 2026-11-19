import { request } from './client';

export const api = {
  // 인증 / 회원
  signup: (body) => request('/auth/signup', { method: 'POST', body }),
  login: (body) => request('/auth/login', { method: 'POST', body }),
  me: () => request('/members/me'),
  myRank: () => request('/members/me/rank'),
  setFavoriteTeam: (teamId) => request('/members/me/favorite-team', { method: 'PUT', body: { teamId } }),

  // 축구 데이터
  leagues: () => request('/leagues'),
  teams: () => request('/teams'),
  fixtures: (params) => request('/fixtures', { params }),
  fixture: (id) => request(`/fixtures/${id}`),
  featured: () => request('/fixtures/featured'),
  standings: (leagueId, season) => request(`/leagues/${leagueId}/standings`, { params: { season } }),

  // 승부예측 / 랭킹
  predictionStats: (fixtureId) => request(`/fixtures/${fixtureId}/predictions`),
  predict: (fixtureId, pick) => request(`/fixtures/${fixtureId}/predictions`, { method: 'POST', body: { pick } }),
  rankings: (size = 50) => request('/rankings', { params: { size } }),

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
