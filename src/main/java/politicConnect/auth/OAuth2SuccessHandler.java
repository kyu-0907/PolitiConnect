package politicConnect.auth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import politicConnect.user.Role;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    // Refresh Token 만료 시간 (예: 7일 -> 초 단위)
    private static final long REFRESH_TOKEN_COOKIE_TIME = 7 * 24 * 60 * 60;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // 1. 유저 정보 꺼내기
        PrincipalDetails customUser = (PrincipalDetails) authentication.getPrincipal();
        Role role = customUser.getUser().getRole();
        String email = customUser.getUser().getEmail();

        // 2. 토큰 생성 (Service에서 TokenDto 반환 가정)
        TokenDto tokenDto = jwtProvider.generateTokenDto(authentication);

        RefreshToken redisToken = new RefreshToken(customUser.getName(), tokenDto.getRefreshToken());
        refreshTokenRepository.save(redisToken);

        // 3. Refresh Token을 HttpOnly 쿠키로 저장
        setRefreshTokenInCookie(response, tokenDto.getRefreshToken());

        // 4. Access Token만 URL 파라미터에 추가하여 리다이렉트 URL 생성
        String targetUrl = determineTargetUrl(role, email, tokenDto.getAccessToken());

        // 5. 리디렉트 수행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // 쿠키 설정 헬퍼 메서드
    private void setRefreshTokenInCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)             // JS로 접근 불가능 (XSS 방지)
                .secure(true)               // HTTPS에서만 전송 (로컬 개발 환경에 따라 false 필요할 수 있음)
                .path("/")
                .maxAge(REFRESH_TOKEN_COOKIE_TIME)
                .sameSite("None")           // 크로스 도메인 요청 시 필수
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 타겟 URL 결정 헬퍼 메서드
    private String determineTargetUrl(Role role, String email, String accessToken) {
        // 프론트엔드 주소 (application.yml 등에서 주입받는 것을 권장)
        String baseUrl = "http://localhost:3000";

        UriComponentsBuilder uriBuilder;

        if (role == Role.GUEST) {
            // [신규 유저] -> 프론트엔드 회원가입 페이지
            uriBuilder = UriComponentsBuilder.fromUriString(baseUrl + "/social/signup")
                    .queryParam("isNewUser", true)
                    .queryParam("email", email);
        } else {
            // [기존 유저] -> 프론트엔드 로그인 콜백/메인 페이지
            uriBuilder = UriComponentsBuilder.fromUriString(baseUrl + "/oauth/callback");
        }

        // 공통 파라미터 (Access Token)
        return uriBuilder
                .queryParam("accessToken", accessToken)
                .encode(StandardCharsets.UTF_8)
                .build().toUriString();
    }
}