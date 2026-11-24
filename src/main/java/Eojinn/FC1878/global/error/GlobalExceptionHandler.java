package Eojinn.FC1878.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(code.name(), e.getMessage(), Map.of()));
    }

    /** 위에서 미처 못 잡은 DB 중복 에러도 500 대신 409 로 (예: 게시글·예측 중복 저장) */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("DB 제약 조건 위반: {}", e.getMostSpecificCause().getMessage());
        ErrorCode code = ErrorCode.DUPLICATE_DATA;
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
    }

    /** 없는 API 주소 → 404 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException e) {
        log.warn("없는 주소 요청: {}", e.getResourcePath());
        ErrorCode code = ErrorCode.NOT_FOUND;
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
    }

    /** 예상 못 한 오류 → 500 (원인은 서버 로그에 남김) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) throws Exception {
        // 스프링 보안의 인증·권한 예외는 원래대로 401·403 처리되도록 넘김
        if (e instanceof org.springframework.security.core.AuthenticationException
                || e instanceof org.springframework.security.access.AccessDeniedException) throw e;
        // 잘못된 파라미터·JSON·메서드 같은 스프링 MVC 예외는 원래 상태 코드(400·405 등) 유지
        if (e instanceof org.springframework.web.ErrorResponse mvc && !mvc.getStatusCode().is5xxServerError()) {
            return ResponseEntity.status(mvc.getStatusCode())
                    .body(new ErrorResponse(ErrorCode.INVALID_INPUT.name(), ErrorCode.INVALID_INPUT.getMessage(), Map.of()));
        }
        log.error("처리 중 오류", e);
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        ErrorCode code = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(code.name(), code.getMessage(), errors));
    }
}
