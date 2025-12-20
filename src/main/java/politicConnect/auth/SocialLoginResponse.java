package politicConnect.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "소셜 로그인 응답 DTO")
public class SocialLoginResponse {

    private boolean isSignedUp;
    private Provider provider;
    private String socialId;
    private TokenDto tokenDto;
}
