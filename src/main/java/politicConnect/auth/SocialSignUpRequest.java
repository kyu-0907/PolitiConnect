package politicConnect.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import politicConnect.user.RegionCity;

@Data
@Builder
@AllArgsConstructor
public class SocialSignUpRequest {

    private RegionCity regionCity;
    private String nickname;
    private String email;
}
