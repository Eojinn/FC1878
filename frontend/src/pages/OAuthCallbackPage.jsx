import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { refreshAccessToken } from '../api/client';
import { useAuth } from '../auth/AuthContext';

/** 소셜 로그인을 마치고 돌아오는 곳. 서버가 넣어 준 로그인 유지 쿠키로 액세스 토큰을 받아옴 */
export default function OAuthCallbackPage() {
  const { refresh } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      if (await refreshAccessToken()) {
        await refresh();
        navigate('/', { replace: true });
      } else {
        navigate('/login?error=' + encodeURIComponent('로그인 정보를 받지 못했어요. 다시 시도해 주세요.'), { replace: true });
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return <div className="state">로그인 중…</div>;
}
