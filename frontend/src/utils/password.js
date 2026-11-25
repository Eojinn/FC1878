/*
 * 비밀번호 규칙 (백엔드 PasswordPolicy.java 와 같은 규칙).
 * 화면에서 바로 보여주기 위한 것이고, 최종 검사는 서버에서 다시 함.
 */
const SEQUENCES = ['abcdefghijklmnopqrstuvwxyz', '0123456789', 'qwertyuiop', 'asdfghjkl', 'zxcvbnm'];
const COMMON_WORDS = ['password', 'passwd', 'qwerty', 'iloveyou', 'admin', 'letmein', 'welcome',
  'football', 'soccer', 'fc1878', 'monkey', 'dragon', 'sunshine'];

const kindsOf = (pw) =>
  [/[A-Za-z]/, /[0-9]/, /[^A-Za-z0-9]/].filter((re) => re.test(pw)).length;

const hasTriple = (pw) => /(.)\1\1/.test(pw);

const hasSequence = (pw) => {
  const lower = pw.toLowerCase();
  for (let i = 0; i + 4 <= lower.length; i++) {
    const part = lower.slice(i, i + 4);
    const rev = [...part].reverse().join('');
    if (SEQUENCES.some((s) => s.includes(part) || s.includes(rev))) return true;
  }
  return false;
};

const hasCommonWord = (pw) => {
  const lower = pw.toLowerCase();
  const unleet = lower.replace(/0/g, 'o').replace(/1/g, 'i').replace(/3/g, 'e').replace(/4/g, 'a')
    .replace(/5/g, 's').replace(/@/g, 'a').replace(/\$/g, 's').replace(/!/g, 'i');
  return COMMON_WORDS.some((w) => lower.includes(w) || unleet.includes(w));
};

const hasPersonalInfo = (pw, email, nickname) => {
  const lower = pw.toLowerCase();
  const id = (email || '').split('@')[0].toLowerCase();
  const nick = (nickname || '').toLowerCase().replace(/\s/g, '');
  return (id.length >= 3 && email?.includes('@') && lower.includes(id)) || (nick.length >= 3 && lower.includes(nick));
};

/** 화면 체크리스트용 규칙 목록 */
export function passwordChecks(pw, { email, nickname } = {}) {
  const value = pw || '';
  return [
    { label: '8~64자', ok: value.length >= 8 && value.length <= 64 },
    { label: '영문·숫자·특수문자 중 2가지 이상', ok: kindsOf(value) >= 2 },
    { label: '공백·한글 없이', ok: value.length > 0 && /^[\x21-\x7E]+$/.test(value) },
    { label: '같은 문자 3번 연속(aaa), 연속된 4자리(1234·abcd·qwer) 없음', ok: value.length > 0 && !hasTriple(value) && !hasSequence(value) },
    { label: '흔한 단어(password 등), 이메일 아이디·닉네임 없음', ok: value.length > 0 && !hasCommonWord(value) && !hasPersonalInfo(value, email, nickname) },
  ];
}

export const isPasswordValid = (pw, info) => passwordChecks(pw, info).every((c) => c.ok);

/** 강도: 0 없음, 1 약함, 2 보통, 3 강함 */
export function passwordStrength(pw, info) {
  if (!pw) return 0;
  if (!isPasswordValid(pw, info)) return 1;
  return pw.length >= 12 && kindsOf(pw) === 3 ? 3 : 2;
}
