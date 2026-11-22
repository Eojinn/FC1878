package Eojinn.FC1878.member;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** 요청 DTO 에 붙이면 PasswordPolicy 규칙으로 검사. 어긴 규칙에 맞는 문구를 돌려줌 */
@Documented
@Constraint(validatedBy = StrongPassword.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default "비밀번호 규칙을 확인해 주세요.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<StrongPassword, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) return true; // 비어 있는지는 @NotBlank 가 검사
            return PasswordPolicy.violation(value).map(reason -> {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(reason).addConstraintViolation();
                return false;
            }).orElse(true);
        }
    }
}
