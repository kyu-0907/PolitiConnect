package politicConnect.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth/")
@RequiredArgsConstructor
public class AuthController {

    private final LocalService localService;
    private final SocialLoginService socialLoginService;

    @PostMapping("local/signUp")
    public ResponseEntity<String> localSignup(@Valid @RequestBody LocalSignupRequest request) {

        localService.localSignUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("회원가입이 완료되었습니다");
    }

    @PostMapping("local/login")
    public ResponseEntity<LoginResponse> localLogin(@Valid @RequestBody LocalLoginRequest request) {

        // 1. Service 로직: Access + Refresh 토큰 모두 받아옴
        TokenDto tokenDto = localService.localLogin(request);

        ResponseCookie refreshCookie = createRefreshTokenCookie(tokenDto.getRefreshToken());

        LoginResponse responseBody = new LoginResponse(
                tokenDto.getAccessToken(),
                tokenDto.getAccessTokenExpiresIn()
        );

        // 4. 응답 반환: 헤더(쿠키) + 바디(Access Token)
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(responseBody);
    }

    @PostMapping("/reissue")
    public ResponseEntity<LoginResponse> reissue(
            @CookieValue(name = "refresh_token", required = false) String refreshToken){
        // 쿠키가 없으면 401 에러
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 서비스: 검증 및 새 토큰 발급 (Rotation)
        TokenDto tokenDto = localService.reissue(refreshToken);

        // 새 쿠키 생성 (덮어쓰기)
        ResponseCookie cookie = createRefreshTokenCookie(tokenDto.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(tokenDto.getAccessToken(), tokenDto.getAccessTokenExpiresIn()));


    }

    @PutMapping("/social/signup")
    public ResponseEntity<LoginResponse> completeSignup(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody SocialSignUpRequest req
    ) {
        // principal.getName()은 토큰 subject에 저장된 "userId(PK)" 입니다.
        // 이메일이 없어도 유저를 찾을 수 있는 유일한 열쇠죠.
        TokenDto tokenDto = socialLoginService.completeSignup(principal.getName(), req);

        ResponseCookie cookie = createRefreshTokenCookie(tokenDto.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(tokenDto.getAccessToken(), tokenDto.getAccessTokenExpiresIn()));
    }



    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam String localLoginId) {

        boolean duplicated = localService.isLocalLoginIdDuplicated(localLoginId);

        return ResponseEntity.ok(duplicated);
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickName(
            @RequestParam String nickName) {

        boolean duplicated = localService.isNickNameDuplicated(nickName);
        return ResponseEntity.ok(duplicated);
    }


    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .path("/")
                .sameSite("None")
                .httpOnly(true)
                .secure(true) // 로컬 개발환경이 HTTP라면 false로 잠시 변경 필요
                .maxAge(60 * 60 * 24 * 7) // 7일
                .build();
    }
}

