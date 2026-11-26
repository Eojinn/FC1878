import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 개발 중 /api, /ws 요청을 Spring Boot(8080)로 넘겨서 CORS 걱정 없이 개발
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      // 소셜 로그인: 시작(/oauth2/authorization/..)과 돌아오는 주소(/login/oauth2/code/..)도 백엔드로.
      // changeOrigin: false → 백엔드가 접속 주소를 localhost:5173 으로 알아야
      // 카카오·구글에 보내는 리다이렉트 URI 가 등록한 값(http://localhost:5173/login/oauth2/code/..)과 같아짐 (다르면 KOE006)
      '/oauth2': { target: 'http://localhost:8080', changeOrigin: false },
      '/login/oauth2': { target: 'http://localhost:8080', changeOrigin: false },
      '/ws': { target: 'ws://localhost:8080', ws: true },
    },
  },
});
