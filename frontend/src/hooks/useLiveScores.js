import { useEffect } from 'react';
import { Client } from '@stomp/stompjs';

/** 서버가 /topic/live 로 보내는 경기 변경을 받아 콜백 실행 (실시간 스코어) */
export function useLiveScores(onUpdate) {
  useEffect(() => {
    const proto = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const client = new Client({
      brokerURL: `${proto}://${window.location.host}/ws`,
      reconnectDelay: 10000,
      debug: () => {},
    });
    client.onConnect = () => {
      client.subscribe('/topic/live', (msg) => {
        try { onUpdate(JSON.parse(msg.body)); } catch { /* ignore */ }
      });
    };
    client.activate();
    return () => { client.deactivate(); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
}
