const TOKEN_KEY = 'fc1878.token';

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.message || `요청 실패 (${status})`);
    this.status = status;
    this.body = body;
  }
}

/** fetch 래퍼: JSON 직렬화, JWT 헤더, 에러 변환 */
export async function request(path, { method = 'GET', body, params } = {}) {
  const url = new URL('/api' + path, window.location.origin);
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v);
    });
  }
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const token = tokenStore.get();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(url, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined });
  if (res.status === 401 && token) {
    tokenStore.clear(); // 만료된 토큰 정리
    window.dispatchEvent(new Event('fc1878:logout'));
  }
  if (res.status === 204) return null;
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) throw new ApiError(res.status, data);
  return data;
}
