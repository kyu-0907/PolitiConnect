package politicConnect.auth;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TokenDto {
    private String accessToken;
    private Long accessTokenExpiresIn;
    private String refreshToken;
}
