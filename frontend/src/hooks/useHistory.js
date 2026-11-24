import { useCallback, useRef, useState } from 'react';

const LIMIT = 100;

/**
 * 실행 취소 / 다시 실행이 되는 상태.
 * - set(next): 기록을 남기고 바꿈 (버튼 클릭 같은 한 번에 끝나는 변경)
 * - checkpoint() 후 replace(next): 드래그처럼 여러 번 바뀌는 변경을 한 번의 기록으로 묶음
 */
export function useHistory(initial) {
  const [state, setState] = useState({ past: [], present: initial, future: [] });
  const presentRef = useRef(state.present);
  presentRef.current = state.present;

  const set = useCallback((next) => setState((s) => {
    const value = typeof next === 'function' ? next(s.present) : next;
    if (value === s.present) return s;
    return { past: [...s.past, s.present].slice(-LIMIT), present: value, future: [] };
  }), []);

  const checkpoint = useCallback(() => setState((s) => (
    { past: [...s.past, s.present].slice(-LIMIT), present: s.present, future: [] })), []);

  const replace = useCallback((next) => setState((s) => (
    { ...s, present: typeof next === 'function' ? next(s.present) : next })), []);

  const undo = useCallback(() => setState((s) => (s.past.length
    ? { past: s.past.slice(0, -1), present: s.past[s.past.length - 1], future: [s.present, ...s.future] } : s)), []);

  const redo = useCallback(() => setState((s) => (s.future.length
    ? { past: [...s.past, s.present], present: s.future[0], future: s.future.slice(1) } : s)), []);

  /** 드래그를 시작했는데 실제로 바뀐 게 없으면 방금 남긴 기록을 지움 */
  const dropCheckpointIfUnchanged = useCallback(() => setState((s) => (
    s.past.length && s.past[s.past.length - 1] === s.present ? { ...s, past: s.past.slice(0, -1) } : s)), []);

  return {
    state: state.present, set, replace, checkpoint, dropCheckpointIfUnchanged, undo, redo,
    canUndo: state.past.length > 0, canRedo: state.future.length > 0, presentRef,
  };
}
