import { useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../auth/AuthContext';
import PasswordRules from '../components/PasswordRules';
import SocialLogin from '../components/SocialLogin';
import { isPasswordValid } from '../utils/password';

const firstError = (err) => Object.values(err.body?.fieldErrors || {})[0] || err.message;

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '', rememberMe: false });
  const [params] = useSearchParams();
  const [error, setError] = useState(params.get('error') || '');

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try { await login(form.email, form.password, form.rememberMe); navigate(location.state?.from || '/', { replace: true }); }
    catch (err) { setError(firstError(err)); }
  };

  return (
    <form className="card form auth-form" onSubmit={submit}>
      <h1 className="h1">로그인</h1>
      <SocialLogin />
      <label className="field"><span>이메일</span>
        <input type="email" required autoComplete="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
      </label>
      <label className="field"><span>비밀번호</span>
        <input type="password" required autoComplete="current-password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
      </label>
      <label className="check">
        <input type="checkbox" checked={form.rememberMe} onChange={(e) => setForm({ ...form, rememberMe: e.target.checked })} />
        <span>로그인 상태 유지</span>
      </label>
      {form.rememberMe && <p className="muted small">브라우저를 닫아도 14일 동안 로그인이 유지돼요. 공용 PC에서는 체크하지 마세요.</p>}
      {error && <p className="error-text">{error}</p>}
      <button type="submit" className="btn btn--primary btn--block">로그인</button>
      <p className="muted center">아직 회원이 아니신가요? <Link to="/signup">회원가입</Link></p>
    </form>
  );
}

export function SignupPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '', nickname: '' });
  const [error, setError] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.signup(form);
      await login(form.email, form.password);
      navigate('/me', { replace: true });
    } catch (err) { setError(firstError(err)); }
  };

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  return (
    <form className="card form auth-form" onSubmit={submit}>
      <h1 className="h1">회원가입</h1>
      <SocialLogin />
      <label className="field"><span>이메일</span>
        <input type="email" required autoComplete="email" value={form.email} onChange={set('email')} />
      </label>
      <label className="field"><span>닉네임 (2~20자)</span>
        <input required minLength={2} maxLength={20} value={form.nickname} onChange={set('nickname')} />
      </label>
      <label className="field"><span>비밀번호</span>
        <input type="password" required minLength={8} maxLength={64} autoComplete="new-password"
               aria-describedby="signup-pw-rules" value={form.password} onChange={set('password')} />
      </label>
      <PasswordRules id="signup-pw-rules" password={form.password} email={form.email} nickname={form.nickname} />
      {error && <p className="error-text">{error}</p>}
      <button type="submit" className="btn btn--primary btn--block"
              disabled={!isPasswordValid(form.password, { email: form.email, nickname: form.nickname })}>가입하기</button>
      <p className="muted center">이미 계정이 있나요? <Link to="/login">로그인</Link></p>
    </form>
  );
}
