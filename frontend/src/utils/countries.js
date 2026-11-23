/*
 * 해외파 지도용 국가 정보.
 * mapId: world-atlas 지도 데이터의 국가 번호(ISO 3166 숫자 코드).
 * 잉글랜드·스코틀랜드는 지도에서 둘 다 영국(826)으로 칠해짐.
 */
export const COUNTRY_MAP_ID = {
  US: 840, DE: 276, ES: 724, PT: 620, TR: 792, BE: 56, 'GB-ENG': 826, 'GB-SCT': 826,
  DK: 208, CH: 756, CN: 156, JP: 392, AE: 784, FR: 250, IT: 380, NL: 528, AT: 40,
  SA: 682, QA: 634, AU: 36, RS: 688, PL: 616, GR: 300, NO: 578, SE: 752,
};

export const KOREA_MAP_ID = 410;
export const SEOUL = [126.978, 37.5665]; // [경도, 위도]

/** 지도 보기 범위: [[서쪽 경도, 남쪽 위도], [동쪽 경도, 북쪽 위도]] */
export const MAP_VIEWS = [
  { key: 'world', label: '전체', bounds: null },
  { key: 'europe', label: '유럽', bounds: [[-18, 31], [32, 58]] },
  { key: 'asia', label: '아시아', bounds: [[48, 18], [146, 46]] },
  { key: 'america', label: '북중미', bounds: [[-125, 22], [-68, 50]] },
];

export const POSITION_LABEL = { GK: '골키퍼', DF: '수비수', MF: '미드필더', FW: '공격수' };
