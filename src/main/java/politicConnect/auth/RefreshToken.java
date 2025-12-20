package politicConnect.auth;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

// value: Redis의 Key prefix (예: "refreshToken:userId값")
// timeToLive: 만료 시간 (초 단위). 604800초 = 7일
@RedisHash(value = "refreshToken", timeToLive = 604800)
@AllArgsConstructor
@NoArgsConstructor // Redis 직렬화/역직렬화를 위해 기본 생성자 필요
@Getter
public class RefreshToken {

    @Id // org.springframework.data.annotation.Id (중요!)
    private String userId; // Redis의 Key로 사용됨 (유저 아이디)

    private String token;  // 실제 Refresh Token 값
}
