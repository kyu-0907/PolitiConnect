package politicConnect.auth;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "로컬 로그인 요청 DTO")
public class LocalLoginRequest {

    private String localLoginId;
    private String localLoginPassword;


}
