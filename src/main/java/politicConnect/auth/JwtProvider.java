package politicConnect.auth;



import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import politicConnect.user.User;
import politicConnect.user.UserRepository;

import java.security.Key;

import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {

    private static final String ROLES_CLAIM_KEY = "roles";
    private static final String PROVIDERS_CLAIM = "providers";
    private static final String BEARER_TYPE= "Bearer";

    private final Key key;

    private final long ACCESS_TOKEN_VALIDITY_SECONDS;  //15분
    private final long REFRESH_TOKEN_VALIDITY_SECONDS; //7일
    private final UserRepository userRepository;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKeyBase64,
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity,
            UserRepository userRepository){

        byte[] decodedSecretKey =  io.jsonwebtoken.io.Decoders.BASE64.decode(secretKeyBase64);

        this.key = Keys.hmacShaKeyFor(decodedSecretKey);

        this.ACCESS_TOKEN_VALIDITY_SECONDS = accessTokenValidity;
        this.REFRESH_TOKEN_VALIDITY_SECONDS = refreshTokenValidity;
        this.userRepository = userRepository;
    }

    //authentication객체를 기반으로 jwtTokenDto 생성
    //소셜은 회원가입/로그인 직후에, 로컬은 로그인 이후에 회원가입 시에 user객체가 담겨있는 authentication 객체를 리턴함)
    //authentication객체는 로컬은 usernamepasswordauthenticationToken, 소셜은 OAuth2AuthenticationToken 임.

    public TokenDto generateTokenDto(Authentication authentication){

        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

        String authorities= authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String userId = principal.getName(); //유저아이디

        // 3. Provider (Enum -> String)
        // Enum 타입이므로 .name()을 호출하여 "LOCAL" 또는 "KAKAO" 같은 문자열로 바꿈.
        String provider = principal.getUser().getProvider().name();


        long now = new Date().getTime();


        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_VALIDITY_SECONDS);

        //AccessToken 생성

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())         // payload "sub": "userId"
                .claim(ROLES_CLAIM_KEY, authorities)          // payload "roles: "common"
                .claim(PROVIDERS_CLAIM, provider)             // payload "providers": "LOCAL"
                .setExpiration(accessTokenExpiresIn)          // payload "exp": 151621022 (ex)
                .signWith(key, SignatureAlgorithm.HS512)      // header  "alg": "HS512"
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .setExpiration(new Date(now + REFRESH_TOKEN_VALIDITY_SECONDS))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return TokenDto.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();

    }

    //JWT 발급 후, 요청마다 들어오는 JWT에서 authentication객체를 뽑아 Securitycontext에 저장함
    public Authentication getAuthentication(String accessToken){

        Claims claims = parseClaims(accessToken);

        Long userId = Long.parseLong(claims.getSubject());

        Provider provider = claims.get(PROVIDERS_CLAIM, Provider.class);
        //여기서 provider도 화인해야함, 추가로 userRepository를 뒤져서 찾아야 함 -> 어떤 소셜계정 혹은 local 의 id인지 확인해야 하기 때문에

        User user = userRepository.findByProviderAndId(provider,userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        PrincipalDetails principal = new PrincipalDetails(user);

        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        //authentication 객체 리턴
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }





    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();    // 유효한 경우: 정상적으로 claims 반환
        } catch (ExpiredJwtException e) {
            return e.getClaims();  // 만료된 경우: 예외 안 터트리고 claims만 꺼내기
        }
    }

    // 1. 유저 ID만 쏙 꺼내주는 메서드
    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }



}