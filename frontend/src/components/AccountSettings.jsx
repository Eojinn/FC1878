import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../auth/AuthContext';

const firstError = (err) => Object.values(err.body?.fieldErrors || {})[0] || err.message;

/** 성공/실패 메시지 한 줄 */
function Message({ msg }) {
  if (!msg) return null;
  return <p className={msg.ok ? 'ok-text' : 'error-text'} role="status">{msg.text}</p>;
}

function NicknameForm() {
  const { user, setUser } = useAuth();
  const [nickname, setNickname] = useState(user.nickname);
  const [msg, setMsg] = useState(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setMsg(null); setBusy(true);
    try {
      setUser(await api.changeNickname(nickname));
      setMsg({ ok: true, text: '닉네임을 바꿨어요.' });
    } catch (err) {
      setMsg({ ok: false, text: firstError(err) });
    } finally { setBusy(false); }
  };

  return (
    <form className="account-block" onSubmit={submit}>
      <h3 className="h4">닉네임 변경</h3>
      <div className="inline-field">
        <label className="sr-only" htmlFor="nickname">새 닉네임</label>
        <input id="nickname" required minLength={2} maxLength={20} value={nickname}
               onChange={(e) => setNickname(e.target.value)} />
        <button type="submit" className="btn btn--primary" disabled={busy || nickname.trim() === user.nickname}>변경</button>
      </div>
      <p className="muted small">2~20자. 게시글과 랭킹에 보이는 이름이에요.</p>
      <Message msg={msg} />
    </form>
  );
}

function PasswordForm() {
  const [form, setForm] = useState({ current: '', next: '', confirm: '' });
  const [msg, setMsg] = useState(null);
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setMsg(null);
    if (form.next !== form.confirm) { setMsg({ ok: false, text: '새 비밀번호 확인이 일치하지 않아요.' }); return; }
    setBusy(true);
    try {
      await api.changePassword(form.current, form.next);
      setForm({ current: '', next: '', confirm: '' });
      setMsg({ ok: true, text: '비밀번호를 바꿨어요. 다음 로그인부터 새 비밀번호를 쓰세요.' });
    } catch (err) {
      setMsg({ ok: false, text: firstError(err) });
    } finally { setBusy(false); }
  };

  return (
    <form className="account-block" onSubmit={submit}>
      <h3 className="h4">비밀번호 변경</h3>
      <label className="field"><span>현재 비밀번호</span>
        <input type="password" required autoComplete="current-password" value={form.current} onChange={set('current')} />
      </label>
      <label className="field"><span>새 비밀번호 (8자 이상)</span>
        <input type="password" required minLength={8} maxLength={64} autoComplete="new-password" value={form.next} onChange={set('next')} />
      </label>
      <label className="field"><span>새 비밀번호 확인</span>
        <input type="password" required minLength={8} maxLength={64} autoComplete="new-password" value={form.confirm} onChange={set('confirm')} />
      </label>
      <button type="submit" className="btn btn--primary align-start" disabled={busy}>비밀번호 변경</button>
      <Message msg={msg} />
    </form>
  );
}

function WithdrawForm() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [password, setPassword] = useState('');
  const [agree, setAgree] = useState(false);
  const [msg, setMsg] = useState(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setMsg(null); setBusy(true);
    try {
      await api.withdraw(password);
      logout();
      navigate('/', { replace: true });
    } catch (err) {
      setMsg({ ok: false, text: firstError(err) });
      setBusy(false);
    }
  };

  if (!open) {
    return (
      <div className="account-block account-block--danger">
        <h3 className="h4">회원 탈퇴</h3>
        <p className="muted small">탈퇴하면 포인트와 랭킹이 사라지고 되돌릴 수 없어요.</p>
        <button type="button" className="btn btn--danger-ghost align-start" onClick={() => setOpen(true)}>탈퇴하기</button>
      </div>
    );
  }

  return (
    <form className="account-block account-block--danger" onSubmit={submit}>
      <h3 className="h4">회원 탈퇴</h3>
      <ul className="muted small notice-list">
        <li>포인트와 랭킹 기록이 사라지고, 다시 되돌릴 수 없어요.</li>
        <li>작성한 게시글·댓글은 지워지지 않고 작성자가 ‘탈퇴한회원’으로 표시돼요.</li>
        <li>같은 이메일로 나중에 다시 가입할 수 있어요.</li>
      </ul>
      <label className="field"><span>비밀번호 확인</span>
        <input type="password" required autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} />
      </label>
      <label className="check">
        <input type="checkbox" checked={agree} onChange={(e) => setAgree(e.target.checked)} />
        <span>위 내용을 확인했고 탈퇴에 동의합니다.</span>
      </label>
      <div className="row-actions">
        <button type="button" className="btn btn--ghost" onClick={() => { setOpen(false); setPassword(''); setAgree(false); setMsg(null); }}>취소</button>
        <button type="submit" className="btn btn--danger" disabled={!agree || busy}>탈퇴하기</button>
      </div>
      <Message msg={msg} />
    </form>
  );
}

/** 내 정보 화면의 계정 관리 영역 */
export default function AccountSettings() {
  return (
    <section className="card account" aria-labelledby="account-title">
      <h2 id="account-title" className="h3">계정 관리</h2>
      <NicknameForm />
      <PasswordForm />
      <WithdrawForm />
    </section>
  );
}
