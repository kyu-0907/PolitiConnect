package politicConnect.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import politicConnect.user.Role;
import politicConnect.user.User;
import politicConnect.user.UserRepository;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenDto completeSignup(String userId, SocialSignUpRequest req) {

        // 1. 유저 ID로 찾기 (이메일이 null일 수도 있으니 ID로 찾는 게 확실함)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 2. [핵심 로직] 이메일 처리 전략
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            // Case A: 소셜에서 이메일을 안 줘서 DB가 비어있는 경우
            // -> 유저가 입력한 이메일(DTO)을 저장해야 함
            if (req.getEmail() == null || req.getEmail().isBlank()) {
                throw new RuntimeException("이메일은 필수 입력값입니다.");
            }
            // 이메일 중복 체크 (혹시 다른 사람이 쓰고 있는지)
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new RuntimeException("이미 사용 중인 이메일입니다.");
            }
            user.setEmail(req.getEmail()); // 저장!

        } else {
            // Case B: 이미 소셜에서 받아온 이메일이 있는 경우
            // -> DTO로 들어온 이메일은 무시(보안)하거나,
            // -> 프론트가 보낸 값과 DB 값이 다르면 에러 처리 (선택 사항)

            // 여기선 아무것도 안 함 (기존 이메일 유지)
        }

        // 3. 나머지 정보 업데이트 (Dirty Checking)
        user.setNickName(req.getNickname());
        user.setRegionCity(req.getRegionCity());

        // 4. 등급 승격 (GUEST -> USER)
        user.setRole(Role.USER);

        // 5. 새 토큰 발급 (USER 권한)
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails, null, principalDetails.getAuthorities());

        TokenDto newToken = jwtProvider.generateTokenDto(authentication);

        // 6. Redis 토큰 교체
        refreshTokenRepository.save(new RefreshToken(userId, newToken.getRefreshToken()));

        return newToken;
    }






    }

