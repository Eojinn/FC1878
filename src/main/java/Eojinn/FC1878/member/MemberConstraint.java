package Eojinn.FC1878.member;

import Eojinn.FC1878.global.error.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * 회원 테이블의 중복(UNIQUE) 에러를 사용자에게 보여줄 에러로 바꾼다.
 * 중복 검사(existsBy...)를 통과한 뒤 저장하기 전에 다른 요청이 같은 값을 먼저 저장하면,
 * DB 의 UNIQUE 제약이 막아 주는데 그때 나는 에러를 500 대신 409 로 돌려주기 위함.
 * MySQL 메시지 예: Duplicate entry 'a@b.com' for key 'member.email'
 */
final class MemberConstraint {

    private MemberConstraint() {
    }

    static ErrorCode toErrorCode(DataIntegrityViolationException e) {
        String msg = String.valueOf(e.getMostSpecificCause().getMessage());
        if (msg.contains("'member.email'") || msg.contains("key 'email'")) return ErrorCode.DUPLICATE_EMAIL;
        if (msg.contains("'member.nickname'") || msg.contains("key 'nickname'")) return ErrorCode.DUPLICATE_NICKNAME;
        return ErrorCode.DUPLICATE_DATA;
    }
}
