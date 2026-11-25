import { api } from '../api/api';
import { useAsync } from '../hooks/useAsync';

/*
 * 소셜 로그인 버튼. 서버에 키가 설정된 서비스만 보여줌.
 * 각 서비스 디자인 가이드의 공식 버튼 이미지(로고 포함)를 받아서 바꿔 끼우면 더 좋아요.
 *  - 카카오: https://developers.kakao.com/docs/latest/ko/kakaologin/design-guide
 *  - 구글:   https://developers.google.com/identity/branding-guidelines
 *  - 애플:   https://developer.apple.com/design/human-interface-guidelines/sign-in-with-apple
 */
const PROVIDERS = {
  kakao: { label: '카카오로 계속하기', className: 'social-btn--kakao' },
  google: { label: 'Google로 계속하기', className: 'social-btn--google' },
  apple: { label: 'Apple로 계속하기', className: 'social-btn--apple' },
};
const ORDER = ['kakao', 'google', 'apple'];

export default function SocialLogin() {
  const { data } = useAsync(() => api.providers(), []);
  const enabled = ORDER.filter((p) => data?.includes(p));
  if (!enabled.length) return null;

  return (
    <div className="social">
      {enabled.map((p) => (
        // 전체 페이지 이동: 백엔드가 각 서비스 로그인 화면으로 보내고, 끝나면 /oauth/callback 으로 돌아옴
        <a key={p} href={`/oauth2/authorization/${p}`} className={`social-btn ${PROVIDERS[p].className}`}>
          {PROVIDERS[p].label}
        </a>
      ))}
      <div className="divider"><span>또는 이메일로</span></div>
    </div>
  );
}
