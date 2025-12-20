package politicConnect.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import politicConnect.user.Role;
import politicConnect.user.User;
import politicConnect.user.UserRepository;
    import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // [로그 1] 서비스 진입 확인
        log.info("========== CustomOAuth2UserService.loadUser 진입 ==========");

        // 1. 소셜 유저 정보 가져오기 (라이브러리 사용)
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // [로그 2] 카카오/구글에서 받아온 원본 데이터 확인 (여기서 id가 있는지 봐야 함)
        log.info("📢 [1] 소셜 원본 Attributes: {}", oAuth2User.getAttributes());

        // 2. provider 판별
        String provider = userRequest.getClientRegistration().getRegistrationId();
        log.info("📢 [2] 요청된 Provider: {}", provider);

        Provider socialProvider = Provider.valueOf(provider.toUpperCase());

        // 3. providerId 추출
        String providerId = extractProviderId(oAuth2User, provider);
        log.info("📢 [3] 추출된 ProviderId: {}", providerId);

        // 🚨 [중요 체크] 만약 providerId가 null이면 여기서 바로 알아야 함
        if (providerId == null || providerId.isEmpty()) {
            log.error("🚨 [치명적 오류] ProviderId가 NULL입니다! (Attributes에서 ID를 못 찾음)");
            throw new IllegalArgumentException("ProviderId cannot be null");
        }

        // 4. DB 조회
        User user = userRepository.findByProviderAndProviderId(socialProvider, providerId)
                .orElse(null);

        // 5. 신규 유저라면 저장
        if (user == null) {
            log.info("📢 [4] 신규 회원입니다. 회원가입 진행...");

            user = User.builder()
                    .email((String) oAuth2User.getAttribute("email")) // 형변환 명시
                    .provider(socialProvider)
                    .providerId(providerId)
                    .role(Role.GUEST)
                    .build();

            user = userRepository.save(user); // 저장된 객체(ID 포함)를 다시 받음
            log.info("📢 [5] 회원가입 완료. User DB ID: {}", user.getId());
        } else {
            log.info("📢 [4] 기존 회원입니다. User DB ID: {}", user.getId());
        }

        // [로그 6] 최종 리턴 직전 데이터 확인
        log.info("========== loadUser 종료 (PrincipalDetails 반환) ==========");

        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }

    // 소셜별 ID 추출기
    private String extractProviderId(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (provider.equals("google")) {
            return (String) attributes.get("sub");
        } else if (provider.equals("github")) {
            return String.valueOf(attributes.get("id")); // Integer -> String
        } else if (provider.equals("kakao")) {
            return String.valueOf(attributes.get("id")); // Long -> String
        } else if (provider.equals("naver")) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return (String) response.get("id");
        }
        throw new OAuth2AuthenticationException("Unsupported Provider: " + provider);
    }


}
