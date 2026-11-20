import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api } from '../api/api';
import { refreshAccessToken, scheduleRefresh, tokenStore } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  /** 내 정보 다시 불러오기 (포인트 등이 바뀌었을 때) */
  const refresh = useCallback(async () => {
    if (!tokenStore.get()) { setUser(null); return; }
    try { setUser(await api.me()); } catch { setUser(null); }
  }, []);

  useEffect(() => {
    // 처음 열었을 때: 리프레시 쿠키가 있으면 로그인 상태 복구
    (async () => {
      if (await refreshAccessToken()) await refresh();
      setLoading(false);
    })();
    const onLogout = () => { tokenStore.clear(); setUser(null); };
    window.addEventListener('fc1878:logout', onLogout);
    return () => window.removeEventListener('fc1878:logout', onLogout);
  }, [refresh]);

  const login = async (email, password, rememberMe = false) => {
    const { accessToken, expiresIn } = await api.login({ email, password, rememberMe });
    tokenStore.set(accessToken);
    scheduleRefresh(expiresIn);
    await refresh();
  };

  const logout = async () => {
    try { await api.logout(); } catch { /* 이미 만료돼도 괜찮음 */ }
    tokenStore.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refresh, setUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
