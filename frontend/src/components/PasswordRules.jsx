import { passwordChecks, passwordStrength } from '../utils/password';

const LEVEL = [
  null,
  { text: '약함', cls: 'is-weak' },
  { text: '보통', cls: 'is-normal' },
  { text: '강함', cls: 'is-strong' },
];

/** 비밀번호 입력칸 아래에 붙이는 강도 표시 + 규칙 체크리스트 */
export default function PasswordRules({ password, email, nickname, id }) {
  const info = { email, nickname };
  const level = LEVEL[passwordStrength(password, info)];
  return (
    <div className="pw-rules" id={id} aria-live="polite">
      {level && (
        <div className={'pw-meter ' + level.cls}>
          <span className="pw-meter__bar"><span /></span>
          <span className="pw-meter__text">비밀번호 강도: {level.text}</span>
        </div>
      )}
      <ul className="pw-checks">
        {passwordChecks(password, info).map((c) => (
          <li key={c.label} className={c.ok ? 'is-ok' : ''}>
            <span aria-hidden="true">{c.ok ? '✓' : '·'}</span>
            <span className="sr-only">{c.ok ? '충족: ' : '미충족: '}</span>
            {c.label}
          </li>
        ))}
      </ul>
    </div>
  );
}
