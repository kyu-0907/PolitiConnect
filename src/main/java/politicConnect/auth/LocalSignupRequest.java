package politicConnect.auth;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import politicConnect.user.RegionCity;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "로컬 회원 가입 요청 DTO")
public class LocalSignupRequest {

    private String localLoginId;

    @PasswordConstraint
    private String localLoginPassword;
    private RegionCity regionCity;
    private String nickName;
    private String email;

}
