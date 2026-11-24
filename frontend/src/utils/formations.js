/*
 * 전술판 포메이션 · 좌표 계산.
 * 피치 크기는 실제 규격처럼 가로 105, 세로 68 (단위: m).
 * 홈은 왼쪽 → 오른쪽으로 공격, 원정은 180도 돌려서 오른쪽 → 왼쪽으로 공격.
 */
export const PITCH_W = 105;
export const PITCH_H = 68;

export const FORMATIONS = [
  '4-3-3', '4-4-2', '4-2-3-1', '4-1-4-1', '4-4-1-1', '4-3-1-2', '4-5-1',
  '3-5-2', '3-4-3', '3-4-2-1', '5-3-2', '5-4-1',
];

/** "4-2-3-1" → GK 포함 11개 자리 [{ role, x, y }] (홈 기준 좌표) */
export function formationSlots(formation) {
  const lines = formation.split('-').map(Number);
  const slots = [{ role: 'GK', x: 5, y: PITCH_H / 2 }];
  const first = 16;
  const last = 47; // 하프라인(52.5) 조금 앞까지
  lines.forEach((count, i) => {
    const x = lines.length === 1 ? first : first + ((last - first) * i) / (lines.length - 1);
    const role = i === 0 ? 'DF' : i === lines.length - 1 ? 'FW' : 'MF';
    const span = Math.min(0.84, 0.2 * count) * PITCH_H;
    for (let j = 0; j < count; j++) {
      const y = count === 1 ? PITCH_H / 2 : (PITCH_H - span) / 2 + (span * j) / (count - 1);
      slots.push({ role, x, y });
    }
  });
  return slots;
}

/** 원정 팀은 피치를 180도 돌린 위치 */
export const mirror = ({ x, y }) => ({ x: PITCH_W - x, y: PITCH_H - y });

export function slotPosition(side, slot) {
  return side === 'away' ? mirror(slot) : { x: slot.x, y: slot.y };
}

/**
 * 선수들을 새 포메이션 자리에 배치. 같은 역할(GK/DF/MF/FW)끼리 먼저 맞추고 남는 선수는 순서대로.
 * players: [{ key, name, number, role }] (11명보다 적거나 많아도 됨)
 */
export function arrange(side, formation, players) {
  const slots = formationSlots(formation);
  const pool = [...players];
  const taken = new Array(slots.length).fill(null);
  // 1) 역할이 같은 선수
  slots.forEach((slot, i) => {
    const idx = pool.findIndex((p) => p.role === slot.role);
    if (idx >= 0) taken[i] = pool.splice(idx, 1)[0];
  });
  // 2) 빈 자리는 남은 선수 (GK 자리는 GK가 없을 때만)
  slots.forEach((slot, i) => {
    if (!taken[i] && pool.length) taken[i] = pool.shift();
  });
  return slots.map((slot, i) => {
    const p = taken[i] ?? { key: `${side}-empty-${i}`, name: '', number: i + 1, role: slot.role };
    return { ...p, slotRole: slot.role, ...slotPosition(side, slot) };
  });
}

/** 스쿼드가 없을 때 쓰는 번호만 있는 선수 11명 */
export function placeholderPlayers(side, formation) {
  return formationSlots(formation).map((slot, i) => ({
    key: `${side}-p${i + 1}`, name: '', number: i + 1, role: slot.role,
  }));
}

/** 피치에 표시할 짧은 이름: "Bukayo Saka" → "Saka" */
export function shortName(name) {
  if (!name) return '';
  const parts = name.trim().split(/\s+/);
  return parts.length > 1 ? parts.slice(1).join(' ') : parts[0];
}
