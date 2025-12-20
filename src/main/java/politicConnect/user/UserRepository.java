package politicConnect.user;

import org.springframework.data.jpa.repository.JpaRepository;
import politicConnect.auth.Provider;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByLocalLoginId(String localLoginId);

    boolean existsByNickName(String nickname);

    boolean existsByEmail(String email);

    Optional<User> findByLocalLoginId(String localLoginId);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByProviderAndId(Provider provider, Long userId);

    Optional<User> findById(Long userId);


}
