package politicConnect.auth;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import politicConnect.auth.PasswordConstraint;


import java.util.regex.Pattern;

// 1. abstract 제거
public class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {

    // 정규식 미리 컴파일 (성능 최적화)
    private static final Pattern LETTER_PATTERN = Pattern.compile("[a-zA-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    @Override
    // 2. 두 번째 파라미터 타입을 ConstraintValidatorContext로 수정
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // null 처리는 @NotNull 어노테이션과 분리하는 것이 관례지만,
        // 여기서 필수값으로 처리하고 싶다면 false 유지.
        if (password == null) {
            return false;
        }

        // 길이 체크
        if (password.length() < 10 || password.length() > 16) {
            return false;
        }

        // 3. matcher().find()를 사용하여 부분 일치 확인 (matches는 전체 일치라 패턴 수정 필요함)
        // 기존 코드의 ".*[...].*" 패턴보다 find()가 더 직관적이고 빠름
        boolean hasUpperLower = LETTER_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecial = SPECIAL_PATTERN.matcher(password).find();

        int count = 0;
        if (hasUpperLower) count++;
        if (hasDigit) count++;
        if (hasSpecial) count++;

        // 2가지 이상 조합 만족 시 true
        return count >= 2;
    }
}