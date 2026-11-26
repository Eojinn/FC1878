import { useState } from 'react';

const COLORS = ['#C8102E', '#132257', '#034694', '#3D195B', '#0B3D91', '#2F3A4A', '#7A1F3D', '#1D6B4F'];

/**
 * 리그/팀 엠블럼. logoUrl(데이터 제공처의 엠블럼 이미지)이 있으면 이미지,
 * 없거나 불러오기 실패하면 이름 약자가 들어간 원으로 대체.
 * light: 로고 뒤쪽 주변을 동그랗게 밝혀서 어두운 로고도 검은 배경에서 잘 보이게 (로고 이미지는 그대로)
 */
export default function Emblem({ src, name = '', size = 28, rounded = false, light = false }) {
  const [broken, setBroken] = useState(false);
  const style = { width: size, height: size };

  if (src && !broken && light) {
    return (
      <span className="emblem emblem--light" style={{ ...style, padding: Math.max(2, Math.round(size * 0.14)) }}>
        <img src={src} alt="" loading="lazy" onError={() => setBroken(true)} />
      </span>
    );
  }
  if (src && !broken) {
    return (
      <img className={'emblem' + (rounded ? ' emblem--rounded' : '')} src={src} alt="" style={style}
           loading="lazy" onError={() => setBroken(true)} />
    );
  }
  const code = name.replace(/\s/g, '').slice(0, 2);
  const color = COLORS[[...name].reduce((a, c) => a + c.charCodeAt(0), 0) % COLORS.length];
  return (
    <span className="emblem emblem--fallback" aria-hidden="true"
          style={{ ...style, background: color, fontSize: Math.round(size * 0.38) }}>{code}</span>
  );
}
