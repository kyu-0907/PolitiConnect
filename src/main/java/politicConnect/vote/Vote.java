package politicConnect.vote;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int timeRequired;

    @Column(nullable = false)
    private Double participationRate;

    private Boolean completed;

    @Enumerated(EnumType.STRING)
    private VoteCategory voteCategory;

    // 진행 기간 (시작일)
    private LocalDateTime startDate;

    // 진행 기간 (종료일)
    private LocalDateTime endDate;

}
