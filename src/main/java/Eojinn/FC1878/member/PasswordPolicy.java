package Eojinn.FC1878.member;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 비밀번호 규칙 (프론트 utils/password.js 와 같은 규칙).
 * 1. 8~64자
 * 2. 영문·숫자·특수문자만, 공백 없이
 * 3. 영문·숫자·특수문자 중 2가지 이상 섞기
 * 4. 같은 문자 3번 연속 금지 (aaa, 111)
 * 5. 연속된 문자·키보드 배열 4자리 금지 (1234, abcd, qwer, 4321)
 * 6. 흔한 단어 금지 (password, qwerty, football …)
 * 7. 이메일 아이디·닉네임 포함 금지 (회원가입·비밀번호 변경 때 서비스에서 따로 검사)
 */
public final class PasswordPolicy {

    public static final int MIN = 8;
    public static final int MAX = 64;

    private static final List<String> SEQUENCES = List.of(
            "abcdefghijklmnopqrstuvwxyz", "0123456789", "qwertyuiop", "asdfghjkl", "zxcvbnm");
    private static final List<String> COMMON_WORDS = List.of(
            "password", "passwd", "qwerty", "iloveyou", "admin", "letmein", "welcome",
            "football", "soccer", "fc1878", "monkey", "dragon", "sunshine");

    private PasswordPolicy() {
    }

    /** 규칙을 어기면 이유를, 통과하면 빈 값 */
    public static Optional<String> violation(String pw) {
        if (pw == null || pw.isEmpty()) return Optional.of("비밀번호를 입력해 주세요.");
        if (pw.length() < MIN || pw.length() > MAX) return Optional.of("비밀번호는 " + MIN + "~" + MAX + "자로 입력해 주세요.");
        if (!pw.chars().allMatch(c -> c > 32 && c < 127)) return Optional.of("비밀번호는 공백 없이 영문, 숫자, 특수문자만 쓸 수 있어요.");

        int kinds = 0;
        if (pw.chars().anyMatch(Character::isLetter)) kinds++;
        if (pw.chars().anyMatch(Character::isDigit)) kinds++;
        if (pw.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) kinds++;
        if (kinds < 2) return Optional.of("영문, 숫자, 특수문자 중 2가지 이상을 섞어 주세요.");

        for (int i = 2; i < pw.length(); i++) {
            if (pw.charAt(i) == pw.charAt(i - 1) && pw.charAt(i) == pw.charAt(i - 2)) {
                return Optional.of("같은 문자를 3번 이상 연속으로 쓸 수 없어요.");
            }
        }

        String lower = pw.toLowerCase(Locale.ROOT);
        for (int i = 0; i + 4 <= lower.length(); i++) {
            String part = lower.substring(i, i + 4);
            String reversed = new StringBuilder(part).reverse().toString();
            for (String seq : SEQUENCES) {
                if (seq.contains(part) || seq.contains(reversed)) {
                    return Optional.of("1234, abcd, qwer처럼 연속된 문자는 4자리 이상 쓸 수 없어요.");
                }
            }
        }

        // P@ssw0rd 처럼 글자를 숫자·기호로 바꾼 것도 잡기
        String unleet = lower.replace('0', 'o').replace('1', 'i').replace('3', 'e').replace('4', 'a')
                .replace('5', 's').replace('@', 'a').replace('$', 's').replace('!', 'i');
        for (String word : COMMON_WORDS) {
            if (lower.contains(word) || unleet.contains(word)) {
                return Optional.of("'" + word + "'처럼 추측하기 쉬운 단어는 쓸 수 없어요.");
            }
        }
        return Optional.empty();
    }

    /** 이메일 아이디(@ 앞)나 닉네임이 들어 있으면 이유를 돌려줌 (3자 이상일 때만 검사) */
    public static Optional<String> personalInfoViolation(String pw, String email, String nickname) {
        if (pw == null) return Optional.empty();
        String lower = pw.toLowerCase(Locale.ROOT);
        if (email != null && email.contains("@")) {
            String id = email.substring(0, email.indexOf('@')).toLowerCase(Locale.ROOT);
            if (id.length() >= 3 && lower.contains(id)) return Optional.of("비밀번호에 이메일 아이디를 넣을 수 없어요.");
        }
        if (nickname != null) {
            String nick = nickname.toLowerCase(Locale.ROOT).replace(" ", "");
            if (nick.length() >= 3 && lower.contains(nick)) return Optional.of("비밀번호에 닉네임을 넣을 수 없어요.");
        }
        return Optional.empty();
    }
}
