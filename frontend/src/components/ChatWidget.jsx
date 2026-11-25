import { Fragment, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../auth/AuthContext';

/*
 * AI 축구 챗봇 (오른쪽 아래 버튼). 질문하면 서버가 우리 DB(일정·순위·선수·이적·해외파)를 조회해서 답함.
 * 대화는 이 탭에서만 기억 (sessionStorage). 서버에는 최근 10개만 보냄.
 */

const STORE_KEY = 'fc1878.chat';
const SUGGESTIONS = [
  '오늘 경기 일정 알려줘',
  '프리미어리그 순위 1~5위는?',
  '손흥민 이번 시즌 기록',
  '해외파 선수들 어디서 뛰어?',
  '최근 한국 선수 이적 소식',
];

function loadHistory() {
  try {
    const saved = JSON.parse(sessionStorage.getItem(STORE_KEY) || '[]');
    return Array.isArray(saved) ? saved : [];
  } catch {
    return [];
  }
}

/** **굵게** 만 처리 */
function inline(text) {
  return text.split(/(\*\*[^*]+\*\*)/g).map((part, i) =>
    part.startsWith('**') && part.endsWith('**') && part.length > 4
      ? <strong key={i}>{part.slice(2, -2)}</strong>
      : <Fragment key={i}>{part}</Fragment>);
}

/** 줄바꿈 · "- " 목록 · **굵게** 정도만 그리는 작은 마크다운 */
function RichText({ text }) {
  const blocks = [];
  let list = null;
  text.split('\n').forEach((raw) => {
    const line = raw.trimEnd();
    const item = line.match(/^\s*(?:[-*•]|\d+\.)\s+(.*)$/);
    if (item) {
      if (!list) { list = []; blocks.push({ type: 'ul', items: list }); }
      list.push(item[1]);
      return;
    }
    list = null;
    if (line.trim()) blocks.push({ type: 'p', text: line.replace(/^#+\s*/, '') });
  });
  return blocks.map((b, i) => (b.type === 'ul'
    ? <ul key={i}>{b.items.map((t, j) => <li key={j}>{inline(t)}</li>)}</ul>
    : <p key={i}>{inline(b.text)}</p>));
}

export default function ChatWidget() {
  const { user } = useAuth();
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState(loadHistory);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [status, setStatus] = useState(null);
  const listRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    try { sessionStorage.setItem(STORE_KEY, JSON.stringify(messages.slice(-30))); } catch { /* 저장 못 해도 괜찮음 */ }
  }, [messages]);

  // 열 때·로그인 상태가 바뀔 때 남은 질문 수 확인
  useEffect(() => {
    if (!open) return;
    api.chatStatus().then(setStatus, () => setStatus(null));
  }, [open, user?.id]);

  useEffect(() => {
    if (open) inputRef.current?.focus();
  }, [open]);

  useEffect(() => {
    listRef.current?.scrollTo({ top: listRef.current.scrollHeight, behavior: 'smooth' });
  }, [messages, sending, open]);

  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => { if (e.key === 'Escape') setOpen(false); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open]);

  const send = async (text) => {
    const q = text.trim();
    if (!q || sending) return;
    const next = [...messages, { role: 'user', text: q }];
    setMessages(next);
    setInput('');
    setSending(true);
    // 오류 안내 말풍선은 빼고, 실제 대화만 최근 10개
    const history = next.filter((m) => !m.notice).slice(-10).map(({ role, text: t }) => ({ role, text: t.slice(0, 2000) }));
    try {
      const res = await api.chat(history);
      setMessages((m) => [...m, {
        role: 'assistant', text: res.answer, links: res.links || [], notice: res.state !== 'OK',
      }]);
      if (res.remaining != null) setStatus((s) => (s ? { ...s, remaining: res.remaining } : s));
    } catch (e) {
      setMessages((m) => [...m, { role: 'assistant', text: e.message || '답변을 받지 못했어요.', notice: true }]);
    } finally {
      setSending(false);
    }
  };

  const onSubmit = (e) => {
    e.preventDefault();
    send(input);
  };

  const onKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey && !e.nativeEvent.isComposing) {
      e.preventDefault();
      send(input);
    }
  };

  const disabled = status && !status.enabled;

  return (
    <>
      {!open && (
        <button type="button" className="chatbot-fab" onClick={() => setOpen(true)} aria-label="AI 축구 챗봇 열기">
          <span className="chatbot-fab__icon" aria-hidden="true">⚽</span>
          <span>AI에게 묻기</span>
        </button>
      )}

      {open && (
        <section className="chatbot" role="dialog" aria-label="AI 축구 챗봇">
          <header className="chatbot__head">
            <div>
              <strong>FC봇</strong>
              <span className="chatbot__badge">AI</span>
              <p className="muted small">일정 · 순위 · 선수 기록 · 이적 · 해외파를 물어보세요</p>
            </div>
            <div className="chatbot__actions">
              {messages.length > 0 && (
                <button type="button" className="chatbot__icon-btn" onClick={() => setMessages([])} title="대화 지우기">새 대화</button>
              )}
              <button type="button" className="chatbot__icon-btn" onClick={() => setOpen(false)} aria-label="닫기">✕</button>
            </div>
          </header>

          <div className="chatbot__body" ref={listRef} aria-live="polite">
            {messages.length === 0 && (
              <div className="chatbot__intro">
                <p>안녕하세요! 사이트에 있는 축구 데이터를 찾아서 답해 드려요.</p>
                <div className="chatbot__suggest">
                  {SUGGESTIONS.map((s) => (
                    <button key={s} type="button" className="chip chip--sm" disabled={disabled || sending} onClick={() => send(s)}>{s}</button>
                  ))}
                </div>
              </div>
            )}
            {messages.map((m, i) => (
              <div key={i} className={`chatbot__msg chatbot__msg--${m.role}${m.notice ? ' is-notice' : ''}`}>
                <div className="chatbot__bubble">
                  {m.role === 'assistant' ? <RichText text={m.text} /> : m.text}
                </div>
                {m.links?.length > 0 && (
                  <div className="chatbot__links">
                    {m.links.map((l) => (
                      <Link key={l.url} to={l.url} className={`chatbot__link chatbot__link--${l.type}`}
                            onClick={() => { if (window.innerWidth <= 560) setOpen(false); }}>{l.label} →</Link>
                    ))}
                  </div>
                )}
              </div>
            ))}
            {sending && (
              <div className="chatbot__msg chatbot__msg--assistant">
                <div className="chatbot__bubble chatbot__typing" aria-label="답변 작성 중"><span /><span /><span /></div>
              </div>
            )}
          </div>

          <form className="chatbot__form" onSubmit={onSubmit}>
            <textarea ref={inputRef} rows={1} value={input} maxLength={500}
                      placeholder={disabled ? 'AI 챗봇이 아직 연결되지 않았어요' : '예) 아스널 다음 경기 언제야?'}
                      disabled={disabled} onChange={(e) => setInput(e.target.value)} onKeyDown={onKeyDown} />
            <button type="submit" className="btn btn--primary btn--sm" disabled={!input.trim() || sending || disabled}>보내기</button>
          </form>
          <p className="chatbot__foot muted small">
            {status?.enabled
              ? <>오늘 남은 질문 {status.remaining}/{status.dailyLimit}{!status.member && ' · 로그인하면 더 많이'}</>
              : status ? '관리자가 AI 키를 연결하면 쓸 수 있어요.' : ' '}
            <span> · AI 답변은 틀릴 수 있어요</span>
          </p>
        </section>
      )}
    </>
  );
}
