/*
 * 인증 흐름
 * - 액세스 토큰(30분): 메모리에만 저장 → 새로고침하면 사라지지만, 아래 refresh 로 바로 다시 받아옴
 * - 리프레시 토큰(로그인 유지): 서버가 HttpOnly 쿠키로 관리 → 자바스크립트로 읽을 수 없어 XSS 에 안전
 * - 액세스 토큰이 만료되기 1분 전에 자동 갱신하고, 혹시 401 이 오면 한 번 갱신 후 요청을 다시 보냄
 */

let accessToken = null;
let refreshTimer = null;
let refreshing = null;

// 예전 버전이 localStorage 에 저장해 둔 토큰 정리
try { localStorage.removeItem('fc1878.token'); } catch { /* ignore */ }

export const tokenStore = {
  get: () => accessToken,
  set: (t) => { accessToken = t; },
  clear: () => {
    accessToken = null;
    if (refreshTimer) clearTimeout(refreshTimer);
    refreshTimer = null;
  },
};

/** 만료 1분 전에 미리 갱신 예약 */
export function scheduleRefresh(expiresInSeconds) {
  if (refreshTimer) clearTimeout(refreshTimer);
  if (!expiresInSeconds) return;
  const delay = Math.max(10, expiresInSeconds - 60) * 1000;
  refreshTimer = setTimeout(() => { refreshAccessToken(); }, delay);
}

/** 쿠키의 리프레시 토큰으로 새 액세스 토큰 받기. 동시에 여러 번 불려도 요청은 1번만 */
export function refreshAccessToken() {
  if (!refreshing) {
    refreshing = fetch('/api/auth/refresh', {
      method: 'POST',
      credentials: 'same-origin',
      headers: { Accept: 'application/json' },
    })
      .then(async (res) => {
        if (!res.ok) { tokenStore.clear(); return false; }
        const data = await res.json();
        tokenStore.set(data.accessToken);
        scheduleRefresh(data.expiresIn);
        return true;
      })
      .catch(() => false)
      .finally(() => { refreshing = null; });
  }
  return refreshing;
}

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.message || `요청 실패 (${status})`);
    this.status = status;
    this.body = body;
  }
}

/** fetch 래퍼: JSON 직렬화, JWT 헤더, 만료 시 자동 갱신 후 재요청, 에러 변환 */
export async function request(path, { method = 'GET', body, params } = {}, retried = false) {
  const url = new URL('/api' + path, window.location.origin);
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v);
    });
  }
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const hadToken = !!accessToken;
  if (hadToken) headers.Authorization = `Bearer ${accessToken}`;

  const res = await fetch(url, {
    method, headers, credentials: 'same-origin',
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401 && !retried && !path.startsWith('/auth/')) {
    if (await refreshAccessToken()) return request(path, { method, body, params }, true);
    if (hadToken) window.dispatchEvent(new Event('fc1878:logout'));
  }

  if (res.status === 204) return null;
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) throw new ApiError(res.status, data);
  return data;
}
