package politicConnect.auth;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordConstraint {
    String message() default "비밀번호 형식이 올바르지 않습니다. (영문 대/소문자, 특수문자 중 2가지 이상 조합, 10 ~ 16자)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
