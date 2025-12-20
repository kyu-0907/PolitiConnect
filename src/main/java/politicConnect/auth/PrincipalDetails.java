package politicConnect.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import politicConnect.user.User;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
// 두 인터페이스를 다 구현합니다!
public class PrincipalDetails implements UserDetails, OAuth2User {

    private User user; // 우리의 User 엔티티 (DB에 저장된 정보)
    private Map<String, Object> attributes; // 소셜 로그인 정보 (Google이 준 것)

    // 1. 일반 로그인용 생성자
    public PrincipalDetails(User user) {
        this.user = user;
    }

    // 2. 소셜 로그인용 생성자
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }



    // --- [UserDetails 구현] ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> user.getRole().getKey());
        return authorities;
    }

    @Override
    public String getPassword() { return user.getLocalLoginPassword(); }

    @Override
    public String getUsername() {
        // 일반 로그인 사용자 (localLoginId 있음)
        String localId = user.getLocalLoginId();
        if (localId != null) {
            return localId;
        }

        // 소셜 로그인 사용자 (localLoginId 없음 -> providerId 대체)
        // 여기서 null을 리턴하면 에러가 납니다!
        return getName(); }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }

    // --- [OAuth2User 구현] ---
    @Override
    public Map<String, Object> getAttributes() {
        return attributes; // 소셜 로그인일 때만 값이 있고, 일반 로그인이면 null
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }
}