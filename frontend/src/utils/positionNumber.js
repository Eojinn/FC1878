/*
 * 포지션별 대표 등번호.
 * 실제 선수 등번호 대신, 축구에서 흔히 쓰는 포지션 번호로 보여줌
 * (골키퍼 1, 오른쪽 풀백 2, 왼쪽 풀백 3, 센터백 4·5, 수비형 미드필더 6, 오른쪽 윙 7, 중앙 미드필더 8, 스트라이커 9, 공격형 미드필더 10, 왼쪽 윙 11)
 */

/** football-data.org 세부 포지션 이름 → 대표 번호 */
const BY_POSITION = {
  goalkeeper: 1,
  'right-back': 2, 'right wing-back': 2,
  'left-back': 3, 'left wing-back': 3,
  'centre-back': 4, defence: 4, defender: 4,
  'defensive midfield': 6,
  'right midfield': 7, 'right winger': 7,
  'central midfield': 8, midfield: 8, midfielder: 8,
  'centre-forward': 9, offence: 9, forward: 9, attacker: 9,
  'attacking midfield': 10, 'second striker': 10,
  'left midfield': 11, 'left winger': 11,
};

/** 역할(GK/DF/MF/FW)만 알 때 */
const BY_ROLE = { GK: 1, DF: 4, MF: 8, FW: 9 };

/** 선수 한 명의 대표 번호 (세부 포지션 → 없으면 역할) */
export function positionNumber(position, role) {
  const key = (position ?? '').trim().toLowerCase();
  return BY_POSITION[key] ?? BY_ROLE[role] ?? null;
}

/**
 * 포메이션 자리별 대표 번호 (GK 다음, 수비 → 공격 줄 순서, 각 줄은 피치 위쪽(왼쪽 측면)부터).
 * 예) 4-3-3 → 1 | 3 5 4 2 | 10 6 8 | 11 9 7
 */
const BY_FORMATION = {
  '4-3-3': [1, 3, 5, 4, 2, 10, 6, 8, 11, 9, 7],
  '4-4-2': [1, 3, 5, 4, 2, 11, 8, 6, 7, 10, 9],
  '4-2-3-1': [1, 3, 5, 4, 2, 8, 6, 11, 10, 7, 9],
  '4-1-4-1': [1, 3, 5, 4, 2, 6, 11, 10, 8, 7, 9],
  '4-4-1-1': [1, 3, 5, 4, 2, 11, 8, 6, 7, 10, 9],
  '4-3-1-2': [1, 3, 5, 4, 2, 8, 6, 7, 10, 11, 9],
  '4-5-1': [1, 3, 5, 4, 2, 11, 8, 6, 10, 7, 9],
  '3-5-2': [1, 5, 4, 6, 3, 10, 8, 7, 2, 11, 9],
  '3-4-3': [1, 5, 4, 6, 3, 8, 10, 2, 11, 9, 7],
  '3-4-2-1': [1, 5, 4, 6, 3, 8, 10, 2, 11, 7, 9],
  '5-3-2': [1, 3, 5, 4, 6, 2, 8, 10, 7, 11, 9],
  '5-4-1': [1, 3, 5, 4, 6, 2, 11, 8, 10, 7, 9],
};

/** 포메이션의 11개 자리 번호. 표에 없는 포메이션이면 1~11 */
export function slotNumbers(formation) {
  return BY_FORMATION[formation] ?? Array.from({ length: 11 }, (_, i) => i + 1);
}
