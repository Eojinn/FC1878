import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 개발 중 /api, /ws 요청을 Spring Boot(8080)로 넘겨서 CORS 걱정 없이 개발
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      // 소셜 로그인: 시작(/oauth2/authorization/..)과 돌아오는 주소(/login/oauth2/code/..)도 백엔드로
      '/oauth2': 'http://localhost:8080',
      '/login/oauth2': 'http://localhost:8080',
      '/ws': { target: 'ws://localhost:8080', ws: true },
    },
  },
});
