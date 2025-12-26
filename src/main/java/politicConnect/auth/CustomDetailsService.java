package politicConnect.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import politicConnect.user.User;
import politicConnect.user.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String localLoginId) throws UsernameNotFoundException {
        // 1. DB에서 아이디로 회원 조회
        User user  = userRepository.findByLocalLoginId(localLoginId)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        return new PrincipalDetails(user);
        //
    }
}
