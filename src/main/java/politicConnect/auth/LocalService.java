package politicConnect.auth;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import politicConnect.user.Role;
import politicConnect.user.User;
import politicConnect.user.UserRepository;

@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class LocalService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder 등록 가정
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;


    /**
     * Local 회원가입
     * - loginId 중복 체크
     * - 최종 가입 성공 시 db에 저장한다 토큰 발급은 시행하지 않는다
     *  (요구사항: 완가입 후 발급)
     */
    @Transactional
    public void localSignUp(LocalSignupRequest req) {

        // 1) 형식 검증 (@Valid가 해주긴 하지만 백업용)
        if (req.getLocalLoginId() == null || req.getLocalLoginId().isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }
        if (req.getLocalLoginPassword() == null || req.getLocalLoginPassword().length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        if (req.getNickName() == null || req.getNickName().isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        // 2) 중복 검사
        if (userRepository.existsByLocalLoginId(req.getLocalLoginId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByNickName(req.getNickName())) {
            throw new IllegalStateException("이미 존재하는 닉네임입니다.");
        }

        if(userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("이미 사용중인 이메일입니다");
        }

        // 3) 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(req.getLocalLoginPassword());

        // 4) User 생성 및 저장
        User user = User.builder()
                .localLoginId(req.getLocalLoginId())
                .localLoginPassword(encodedPassword)  // 암호화된 비밀번호
                .regionCity(req.getRegionCity())
                .nickName(req.getNickName())
                .email(req.getEmail())
                .role(Role.USER)          // 기본 Role 설정
                .provider(Provider.LOCAL) // local 회원임을 기록
                .build();

        userRepository.save(user);
    }


    public TokenDto localLogin (LocalLoginRequest req) {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(req.getLocalLoginId(), req.getLocalLoginPassword());

        try {
            Authentication authentication =
                    authenticationManager.authenticate(token);

            /**
             * authenticate 메서드는usernamePasswordAuthentication
             * 을 인자가 2개(id, pw)인 미인증 상태에서
             * 인자가 3개인 인증된 authentication 으로 변환한다
            */
            TokenDto tokenDto = jwtProvider.generateTokenDto(authentication);

            // 3. Refresh Token -> Redis 저장
            RefreshToken refreshToken = new RefreshToken(authentication.getName(), tokenDto.getRefreshToken());
            refreshTokenRepository.save(refreshToken);


            return tokenDto;


        } catch (BadCredentialsException e) {
            throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다");
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("존재하지 않는 아이디입니다");
        }
    }

    @Transactional
    public TokenDto reissue(String refreshTokenInCookie) {
        // 1. 토큰 유효성 검증
        if (!jwtProvider.validateToken(refreshTokenInCookie)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. 토큰에서 User ID 추출 (Subject)
        Authentication authentication = jwtProvider.getAuthentication(refreshTokenInCookie);
        String userId = authentication.getName();

        // 3. Redis 조회
        RefreshToken redisToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그아웃 되었습니다."));

        // 4. [보안] 쿠키 값 vs Redis 값 비교 (탈취 감지)
        if (!redisToken.getToken().equals(refreshTokenInCookie)) {
            throw new RuntimeException("토큰 정보가 일치하지 않습니다.");
        }

        // 5. 새 토큰 생성 및 Redis 업데이트 (Rotation)
        TokenDto newTokenDto = jwtProvider.generateTokenDto(authentication);
        refreshTokenRepository.save(new RefreshToken(userId, newTokenDto.getRefreshToken()));

        return newTokenDto;
    }

    @Transactional
    public void logout(String refreshTokenInCookie) {
        if(jwtProvider.validateToken(refreshTokenInCookie)) {
            String userId = jwtProvider.getUserIdFromToken(refreshTokenInCookie); // getAuthentication().getName()과 동일
            refreshTokenRepository.deleteById(userId);
        }
    }






    @Transactional(readOnly = true)
    public boolean isLocalLoginIdDuplicated(String localLoginId) {
        if (localLoginId == null || localLoginId.isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해 주세요.");
        }
        return userRepository.existsByLocalLoginId(localLoginId);
    }

    @Transactional(readOnly = true)
    public boolean isNickNameDuplicated(String nickName) {
        if (nickName == null || nickName.isBlank()) {
            throw new IllegalArgumentException("닉네임을 입력해 주세요.");
        }
        return userRepository.existsByNickName(nickName);
    }

    @Transactional(readOnly = true)
    public boolean isEmailDuplicated(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해 주세요.");
        }
        return userRepository.existsByEmail(email);
    }
    }


