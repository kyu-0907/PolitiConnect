package politicConnect.user;

import jakarta.persistence.*;
import lombok.*;
import politicConnect.auth.Provider;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String localLoginId;

    @Column
    private String localLoginPassword;

    @Column(nullable = false)
    private Role role;

    private String nickName;

    private RegionCity regionCity;

    //로컬 로그인 시 직접입력, 소셜로그인 시 제공되는 메일 자동입₩
    private String email;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column
    private String providerId;


}
