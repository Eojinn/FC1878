import { api } from '../api/api';
import { POPULAR_LEAGUE_ORDER } from '../utils/format';
import { useAsync } from './useAsync';

let cache = null;

/** 리그 목록 (인기 리그 순서로 정렬, 한 번만 불러옴) */
export function useLeagues() {
  const { data, loading, error } = useAsync(async () => {
    if (!cache) {
      const list = await api.leagues();
      const rank = (l) => {
        const i = POPULAR_LEAGUE_ORDER.indexOf(l.externalId);
        return i === -1 ? 999 : i;
      };
      cache = [...list].sort((a, b) => rank(a) - rank(b));
    }
    return cache;
  }, []);
  return { leagues: data || [], loading, error };
}
