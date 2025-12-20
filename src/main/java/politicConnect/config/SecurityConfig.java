package politicConnect.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import politicConnect.auth.CustomOAuth2UserService;
import politicConnect.auth.JwtFilter;
import politicConnect.auth.JwtProvider;
import politicConnect.auth.OAuth2SuccessHandler;

    @Configuration
    @EnableWebSecurity
    @RequiredArgsConstructor
    public class SecurityConfig {

        private final JwtProvider jwtProvider;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;



        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
            return authenticationConfiguration.getAuthenticationManager();
        }


        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    // 요청 권한 설정
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/auth/**",
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/swagger-ui/index.html",
                                    "/v3/api-docs/**",
                                    "/swagger-resources/**",
                                    "/webjars/**",
                                    "/favicon.ico"
                            ).permitAll()
                            .anyRequest().authenticated()
                    )
                    // CSRF 완전히 비활성화
                    .csrf(AbstractHttpConfigurer::disable)
                    // JWT 필터 적용
                    .addFilterBefore(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                    // 기본 폼 로그인 및 HTTP Basic 인증 비활성화
                    .formLogin(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .oauth2Login(oauth2 -> oauth2
                                    .userInfoEndpoint(userInfo -> userInfo
                                            .userService(customOAuth2UserService)
                                    )

                            .successHandler(oAuth2SuccessHandler)
                    );


            return http.build();
        }
    }