import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api } from '../api/api';
import { tokenStore } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(!!tokenStore.get());

  const refresh = useCallback(async () => {
    if (!tokenStore.get()) { setUser(null); setLoading(false); return; }
    try { setUser(await api.me()); } catch { setUser(null); } finally { setLoading(false); }
  }, []);

  useEffect(() => {
    refresh();
    const onLogout = () => setUser(null);
    window.addEventListener('fc1878:logout', onLogout);
    return () => window.removeEventListener('fc1878:logout', onLogout);
  }, [refresh]);

  const login = async (email, password) => {
    const { accessToken } = await api.login({ email, password });
    tokenStore.set(accessToken);
    await refresh();
  };

  const logout = () => { tokenStore.clear(); setUser(null); };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refresh, setUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
