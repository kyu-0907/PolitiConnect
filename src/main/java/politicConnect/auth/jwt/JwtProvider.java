package politicConnect.auth.jwt;



import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.AuthProvider;
import java.security.Key;

import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {

    private static final String ROLES_CLAIM_KEY = "roles";
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

    //authenticationr객체를 기반으로 jwtTokenDto 생성
    public TokenDto generateTokenDto(Authentication authentication){
        String authorities= authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = new Date().getTime();

        //AccessToken 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_VALIDITY_SECONDS);
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())         // payload "sub": "kakaoId"
                .claim(ROLES_CLAIM_KEY, authorities)          // payload "roles: "common"
                .setExpiration(accessTokenExpiresIn)          // payload "exp": 151621022 (ex)
                .signWith(key, SignatureAlgorithm.HS512)      // header  "alg": "HS512"
                .compact();

        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_VALIDITY_SECONDS))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();

    }

    //JWT 발급 후, 요청마다 들어오는 JWT에서 authentication객체를 뽑아 Securitycontext에 저장함
    public Authentication getAuthentication(String accessToken){

        Claims claims = parseClaims(accessToken);

        String id = claims.getSubject();

        User user = userRepository.findByKakaoId(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetailsImpl principal = new UserDetailsImpl(user);

        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
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

}