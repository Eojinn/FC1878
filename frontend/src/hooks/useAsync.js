import { useCallback, useEffect, useState } from 'react';

/** 간단한 데이터 로딩 훅: { data, error, loading, reload, setData } */
export function useAsync(fn, deps = []) {
  const [state, setState] = useState({ data: null, error: null, loading: true });

  const load = useCallback(() => {
    let alive = true;
    setState((s) => ({ ...s, loading: true, error: null }));
    fn().then(
      (data) => alive && setState({ data, error: null, loading: false }),
      (error) => alive && setState({ data: null, error, loading: false }),
    );
    return () => { alive = false; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => load(), [load]);

  const setData = (updater) =>
    setState((s) => ({ ...s, data: typeof updater === 'function' ? updater(s.data) : updater }));

  return { ...state, reload: load, setData };
}
