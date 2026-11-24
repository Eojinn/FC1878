package Eojinn.FC1878.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NEWS_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "뉴스를 불러오지 못했어요. 잠시 후 다시 시도해 주세요."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    RESERVED_NICKNAME(HttpStatus.BAD_REQUEST, "사용할 수 없는 닉네임입니다."),
    DUPLICATE_DATA(HttpStatus.CONFLICT, "이미 등록된 정보와 겹칩니다. 잠시 후 다시 시도해 주세요."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    LOGIN_LOCKED(HttpStatus.TOO_MANY_REQUESTS, "로그인 시도가 너무 많아요. 잠시 후 다시 시도해 주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다. 다시 로그인해 주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다."),
    SOCIAL_ONLY_ACCOUNT(HttpStatus.BAD_REQUEST, "소셜 로그인으로 가입한 계정은 비밀번호가 없어요."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호 규칙을 확인해 주세요."),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호가 지금 비밀번호와 같습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
    LEAGUE_NOT_FOUND(HttpStatus.NOT_FOUND, "리그를 찾을 수 없습니다."),
    FIXTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "경기를 찾을 수 없습니다."),
    PREDICTION_CLOSED(HttpStatus.BAD_REQUEST, "킥오프 이후에는 예측할 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
